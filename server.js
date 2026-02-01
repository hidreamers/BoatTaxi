case 'payment_link.succeeded':
  const link = event.data.object;
  const adId = link.metadata.adId;
  const userId = link.metadata.userId;
  // Activate ad in Firestore
  console.log(`Ad ${adId} activated for user ${userId}`);
  break;

const express = require('express');
const cors = require('cors');
const stripe = require('stripe');
const admin = require('firebase-admin');
require('dotenv').config();

console.log('Environment check:');
console.log('STRIPE_SECRET_KEY exists:', !!process.env.STRIPE_SECRET_KEY);
console.log('STRIPE_SECRET_KEY starts with:', process.env.STRIPE_SECRET_KEY?.substring(0, 10));

const app = express();
const port = process.env.PORT || 3000;

// Initialize Stripe with your secret key
const stripeClient = stripe(process.env.STRIPE_SECRET_KEY);

// Initialize Firebase Admin
const serviceAccount = require('./firebase-service-account.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// ... rest of the code

// Subscription plans mapping
const SUBSCRIPTION_PLANS = {
  'DAY_PASS': { days: 1, name: 'Day Pass', price: 199 },
  'THREE_DAY_PASS': { days: 3, name: '3 Day Pass', price: 499 },
  'FIVE_DAY_PASS': { days: 5, name: '5 Day Pass', price: 799 },
  'WEEK_PASS': { days: 7, name: 'Week Pass', price: 999 },
  'TWO_WEEK_PASS': { days: 14, name: '2 Week Pass', price: 1799 },
  'MONTH_PASS': { days: 30, name: 'Month Pass', price: 2999 }
};

// Function to update user subscription in Firestore
async function updateUserSubscription(userId, planId, session) {
  const plan = SUBSCRIPTION_PLANS[planId];
  if (!plan) {
    throw new Error(`Unknown plan: ${planId}`);
  }

  // Calculate subscription end date
  const startDate = new Date();
  const endDate = new Date();
  endDate.setDate(startDate.getDate() + plan.days);

  // Create subscription document
  const subscriptionData = {
    userId: userId,
    planId: planId,
    planName: plan.name,
    days: plan.days,
    startDate: admin.firestore.Timestamp.fromDate(startDate),
    endDate: admin.firestore.Timestamp.fromDate(endDate),
    status: 'active',
    paymentId: session.payment_intent || session.id,
    stripeSessionId: session.id,
    amount: session.amount_total / 100, // Convert from cents
    currency: session.currency,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  };

  // Save to Firestore
  const subscriptionRef = db.collection('subscriptions').doc();
  await subscriptionRef.set(subscriptionData);

  // Update user's subscription status
  const userRef = db.collection('users').doc(userId);
  await userRef.update({
    hasActiveSubscription: true,
    currentSubscriptionId: subscriptionRef.id,
    subscriptionEndDate: admin.firestore.Timestamp.fromDate(endDate),
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  });

  return subscriptionRef.id;
}

// Middleware
app.use(cors());

// Webhook endpoint for handling Stripe events (must come before express.json())
app.post('/api/webhooks', express.raw({ type: 'application/json' }), async (req, res) => {
  const sig = req.headers['stripe-signature'];
  const endpointSecret = process.env.STRIPE_WEBHOOK_SECRET;

  let event;

  try {
    event = stripeClient.webhooks.constructEvent(req.body, sig, endpointSecret);
  } catch (err) {
    console.log(`Webhook signature verification failed.`, err.message);
    return res.status(400).send(`Webhook Error: ${err.message}`);
  }

  // Handle the event
  switch (event.type) {
    case 'checkout.session.completed':
      const session = event.data.object;
      console.log('Checkout session completed:', session.id);

      // Extract user ID from client_reference_id (passed from Android app)
      const userId = session.client_reference_id;
      const planId = session.metadata?.planId;

      if (userId && planId) {
        try {
          // Update user's subscription in Firestore
          await updateUserSubscription(userId, planId, session);
          console.log(`Successfully activated ${planId} subscription for user ${userId}`);
        } catch (error) {
          console.error('Error updating subscription:', error);
        }
      } else {
        console.log('Missing userId or planId in session:', { userId, planId, metadata: session.metadata });
      }
      break;

    case 'invoice.payment_succeeded':
      // Handle recurring subscription payments
      const invoice = event.data.object;
      console.log('Invoice payment succeeded:', invoice.id);
      // TODO: Handle recurring payment success
      break;

    case 'invoice.payment_failed':
      // Handle failed recurring payments
      const failedInvoice = event.data.object;
      console.log('Invoice payment failed:', failedInvoice.id);
      // TODO: Handle payment failure, maybe suspend subscription
      break;

    case 'customer.subscription.deleted':
      // Handle subscription cancellation
      const canceledSubscription = event.data.object;
      console.log('Subscription canceled:', canceledSubscription.id);
      // TODO: Deactivate user's subscription
      break;

    default:
      console.log(`Unhandled event type ${event.type}`);
  }

  res.json({ received: true });
});

app.use(express.json());

// Create PaymentIntent endpoint
app.post('/api/create-payment-intent', async (req, res) => {
  try {
    const { planId, currency = 'usd' } = req.body;

    if (!planId || !SUBSCRIPTION_PLANS[planId]) {
      return res.status(400).json({
        error: 'Invalid plan ID'
      });
    }

    const plan = SUBSCRIPTION_PLANS[planId];

    // Create PaymentIntent
    const paymentIntent = await stripeClient.paymentIntents.create({
      amount: plan.price,
      currency: currency,
      automatic_payment_methods: {
        enabled: true,
      },
      metadata: {
        planId: planId,
        planName: plan.name
      }
    });

    res.json({
      clientSecret: paymentIntent.client_secret,
      amount: plan.price,
      currency: currency,
      planName: plan.name
    });

  } catch (error) {
    console.error('Error creating payment intent:', error);
    res.status(500).json({
      error: 'Failed to create payment intent'
    });
  }
});

// Create Checkout Session endpoint
app.post('/api/create-checkout-session', async (req, res) => {
  try {
    console.log('Received body:', req.body);
    const { planId, userId, currency = 'usd' } = req.body;

    if (!planId || !SUBSCRIPTION_PLANS[planId]) {
      return res.status(400).json({
        error: 'Invalid plan ID'
      });
    }

    if (!userId) {
      return res.status(400).json({
        error: 'User ID required'
      });
    }

    const plan = SUBSCRIPTION_PLANS[planId];

    // Create Checkout Session
    const session = await stripeClient.checkout.sessions.create({
      payment_method_types: ['card'],
      line_items: [
        {
          price_data: {
            currency: currency,
            product_data: {
              name: plan.name,
            },
            unit_amount: plan.price,
          },
          quantity: 1,
        },
      ],
      mode: 'payment',
      success_url: 'boattaxie://success', // Your app's deep link
      cancel_url: 'boattaxie://cancel',
      client_reference_id: userId,
      metadata: {
        planId: planId,
        planName: plan.name
      }
    });

    res.json({
      url: session.url,
      sessionId: session.id
    });

  } catch (error) {
    console.error('Error creating checkout session:', error);
    res.status(500).json({
      error: 'Failed to create checkout session'
    });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

app.listen(port, '0.0.0.0', () => {
  console.log(`BoatTaxie backend server running on port ${port}`);
  console.log(`Make sure to set your STRIPE_SECRET_KEY environment variable`);

});

// Create Payment Link endpoint for ads
app.post('/api/create-payment-link', async (req, res) => {
  try {
    const { amount, currency = 'usd', description, adId, userId } = req.body;

    const paymentLink = await stripeClient.paymentLinks.create({
      line_items: [
        {
          price_data: {
            currency: currency,
            product_data: { name: description },
            unit_amount: amount,
          },
          quantity: 1,
        },
      ],
      after_completion: {
        type: 'redirect',
        redirect: { url: 'boattaxie://success' }
      },
      metadata: { adId, userId }
    });

    res.json({ url: paymentLink.url });
  } catch (error) {
    console.error('Error creating payment link:', error);
    res.status(500).json({ error: 'Failed to create payment link' });
  }
});



