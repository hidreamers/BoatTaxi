# 🚤🚕 BoatTaxie

A ride-sharing mobile application for boats and taxis - like Uber, but for water and land transportation!

## 📱 Overview

BoatTaxie is a comprehensive Android application that connects riders with boat captains and taxi drivers. The app features:

- **Dual Transportation**: Book either boats or taxis based on your needs
- **Real-time Tracking**: Live GPS tracking of drivers and captains
- **Flexible Subscriptions**: Pay-as-you-go pricing starting at $2.99/day
- **Free for Drivers**: Verified boat owners and taxi drivers use the app for free
- **Local Advertising**: Businesses can post paid ads to reach users

## 🎯 Key Features

### For Riders
- **Easy Booking**: Book a boat or taxi with just a few taps
- **Real-time Tracking**: Watch your driver approach on the map
- **Fare Estimates**: Know your fare before you ride
- **Multiple Payment Methods**: Add cards via Stripe
- **Subscription Plans**:
  - 1 Day Pass: $2.99
  - 3 Day Pass: $7.99
  - 5 Day Pass: $11.99
  - Week Pass: $14.99
  - 2 Week Pass: $24.99
  - Month Pass: $49.99
- **Trip History**: View all your past rides
- **Rating System**: Rate drivers and leave reviews

### For Drivers & Captains
- **FREE to Use**: No subscription fees for verified drivers
- **Easy Verification**: Upload vehicle photos and documents
- **Real-time Requests**: Receive ride requests with fare info
- **Earnings Dashboard**: Track daily and weekly earnings
- **Navigation Integration**: Built-in maps for pickups and drop-offs
- **Flexible Hours**: Go online/offline whenever you want

### For Advertisers
- **Local Business Ads**: Post ads for restaurants, shops, services
- **Multiple Ad Plans**:
  - 1 Week: $9.99 (Featured: $19.99)
  - 2 Weeks: $17.99 (Featured: $34.99)
  - 1 Month: $29.99 (Featured: $59.99)
  - 3 Months: $74.99 (Featured: $149.99)
- **Analytics**: Track impressions and clicks
- **Featured Placement**: Get premium visibility

## 🛠 Tech Stack

### Frontend
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt/Dagger

### Backend Services
- **Authentication**: Firebase Auth
- **Database**: Cloud Firestore
- **Storage**: Firebase Storage
- **Push Notifications**: Firebase Cloud Messaging
- **Analytics**: Firebase Analytics

### Maps & Location
- **Maps**: Google Maps SDK for Android
- **Maps Compose**: Compose integration for Google Maps
- **Location**: Fused Location Provider

### Payments
- **Payment Processing**: PayPal Android SDK
- **Subscription Management**: PayPal Checkout

### Other
- **Image Loading**: Coil
- **Camera**: AndroidX Camera
- **Local Database**: Room
- **Ads Display**: Google AdMob

## 📁 Project Structure

```
app/src/main/java/com/boattaxie/app/
├── data/
│   ├── model/           # Data classes
│   │   ├── User.kt
│   │   ├── Vehicle.kt
│   │   ├── Verification.kt
│   │   ├── Booking.kt
│   │   ├── Subscription.kt
│   │   └── Advertisement.kt
│   └── repository/      # Data access layer
│       ├── AuthRepository.kt
│       ├── VerificationRepository.kt
│       ├── BookingRepository.kt
│       ├── SubscriptionRepository.kt
│       └── AdvertisementRepository.kt
├── di/                  # Dependency injection
│   └── AppModule.kt
├── navigation/          # App navigation
│   ├── Screen.kt
│   └── NavHost.kt
├── service/             # Background services
│   ├── LocationTrackingService.kt
│   └── BoatTaxieMessagingService.kt
└── ui/
    ├── theme/           # App theming
    │   ├── Color.kt
    │   ├── Shape.kt
    │   ├── Type.kt
    │   └── Theme.kt
    ├── components/      # Reusable UI components
    │   └── CommonComponents.kt
    ├── MainActivity.kt
    └── screens/
        ├── auth/        # Authentication screens
        ├── verification/# Driver verification screens
        ├── home/        # Home and dashboard
        ├── booking/     # Ride booking flow
        ├── subscription/# Subscription management
        ├── ads/         # Advertisement screens
        ├── driver/      # Driver/Captain screens
        └── profile/     # User profile screens
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 34
- Google Play Services

### Configuration

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/boattaxie.git
   cd boattaxie
   ```

2. **Set up Firebase**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Enable Authentication (Email/Password, Google Sign-In)
   - Create a Firestore database
   - Enable Firebase Storage
   - Download `google-services.json` and place it in `app/`

3. **Configure Google Maps**
   - Get a Google Maps API key from [Google Cloud Console](https://console.cloud.google.com)
   - Add to `local.properties`:
     ```properties
     MAPS_API_KEY=your_api_key_here
     ```

4. **Configure PayPal**
   - Create a PayPal Developer account at [PayPal Developer](https://developer.paypal.com)
   - Create an app to get your Client ID
   - Add to `local.properties`:
     ```properties
     PAYPAL_CLIENT_ID=your_client_id_here
     ```

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## 📋 Firestore Collections

### Users Collection
```
users/{userId}
├── email: string
├── firstName: string
├── lastName: string
├── userType: "RIDER" | "CAPTAIN" | "DRIVER"
├── verificationStatus: "NOT_STARTED" | "PENDING" | "VERIFIED" | "REJECTED"
├── rating: number
├── totalTrips: number
└── fcmToken: string
```

### Bookings Collection
```
bookings/{bookingId}
├── riderId: string
├── driverId: string
├── vehicleType: "BOAT" | "TAXI"
├── status: "PENDING" | "ACCEPTED" | "ARRIVED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED"
├── pickupLocation: GeoPoint
├── dropoffLocation: GeoPoint
├── estimatedFare: number
├── actualFare: number
└── createdAt: timestamp
```

### Subscriptions Collection
```
subscriptions/{subscriptionId}
├── userId: string
├── plan: "DAY_PASS" | "THREE_DAY_PASS" | ...
├── status: "ACTIVE" | "EXPIRED" | "CANCELLED"
├── startDate: timestamp
└── endDate: timestamp
```

### Advertisements Collection
```
advertisements/{adId}
├── ownerId: string
├── businessName: string
├── title: string
├── description: string
├── imageUrl: string
├── status: "PENDING" | "ACTIVE" | "PAUSED" | "EXPIRED"
├── plan: "ONE_WEEK" | "TWO_WEEKS" | "ONE_MONTH" | "THREE_MONTHS"
├── impressions: number
└── clicks: number
```

## 🔐 Security Rules

See `firestore.rules` for Firestore security configuration.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

For support, email support@boattaxie.com or open an issue on GitHub.

---

Built with ❤️ for boaters and taxi riders everywhere!
