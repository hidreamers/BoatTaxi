package com.boattaxie.app.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    // Auth screens
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object UserTypeSelection : Screen("user_type_selection")
    
    // Verification screens
    object VehicleTypeSelection : Screen("vehicle_type_selection")
    object Verification : Screen("verification/{vehicleType}") {
        fun createRoute(vehicleType: String) = "verification/$vehicleType"
    }
    object VerificationStatus : Screen("verification_status")
    
    // Main screens
    object Home : Screen("home")
    object Map : Screen("map")
    object BookRide : Screen("book_ride/{vehicleType}?requestDriverId={requestDriverId}&requestDriverName={requestDriverName}") {
        fun createRoute(vehicleType: String, requestDriverId: String? = null, requestDriverName: String? = null): String {
            return if (requestDriverId != null && requestDriverName != null) {
                "book_ride/$vehicleType?requestDriverId=$requestDriverId&requestDriverName=$requestDriverName"
            } else {
                "book_ride/$vehicleType"
            }
        }
    }
    object RideTracking : Screen("ride_tracking/{bookingId}") {
        fun createRoute(bookingId: String) = "ride_tracking/$bookingId"
    }
    object RideComplete : Screen("ride_complete/{bookingId}") {
        fun createRoute(bookingId: String) = "ride_complete/$bookingId"
    }
    
    // Location search
    object LocationSearch : Screen("location_search/{isPickup}/{vehicleType}") {
        fun createRoute(isPickup: Boolean, vehicleType: String) = "location_search/$isPickup/$vehicleType"
    }
    
    // Map picker for tap-to-select location
    object MapPicker : Screen("map_picker/{isPickup}/{vehicleType}") {
        fun createRoute(isPickup: Boolean, vehicleType: String) = "map_picker/$isPickup/$vehicleType"
    }
    
    // Driver/Captain screens
    object DriverHome : Screen("driver_home")
    object DriverEarnings : Screen("driver_earnings")
    object RideRequest : Screen("ride_request/{bookingId}") {
        fun createRoute(bookingId: String) = "ride_request/$bookingId"
    }
    object ActiveRide : Screen("active_ride/{bookingId}") {
        fun createRoute(bookingId: String) = "active_ride/$bookingId"
    }
    
    // Subscription screens
    object Subscription : Screen("subscription")
    object SubscriptionPlans : Screen("subscription_plans")
    object Payment : Screen("payment/{planId}") {
        fun createRoute(planId: String) = "payment/$planId"
    }
    object PaymentSuccess : Screen("payment_success")
    
    // Advertisement screens
    object Advertisements : Screen("advertisements")
    object AdDetails : Screen("ad_details/{adId}") {
        fun createRoute(adId: String) = "ad_details/$adId"
    }
    object CreateAd : Screen("create_ad")
    object MyAds : Screen("my_ads")
    
    // Profile screens
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object TripHistory : Screen("trip_history")
    object PaymentMethods : Screen("payment_methods")
    object Settings : Screen("settings")
    object Help : Screen("help")
    object About : Screen("about")
    
    // Admin screens
    object AdminVerifications : Screen("admin_verifications")
}

/**
 * Navigation arguments
 */
object NavArgs {
    const val VEHICLE_TYPE = "vehicleType"
    const val BOOKING_ID = "bookingId"
    const val PLAN_ID = "planId"
    const val AD_ID = "adId"
    const val IS_PICKUP = "isPickup"
}
