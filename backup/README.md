# BoatTaxie Backend API

Backend server for processing Stripe payments in the BoatTaxie Android app.

## Setup

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Set up environment variables:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and add your Stripe secret key.

3. **Get your Stripe keys:**
   - Go to [Stripe Dashboard](https://dashboard.stripe.com/apikeys)
   - Copy your **Secret key** (starts with `sk_test_` for test mode)
   - Add it to your `.env` file as `STRIPE_SECRET_KEY`

4. **Start the server:**
   ```bash
   npm start
   ```
   Or for development:
   ```bash
   npm run dev
   ```

## API Endpoints

### Create Payment Intent
**POST** `/api/create-payment-intent`

Creates a Stripe PaymentIntent for subscription payments.

**Request Body:**
```json
{
  "planId": "MONTHLY",
  "currency": "usd"
}
```

**Response:**
```json
{
  "clientSecret": "pi_xxx_secret_xxx",
  "amount": 5000,
  "currency": "usd",
  "planName": "Monthly"
}
```

### Health Check
**GET** `/api/health`

Returns server status.

## Subscription Plans

- `DAY_PASS`: $5.00 (500 cents)
- `WEEKLY`: $20.00 (2000 cents)
- `MONTHLY`: $50.00 (5000 cents)
- `YEARLY`: $500.00 (50000 cents)

## Deployment

### For Development
```bash
npm run dev
```

### For Production
Consider using services like:
- Heroku
- Railway
- Vercel
- AWS Lambda
- DigitalOcean App Platform

Make sure to:
1. Set environment variables in your deployment platform
2. Use HTTPS in production
3. Set up Stripe webhooks for payment confirmations

## Android App Integration

Update your Android app to call this backend API instead of simulating payments. Modify the `createPaymentIntent` function in `SubscriptionViewModel.kt` to make a real HTTP request to your backend.

Example:
```kotlin
// Replace the mock implementation with real API call
private suspend fun createPaymentIntent(plan: SubscriptionPlan): Result<String> {
    return try {
        val response = httpClient.post("https://your-backend-url.com/api/create-payment-intent") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(mapOf(
                "planId" to plan.name,
                "currency" to "usd"
            )))
        }

        if (response.status.isSuccess()) {
            val responseBody = Json.decodeFromString<PaymentIntentResponse>(response.bodyAsText())
            Result.success(responseBody.clientSecret)
        } else {
            Result.failure(Exception("Failed to create payment intent"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Stripe Webhook Setup

Webhooks allow Stripe to notify your server when payment events occur, enabling automatic subscription activation.

### 1. Set up Firebase Service Account

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project → Project Settings → Service Accounts
3. Click "Generate new private key"
4. Download the JSON file and save it as `firebase-service-account.json` in the backend directory

### 2. Configure Environment Variables

Add these to your `.env` file:
```env
STRIPE_WEBHOOK_SECRET=whsec_...
FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
```

### 3. Set up Webhook in Stripe Dashboard

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/webhooks)
2. Click "Add endpoint"
3. Set Endpoint URL to: `https://your-domain.com/api/webhooks`
4. Select events to listen for:
   - `checkout.session.completed` (for payment link completions)
   - `invoice.payment_succeeded` (for recurring payments)
   - `invoice.payment_failed` (for failed payments)
   - `customer.subscription.deleted` (for cancellations)
5. Copy the **Webhook signing secret** and add it to your `.env` as `STRIPE_WEBHOOK_SECRET`

### 4. Configure Payment Links with Metadata

When creating payment links in Stripe Dashboard:

1. Go to [Stripe Products](https://dashboard.stripe.com/products)
2. Create/Edit your subscription products
3. Add metadata to identify plans:
   - `planId`: `DAY_PASS`, `THREE_DAY_PASS`, etc.
4. For user identification, you can:
   - Add user ID as URL parameter: `?client_reference_id=user123`
   - Or use Stripe Customer IDs if you create customers

### 5. Test Webhook Locally (Optional)

For local development, use Stripe CLI:

```bash
# Install Stripe CLI
# Then login and forward webhooks
stripe login
stripe listen --forward-to localhost:3000/api/webhooks
```

### 6. Update Android App

The webhook will automatically activate subscriptions when payments succeed. You can remove the manual payment checking logic from `SubscriptionViewModel.kt` since the webhook handles this server-side.

### Webhook Events Handled

- **`checkout.session.completed`**: Activates user subscription in Firestore
- **`invoice.payment_succeeded`**: Handles recurring subscription payments
- **`invoice.payment_failed`**: Handles failed recurring payments
- **`customer.subscription.deleted`**: Deactivates canceled subscriptions