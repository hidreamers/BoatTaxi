const functions = require('firebase-functions');
const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');

const app = express();

// Initialize Firebase Admin
admin.initializeApp();

const db = admin.firestore();

// Subscription plans mapping
const SUBSCRIPTION_PLANS = {
  'DAY_PASS': { days: 1, name: 'Day Pass', price: 199 },
  'THREE_DAY_PASS': { days: 3, name: '3 Day Pass', price: 499 },
  'FIVE_DAY_PASS': { days: 5, name: '5 Day Pass', price: 799 },
  'WEEK_PASS': { days: 7, name: 'Week Pass', price: 999 },
  'TWO_WEEK_PASS': { days: 14, name: '2 Week Pass', price: 1799 },
  'MONTH_PASS': { days: 30, name: 'Month Pass', price: 2999 }
};

// Function to update user subscription in Firestore (Google Play IAP)
async function updateUserSubscription(userId, planId, purchaseData) {
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
    paymentId: purchaseData.purchaseToken || purchaseData.orderId,
    googlePlayOrderId: purchaseData.orderId,
    amount: plan.price / 100, // Convert from cents
    currency: 'usd',
    paymentMethod: 'GOOGLE_PLAY',
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
app.use(express.json());

// ========================================
// GOOGLE PLAY IN-APP PURCHASE VERIFICATION
// ========================================

// Verify Google Play subscription purchase and activate
app.post('/api/verify-subscription-purchase', async (req, res) => {
  try {
    const { purchaseToken, orderId, productId, userId, planId } = req.body;

    if (!purchaseToken || !userId || !planId) {
      return res.status(400).json({
        error: 'purchaseToken, userId, and planId are required'
      });
    }

    if (!SUBSCRIPTION_PLANS[planId]) {
      return res.status(400).json({
        error: 'Invalid plan ID'
      });
    }

    // In production, verify the purchase with Google Play Developer API
    console.log(`Verifying subscription purchase: ${orderId} for user ${userId}`);

    const purchaseData = {
      purchaseToken: purchaseToken,
      orderId: orderId,
      productId: productId
    };

    // Activate the subscription
    const subscriptionId = await updateUserSubscription(userId, planId, purchaseData);

    res.json({
      success: true,
      subscriptionId: subscriptionId,
      message: 'Subscription activated successfully'
    });

  } catch (error) {
    console.error('Error verifying subscription purchase:', error);
    res.status(500).json({
      error: 'Failed to verify subscription purchase',
      details: error.message
    });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Get available IAP products info
app.get('/api/iap-products', async (req, res) => {
  try {
    res.json({
      subscriptions: SUBSCRIPTION_PLANS
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to get products' });
  }
});

// Export the Express app as a Firebase Cloud Function
exports.api = functions.https.onRequest(app);