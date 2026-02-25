const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
require('dotenv').config();

// Schedule ad expiration to run every hour
function startAdExpirationScheduler() {
  // Run every hour (3600000 milliseconds)
  setInterval(async () => {
    try {
      const count = await expireOldAds();
      console.log(`Scheduled: Expired ${count} ads at ${new Date().toISOString()}`);
    } catch (err) {
      console.error('Scheduled ad expiration failed:', err);
    }
  }, 60 * 60 * 1000); // 1 hour
  
  console.log('Ad expiration scheduler started (runs every hour)');
}

console.log('Environment check:');
console.log('FIREBASE_PROJECT_ID exists:', !!process.env.FIREBASE_PROJECT_ID);

const app = express();
const port = process.env.PORT || 3000;

// Initialize Firebase Admin with environment variables
let firebaseConfig;
// Check for Railway environment variables
const privateKeyRaw = process.env.FIREBASE_PRIVATE_KEY;
const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;

if (privateKeyRaw && clientEmail) {
  // Handle both escaped \n and actual newlines
  let privateKey = privateKeyRaw;
  if (privateKeyRaw.includes('\\n')) {
    privateKey = privateKeyRaw.replace(/\\n/g, '\n');
  }
  console.log('Private key starts with:', privateKey.substring(0, 30));
  console.log('Private key length:', privateKey.length);
  
  // Use environment variables (for Railway)
  firebaseConfig = {
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID || 'boat-taxie',
      clientEmail: clientEmail,
      privateKey: privateKey
    })
  };
  console.log('Using Firebase credentials from environment variables');
  console.log('Client email:', clientEmail);
} else {
  // Fallback to JSON file (for local development)
  const serviceAccount = require('./firebase-service-account.json');
  firebaseConfig = {
    credential: admin.credential.cert(serviceAccount)
  };
  console.log('Using Firebase credentials from JSON file');
}

admin.initializeApp(firebaseConfig);

const db = admin.firestore();

// Subscription plans mapping
const SUBSCRIPTION_PLANS = {
  'DAY_PASS': { days: 1, name: 'Day Pass', price: 199, autoRenew: false },
  'THREE_DAY_PASS': { days: 3, name: '3 Day Pass', price: 499, autoRenew: false },
  'FIVE_DAY_PASS': { days: 5, name: '5 Day Pass', price: 799, autoRenew: false },
  'WEEK_PASS': { days: 7, name: 'Week Pass', price: 999, autoRenew: false },
  'TWO_WEEK_PASS': { days: 14, name: '2 Week Pass', price: 1799, autoRenew: false },
  'MONTH_PASS': { days: 30, name: 'Month Pass', price: 2999, autoRenew: false },
  'MONTH_PASS_AUTO': { days: 30, name: 'Monthly (Auto-Renew)', price: 2499, autoRenew: true }
};

// Ad pricing in cents (for Google Play IAP reference)
const AD_PRICES = {
  standard: {
    1: 499,    // $4.99
    3: 999,    // $9.99
    7: 1999,   // $19.99
    14: 2999,  // $29.99
    30: 4999,  // $49.99
    'monthly_auto': 3999  // $39.99/month auto-renew
  },
  featured: {
    1: 999,    // $9.99
    3: 1999,   // $19.99
    7: 3499,   // $34.99
    14: 5499,  // $54.99
    30: 8999,  // $89.99
    'monthly_auto': 6999  // $69.99/month auto-renew
  }
};

// Function to activate an ad after payment (Google Play IAP)
async function activateAd(adId, purchaseData) {
  try {
    // Determine which collection the ad is in from purchase metadata
    let collection = purchaseData?.collection || 'ads';
    let durationDays = parseInt(purchaseData?.durationDays) || 7;
    
    // Try to get the ad document
    let adDoc = await db.collection(collection).doc(adId).get();
    
    // If not found in the specified collection, try the other one
    if (!adDoc.exists) {
      const otherCollection = collection === 'ads' ? 'advertisements' : 'ads';
      adDoc = await db.collection(otherCollection).doc(adId).get();
      if (adDoc.exists) {
        collection = otherCollection;
        console.log(`Ad ${adId} found in ${collection} collection instead`);
      } else {
        throw new Error(`Ad ${adId} not found in either collection`);
      }
    }
    
    const adData = adDoc.data();
    // Use durationDays from purchase metadata, falling back to ad data, then to 7
    if (!durationDays || isNaN(durationDays)) {
      durationDays = adData.durationDays || 7;
    }
    
    // Calculate start and end dates
    const startDate = new Date();
    const endDate = new Date();
    endDate.setDate(startDate.getDate() + durationDays);
    
    // Update the ad with active status and dates
    const updateData = {
      status: 'ACTIVE',  // Use uppercase to match app's enum
      startDate: admin.firestore.Timestamp.fromDate(startDate),
      endDate: admin.firestore.Timestamp.fromDate(endDate),
      activatedAt: admin.firestore.FieldValue.serverTimestamp(),
      paymentId: purchaseData.purchaseToken || purchaseData.orderId,
      googlePlayOrderId: purchaseData.orderId,
      amountPaid: purchaseData.amountPaid || 0,
      paymentStatus: 'COMPLETED',
      paymentMethod: 'GOOGLE_PLAY',
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };
    
    // For advertisements collection (AI-generated), also set coupon expiry
    if (collection === 'advertisements' && adData.hasCoupon) {
      updateData.couponExpiresAt = admin.firestore.Timestamp.fromDate(endDate);
    }
    
    await db.collection(collection).doc(adId).update(updateData);
    
    console.log(`Ad ${adId} in ${collection} activated until ${endDate.toISOString()}`);
    return true;
  } catch (error) {
    console.error(`Error activating ad ${adId}:`, error);
    throw error;
  }
}

// Function to check and expire old ads
async function expireOldAds() {
  try {
    const now = admin.firestore.Timestamp.now();
    
    // Find all active ads that have expired
    const expiredAdsSnapshot = await db.collection('ads')
      .where('status', '==', 'active')
      .where('endDate', '<=', now)
      .get();
    
    const batch = db.batch();
    let expiredCount = 0;
    
    expiredAdsSnapshot.forEach(doc => {
      batch.update(doc.ref, {
        status: 'expired',
        expiredAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
      expiredCount++;
    });
    
    if (expiredCount > 0) {
      await batch.commit();
      console.log(`Expired ${expiredCount} ads`);
    }
    
    return expiredCount;
  } catch (error) {
    console.error('Error expiring ads:', error);
    throw error;
  }
}

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

    // In production, you should verify the purchase with Google Play Developer API
    // For now, we trust the client and activate the subscription
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

// Verify Google Play ad purchase and activate ad
app.post('/api/verify-ad-purchase', async (req, res) => {
  try {
    const { purchaseToken, orderId, productId, adId, durationDays, amountPaid, collection } = req.body;

    if (!purchaseToken || !adId) {
      return res.status(400).json({
        error: 'purchaseToken and adId are required'
      });
    }

    console.log(`Verifying ad purchase: ${orderId} for ad ${adId}`);

    const purchaseData = {
      purchaseToken: purchaseToken,
      orderId: orderId,
      productId: productId,
      durationDays: durationDays || 7,
      amountPaid: amountPaid || 0,
      collection: collection || 'ads'
    };

    // Activate the ad
    await activateAd(adId, purchaseData);

    res.json({
      success: true,
      adId: adId,
      message: 'Ad activated successfully'
    });

  } catch (error) {
    console.error('Error verifying ad purchase:', error);
    res.status(500).json({
      error: 'Failed to verify ad purchase',
      details: error.message
    });
  }
});

// Get available IAP products info (for reference)
app.get('/api/iap-products', async (req, res) => {
  try {
    res.json({
      subscriptions: SUBSCRIPTION_PLANS,
      ads: AD_PRICES
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to get products' });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Admin user ID - your account for receiving verification notifications
const ADMIN_USER_ID = process.env.ADMIN_USER_ID || 'jerimiah_admin';
const ADMIN_EMAIL = 'jerimiah@lacunabotanicals.com';

// Notify admin about new driver verification request
app.post('/api/notify-admin-verification', async (req, res) => {
  try {
    const { submissionId, userName, vehicleType } = req.body;
    
    console.log(`New verification request: ${submissionId} from ${userName} for ${vehicleType}`);
    
    // Get admin user(s) to send push notification
    // Find users with admin flag or specific admin email
    const adminUsersSnapshot = await db.collection('users')
      .where('email', '==', ADMIN_EMAIL)
      .limit(1)
      .get();
    
    if (adminUsersSnapshot.empty) {
      console.log('No admin user found with email:', ADMIN_EMAIL);
      return res.json({ success: true, message: 'No admin found to notify' });
    }
    
    const adminDoc = adminUsersSnapshot.docs[0];
    const adminData = adminDoc.data();
    const fcmToken = adminData.fcmToken;
    
    if (!fcmToken) {
      console.log('Admin user has no FCM token');
      return res.json({ success: true, message: 'Admin has no FCM token' });
    }
    
    // Send push notification to admin
    const message = {
      token: fcmToken,
      notification: {
        title: '🚗 New Driver Verification',
        body: `${userName} wants to become a ${vehicleType} driver. Tap to review.`
      },
      data: {
        type: 'driver_verification',
        submissionId: submissionId,
        userName: userName,
        vehicleType: vehicleType
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'verification',
          priority: 'high',
          defaultSound: true
        }
      }
    };
    
    const response = await admin.messaging().send(message);
    console.log('Push notification sent to admin:', response);
    
    res.json({ success: true, messageId: response });
  } catch (error) {
    console.error('Error notifying admin:', error);
    res.status(500).json({ error: 'Failed to notify admin', details: error.message });
  }
});

// Get pending verification submissions (for admin)
app.get('/api/admin/verifications/pending', async (req, res) => {
  try {
    const snapshot = await db.collection('verification_submissions')
      .where('overallStatus', '==', 'PENDING')
      .orderBy('submittedAt', 'desc')
      .limit(50)
      .get();
    
    const submissions = [];
    for (const doc of snapshot.docs) {
      const data = doc.data();
      
      // Get user info
      const userDoc = await db.collection('users').doc(data.userId).get();
      const userData = userDoc.exists ? userDoc.data() : {};
      
      submissions.push({
        id: doc.id,
        ...data,
        userName: userData.fullName || userData.email || 'Unknown',
        userEmail: userData.email || '',
        userPhone: userData.phoneNumber || ''
      });
    }
    
    res.json({ submissions });
  } catch (error) {
    console.error('Error fetching pending verifications:', error);
    res.status(500).json({ error: 'Failed to fetch verifications' });
  }
});

// Approve driver verification (for admin)
app.post('/api/admin/verifications/:submissionId/approve', async (req, res) => {
  try {
    const { submissionId } = req.params;
    const { adminNotes } = req.body;
    
    // Get submission
    const submissionDoc = await db.collection('verification_submissions').doc(submissionId).get();
    if (!submissionDoc.exists) {
      return res.status(404).json({ error: 'Submission not found' });
    }
    
    const submission = submissionDoc.data();
    const userId = submission.userId;
    const vehicleType = submission.vehicleType;
    
    // Update submission status
    await db.collection('verification_submissions').doc(submissionId).update({
      overallStatus: 'APPROVED',
      status: 'approved',
      reviewedAt: admin.firestore.FieldValue.serverTimestamp(),
      adminNotes: adminNotes || 'Approved'
    });
    
    // Update user - mark as verified driver
    const userType = vehicleType === 'TAXI' ? 'driver' : 'captain';
    await db.collection('users').doc(userId).update({
      verificationStatus: 'approved',
      isVerified: true,
      isLocalResident: true,
      canBeDriver: true,
      userType: userType,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // Send push notification to driver that they're approved
    const userDoc = await db.collection('users').doc(userId).get();
    const userData = userDoc.data();
    if (userData && userData.fcmToken) {
      const message = {
        token: userData.fcmToken,
        notification: {
          title: '✅ Verification Approved!',
          body: `Congratulations! You're now a verified ${vehicleType} driver. Start accepting rides!`
        },
        data: {
          type: 'verification_approved',
          vehicleType: vehicleType
        }
      };
      await admin.messaging().send(message);
      console.log('Approval notification sent to driver');
    }
    
    res.json({ success: true, message: 'Driver approved successfully' });
  } catch (error) {
    console.error('Error approving verification:', error);
    res.status(500).json({ error: 'Failed to approve verification' });
  }
});

// Reject driver verification (for admin)
app.post('/api/admin/verifications/:submissionId/reject', async (req, res) => {
  try {
    const { submissionId } = req.params;
    const { reason } = req.body;
    
    // Get submission
    const submissionDoc = await db.collection('verification_submissions').doc(submissionId).get();
    if (!submissionDoc.exists) {
      return res.status(404).json({ error: 'Submission not found' });
    }
    
    const submission = submissionDoc.data();
    const userId = submission.userId;
    
    // Update submission status
    await db.collection('verification_submissions').doc(submissionId).update({
      overallStatus: 'REJECTED',
      status: 'rejected',
      reviewedAt: admin.firestore.FieldValue.serverTimestamp(),
      adminNotes: reason || 'Rejected'
    });
    
    // Update user
    await db.collection('users').doc(userId).update({
      verificationStatus: 'rejected',
      isVerified: false,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // Send push notification to driver that they're rejected
    const userDoc = await db.collection('users').doc(userId).get();
    const userData = userDoc.data();
    if (userData && userData.fcmToken) {
      const message = {
        token: userData.fcmToken,
        notification: {
          title: '❌ Verification Not Approved',
          body: reason || 'Your driver verification was not approved. Please check your documents and try again.'
        },
        data: {
          type: 'verification_rejected',
          reason: reason || 'Documents not accepted'
        }
      };
      await admin.messaging().send(message);
      console.log('Rejection notification sent to driver');
    }
    
    res.json({ success: true, message: 'Driver rejected' });
  } catch (error) {
    console.error('Error rejecting verification:', error);
    res.status(500).json({ error: 'Failed to reject verification' });
  }
});

// Create Draft Ad endpoint (prepares ad for Google Play IAP payment)
app.post('/api/create-draft-ad', async (req, res) => {
  try {
    const { durationDays, isFeatured, businessName, title, description, imageUri, logoUri, youtubeUrl, phone, email, website, category, location, userId, existingAdId } = req.body;

    const price = AD_PRICES[isFeatured ? 'featured' : 'standard'][durationDays];

    if (!price) {
      return res.status(400).json({ error: 'Invalid duration or featured status' });
    }

    let adId;
    let collection = 'ads';  // Default collection for new ads

    if (existingAdId) {
      // Check if the existing ad is in 'advertisements' collection (AI-generated ads)
      const advertisementsDoc = await db.collection('advertisements').doc(existingAdId).get();
      if (advertisementsDoc.exists) {
        collection = 'advertisements';
        adId = existingAdId;
        console.log(`Using existing ad ${adId} from ${collection} collection`);
      } else {
        // Check 'ads' collection
        const adsDoc = await db.collection('ads').doc(existingAdId).get();
        if (adsDoc.exists) {
          collection = 'ads';
          adId = existingAdId;
          console.log(`Using existing ad ${adId} from ${collection} collection`);
        } else {
          return res.status(404).json({ error: 'Ad not found' });
        }
      }
    } else {
      // Create new ad in Firestore as draft
      const adData = {
        businessName: businessName || '',
        title: title || '',
        description: description || '',
        imageUri: imageUri || '',
        logoUri: logoUri || '',
        youtubeUrl: youtubeUrl || '',
        phone: phone || '',
        email: email || '',
        website: website || '',
        category: category || '',
        durationDays: durationDays || 7,
        isFeatured: isFeatured || false,
        location: location || '',
        userId: userId || '',
        status: 'PENDING_PAYMENT',
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      };
      
      const adRef = await db.collection('ads').add(adData);
      adId = adRef.id;
      console.log(`Created new draft ad ${adId}`);
    }

    // Return ad info for Google Play IAP purchase
    res.json({ 
      adId: adId,
      collection: collection,
      durationDays: durationDays,
      isFeatured: isFeatured,
      price: price,
      productId: `ad_${isFeatured ? 'featured' : 'standard'}_${durationDays}day`
    });

  } catch (error) {
    console.error('Error creating draft ad:', error);
    res.status(500).json({ error: 'Failed to create draft ad' });
  }
});

// Endpoint to manually expire old ads (can be called by a cron job)
app.post('/api/expire-ads', async (req, res) => {
  try {
    const expiredCount = await expireOldAds();
    res.json({ success: true, expiredCount });
  } catch (error) {
    console.error('Error in expire-ads endpoint:', error);
    res.status(500).json({ error: 'Failed to expire ads' });
  }
});

// Get active ads for the app
app.get('/api/ads/active', async (req, res) => {
  try {
    // First expire any old ads
    await expireOldAds();
    
    // Get all active ads
    const adsSnapshot = await db.collection('ads')
      .where('status', '==', 'active')
      .orderBy('isFeatured', 'desc')
      .orderBy('createdAt', 'desc')
      .limit(50)
      .get();
    
    const ads = [];
    adsSnapshot.forEach(doc => {
      ads.push({ id: doc.id, ...doc.data() });
    });
    
    res.json({ ads });
  } catch (error) {
    console.error('Error fetching active ads:', error);
    res.status(500).json({ error: 'Failed to fetch ads' });
  }
});

// Get ad status by ID
app.get('/api/ads/:adId/status', async (req, res) => {
  try {
    const { adId } = req.params;
    const adDoc = await db.collection('ads').doc(adId).get();
    
    if (!adDoc.exists) {
      return res.status(404).json({ error: 'Ad not found' });
    }
    
    const adData = adDoc.data();
    res.json({
      id: adId,
      status: adData.status,
      startDate: adData.startDate?.toDate(),
      endDate: adData.endDate?.toDate()
    });
  } catch (error) {
    console.error('Error fetching ad status:', error);
    res.status(500).json({ error: 'Failed to fetch ad status' });
  }
});

// ========================================
// EXPLORE PLACES API - Cached nearby search
// ========================================

// Cache duration in milliseconds (2 hours)
const PLACES_CACHE_DURATION = 2 * 60 * 60 * 1000;

// Get nearby places with caching
app.get('/api/places/nearby', async (req, res) => {
  try {
    const { lat, lng, radius = 5000, category = 'all', forceRefresh = false } = req.query;
    
    if (!lat || !lng) {
      return res.status(400).json({ error: 'lat and lng are required' });
    }
    
    // Create a cache key based on location (rounded to reduce cache misses)
    const roundedLat = Math.round(parseFloat(lat) * 100) / 100;
    const roundedLng = Math.round(parseFloat(lng) * 100) / 100;
    const cacheKey = `places_${roundedLat}_${roundedLng}_${category}_${radius}`;
    
    // Check cache first (unless force refresh)
    if (!forceRefresh) {
      const cacheDoc = await db.collection('places_cache').doc(cacheKey).get();
      if (cacheDoc.exists) {
        const cacheData = cacheDoc.data();
        const cacheAge = Date.now() - cacheData.cachedAt.toMillis();
        
        if (cacheAge < PLACES_CACHE_DURATION) {
          console.log(`Places cache hit for ${cacheKey}, age: ${Math.round(cacheAge / 60000)} minutes`);
          return res.json({
            places: cacheData.places,
            fromCache: true,
            cacheAge: cacheAge
          });
        }
      }
    }
    
    // Cache miss or expired - fetch from Google Places API
    console.log(`Places cache miss for ${cacheKey}, fetching from Google...`);
    
    const apiKey = process.env.GOOGLE_MAPS_API_KEY;
    if (!apiKey) {
      return res.status(500).json({ error: 'Google Maps API key not configured' });
    }
    
    // Map category to Google Places type
    const typeMap = {
      'all': '',
      'restaurants': 'restaurant',
      'bars': 'bar|night_club',
      'tours': 'travel_agency|tourist_attraction',
      'hotels': 'lodging',
      'attractions': 'tourist_attraction|museum|amusement_park',
      'shopping': 'shopping_mall|store'
    };
    
    const placeType = typeMap[category] || '';
    
    // Build Google Places API URL
    let url = `https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${lat},${lng}&radius=${radius}&key=${apiKey}`;
    if (placeType) {
      url += `&type=${placeType.split('|')[0]}`; // API only accepts single type
    }
    
    const fetch = (await import('node-fetch')).default;
    const response = await fetch(url);
    const data = await response.json();
    
    if (data.status !== 'OK' && data.status !== 'ZERO_RESULTS') {
      console.error('Google Places API error:', data.status, data.error_message);
      return res.status(500).json({ error: 'Failed to fetch places', details: data.status });
    }
    
    // Transform places to our format
    const places = (data.results || []).map(place => ({
      placeId: place.place_id,
      name: place.name,
      address: place.vicinity || '',
      lat: place.geometry?.location?.lat || 0,
      lng: place.geometry?.location?.lng || 0,
      rating: place.rating || null,
      userRatingsTotal: place.user_ratings_total || null,
      priceLevel: place.price_level || null,
      types: place.types || [],
      isOpenNow: place.opening_hours?.open_now || null,
      photoReference: place.photos?.[0]?.photo_reference || null,
      iconUrl: place.icon || null,
      businessStatus: place.business_status || null
    }));
    
    // Store in cache
    await db.collection('places_cache').doc(cacheKey).set({
      places: places,
      cachedAt: admin.firestore.FieldValue.serverTimestamp(),
      location: { lat: parseFloat(lat), lng: parseFloat(lng) },
      category: category,
      radius: parseInt(radius)
    });
    
    console.log(`Cached ${places.length} places for ${cacheKey}`);
    
    res.json({
      places: places,
      fromCache: false,
      count: places.length
    });
    
  } catch (error) {
    console.error('Error fetching nearby places:', error);
    res.status(500).json({ error: 'Failed to fetch places', details: error.message });
  }
});

// Get place details (also cached)
app.get('/api/places/:placeId/details', async (req, res) => {
  try {
    const { placeId } = req.params;
    
    // Check cache first
    const cacheDoc = await db.collection('place_details_cache').doc(placeId).get();
    if (cacheDoc.exists) {
      const cacheData = cacheDoc.data();
      const cacheAge = Date.now() - cacheData.cachedAt.toMillis();
      
      // Place details cache for 24 hours (they don't change often)
      if (cacheAge < 24 * 60 * 60 * 1000) {
        return res.json({
          place: cacheData.place,
          fromCache: true
        });
      }
    }
    
    const apiKey = process.env.GOOGLE_MAPS_API_KEY;
    if (!apiKey) {
      return res.status(500).json({ error: 'Google Maps API key not configured' });
    }
    
    const fields = 'place_id,name,formatted_address,formatted_phone_number,website,opening_hours,rating,user_ratings_total,reviews,geometry,photos,types,price_level,business_status';
    const url = `https://maps.googleapis.com/maps/api/place/details/json?place_id=${placeId}&fields=${fields}&key=${apiKey}`;
    
    const fetch = (await import('node-fetch')).default;
    const response = await fetch(url);
    const data = await response.json();
    
    if (data.status !== 'OK') {
      console.error('Google Place Details API error:', data.status);
      return res.status(500).json({ error: 'Failed to fetch place details' });
    }
    
    const result = data.result;
    const place = {
      placeId: result.place_id,
      name: result.name,
      address: result.formatted_address || '',
      phoneNumber: result.formatted_phone_number || null,
      website: result.website || null,
      lat: result.geometry?.location?.lat || 0,
      lng: result.geometry?.location?.lng || 0,
      rating: result.rating || null,
      userRatingsTotal: result.user_ratings_total || null,
      priceLevel: result.price_level || null,
      types: result.types || [],
      isOpenNow: result.opening_hours?.open_now || null,
      openingHours: result.opening_hours?.weekday_text || null,
      photoReference: result.photos?.[0]?.photo_reference || null,
      businessStatus: result.business_status || null,
      reviews: (result.reviews || []).slice(0, 5).map(r => ({
        author: r.author_name,
        rating: r.rating,
        text: r.text,
        time: r.relative_time_description
      }))
    };
    
    // Cache it
    await db.collection('place_details_cache').doc(placeId).set({
      place: place,
      cachedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    res.json({
      place: place,
      fromCache: false
    });
    
  } catch (error) {
    console.error('Error fetching place details:', error);
    res.status(500).json({ error: 'Failed to fetch place details', details: error.message });
  }
});

// Clear places cache (admin endpoint)
app.post('/api/admin/clear-places-cache', async (req, res) => {
  try {
    const batchSize = 100;
    let deleted = 0;
    
    // Delete places_cache collection
    const placesSnapshot = await db.collection('places_cache').limit(batchSize).get();
    if (!placesSnapshot.empty) {
      const batch = db.batch();
      placesSnapshot.docs.forEach(doc => batch.delete(doc.ref));
      await batch.commit();
      deleted += placesSnapshot.size;
    }
    
    // Delete place_details_cache collection
    const detailsSnapshot = await db.collection('place_details_cache').limit(batchSize).get();
    if (!detailsSnapshot.empty) {
      const batch = db.batch();
      detailsSnapshot.docs.forEach(doc => batch.delete(doc.ref));
      await batch.commit();
      deleted += detailsSnapshot.size;
    }
    
    res.json({ success: true, deleted: deleted });
  } catch (error) {
    console.error('Error clearing places cache:', error);
    res.status(500).json({ error: 'Failed to clear cache' });
  }
});

app.listen(port, '0.0.0.0', () => {
  console.log(`BoatTaxie backend server running on port ${port}`);
  console.log(`Using Google Play In-App Purchases for payments`);
  
  // Run ad expiration check on startup
  expireOldAds().then(count => {
    console.log(`Startup: Expired ${count} ads`);
    // Start the hourly scheduler after startup check
    startAdExpirationScheduler();
  }).catch(err => {
    console.error('Startup ad expiration failed:', err);
  });
});



