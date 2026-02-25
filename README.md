# BoatTaxie Backend API

Backend server for processing Google Play In-App Purchases in the BoatTaxie Android app.

## Setup

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Set up environment variables:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and configure Firebase credentials.

3. **Set up Firebase:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project → Project Settings → Service Accounts
   - Generate new private key and save as `firebase-service-account.json`

4. **Start the server:**
   ```bash
   npm start
   ```
   Or for development:
   ```bash
   npm run dev
   ```

## API Endpoints

### Verify Subscription Purchase
**POST** `/api/verify-subscription-purchase`

Verifies a Google Play subscription purchase and activates it.

**Request Body:**
```json
{
  "purchaseToken": "purchase_token_from_google_play",
  "orderId": "GPA.xxx",
  "productId": "week_pass",
  "userId": "firebase_user_id",
  "planId": "WEEK_PASS"
}
```

**Response:**
```json
{
  "success": true,
  "subscriptionId": "subscription_doc_id",
  "message": "Subscription activated successfully"
}
```

### Verify Ad Purchase
**POST** `/api/verify-ad-purchase`

Verifies a Google Play ad purchase and activates the ad.

**Request Body:**
```json
{
  "purchaseToken": "purchase_token_from_google_play",
  "orderId": "GPA.xxx",
  "productId": "ad_standard_7day",
  "adId": "firebase_ad_id",
  "durationDays": 7,
  "amountPaid": 1999
}
```

### Create Draft Ad
**POST** `/api/create-draft-ad`

Creates a draft ad pending payment via Google Play IAP.

### Get IAP Products
**GET** `/api/iap-products`

Returns available subscription plans and ad pricing.

### Health Check
**GET** `/api/health`

Returns server status.

## Subscription Plans

- `DAY_PASS`: $1.99 (199 cents) - 1 day
- `THREE_DAY_PASS`: $4.99 (499 cents) - 3 days
- `FIVE_DAY_PASS`: $7.99 (799 cents) - 5 days
- `WEEK_PASS`: $9.99 (999 cents) - 7 days
- `TWO_WEEK_PASS`: $17.99 (1799 cents) - 14 days
- `MONTH_PASS`: $29.99 (2999 cents) - 30 days

## Google Play Console Setup

### 1. Create In-App Products

In Google Play Console:
1. Go to your app → Monetise → Products → In-app products
2. Create products with these IDs:
   - `day_pass`, `three_day_pass`, `five_day_pass`, etc.
   - `ad_standard_1day`, `ad_standard_7day`, `ad_featured_7day`, etc.
3. Set pricing for each product
4. Activate the products

### 2. Configure Play Billing Library

The Android app uses `com.android.billingclient:billing-ktx:6.1.0` for purchases.

## Deployment

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
3. Configure Firebase service account credentials

## Android App Integration

The Android app handles purchases through `BillingManager.kt`:

1. User selects a subscription plan
2. App launches Google Play purchase flow
3. On successful purchase, app calls `/api/verify-subscription-purchase`
4. Backend verifies and activates subscription in Firestore

## Security Notes

- In production, verify purchase tokens with Google Play Developer API
- Store Firebase service account credentials securely
- Never expose sensitive keys in client-side code