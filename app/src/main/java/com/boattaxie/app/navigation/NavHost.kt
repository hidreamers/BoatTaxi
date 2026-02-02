package com.boattaxie.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.boattaxie.app.data.model.VehicleType
import com.boattaxie.app.ui.screens.auth.*
import com.boattaxie.app.ui.screens.home.*
import com.boattaxie.app.ui.screens.verification.*
import com.boattaxie.app.ui.screens.subscription.*
import com.boattaxie.app.ui.screens.ads.*
import com.boattaxie.app.ui.screens.profile.*
import com.boattaxie.app.ui.screens.booking.*
import com.boattaxie.app.ui.screens.driver.*

@Composable
fun BoatTaxieNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    // Shared AuthViewModel scoped to NavHost
    val authViewModel: AuthViewModel = hiltViewModel()
    
    // Shared DriverViewModel scoped to NavHost - survives mode switching
    val driverViewModel: DriverViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth screens
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDriverHome = {
                    navController.navigate(Screen.DriverHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToVerification = {
                    navController.navigate(Screen.VerificationStatus.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                viewModel = authViewModel
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToDriverHome = {
                    navController.navigate(Screen.DriverHome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToVerification = {
                    navController.navigate(Screen.VerificationStatus.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onNavigateBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToUserTypeSelection = { navController.navigate(Screen.UserTypeSelection.route) },
                onNavigateBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }
        
        composable(Screen.UserTypeSelection.route) {
            UserTypeSelectionScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToVehicleTypeSelection = {
                    navController.navigate(Screen.VehicleTypeSelection.route)
                },
                viewModel = authViewModel
            )
        }
        
        // Verification screens
        composable(Screen.VehicleTypeSelection.route) {
            VehicleTypeSelectionScreen(
                onNavigateToVerification = { vehicleType ->
                    navController.navigate(Screen.Verification.createRoute(vehicleType))
                },
                onNavigateBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        
        composable(
            route = Screen.Verification.route,
            arguments = listOf(navArgument(NavArgs.VEHICLE_TYPE) { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleType = backStackEntry.arguments?.getString(NavArgs.VEHICLE_TYPE) ?: "boat"
            VerificationScreen(
                vehicleType = vehicleType,
                onNavigateToVerificationStatus = {
                    navController.navigate(Screen.VerificationStatus.route) {
                        popUpTo(Screen.VehicleTypeSelection.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.VerificationStatus.route) {
            VerificationStatusScreen(
                onNavigateToDriverHome = {
                    navController.navigate(Screen.DriverHome.route) {
                        popUpTo(Screen.VerificationStatus.route) { inclusive = true }
                    }
                },
                onNavigateToVerification = { vehicleType ->
                    navController.navigate(Screen.Verification.createRoute(vehicleType))
                }
            )
        }
        
        // Main screens
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToBookRide = { vehicleType ->
                    navController.navigate(Screen.BookRide.createRoute(vehicleType))
                },
                onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) },
                onNavigateToAds = { navController.navigate(Screen.Advertisements.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToTripHistory = { navController.navigate(Screen.TripHistory.route) },
                onNavigateToDriverMode = {
                    navController.navigate(Screen.DriverHome.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToDriverVerification = {
                    navController.navigate(Screen.VerificationStatus.route)
                }
            )
        }
        
        composable(
            route = Screen.BookRide.route,
            arguments = listOf(
                navArgument(NavArgs.VEHICLE_TYPE) { type = NavType.StringType },
                navArgument("requestDriverId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("requestDriverName") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val vehicleTypeStr = backStackEntry.arguments?.getString(NavArgs.VEHICLE_TYPE) ?: "boat"
            val vehicleType = if (vehicleTypeStr == "taxi") VehicleType.TAXI else VehicleType.BOAT
            val requestDriverId = backStackEntry.arguments?.getString("requestDriverId")
            val requestDriverName = backStackEntry.arguments?.getString("requestDriverName")
            
            // Observe location selection results from LocationSearchScreen
            val savedStateHandle = backStackEntry.savedStateHandle
            val selectedAddress = savedStateHandle.get<String>("selectedAddress")
            val selectedPlaceId = savedStateHandle.get<String>("selectedPlaceId")
            val isPickupResult = savedStateHandle.get<Boolean>("isPickup")
            
            BookRideScreen(
                vehicleType = vehicleType,
                onBookingConfirmed = { bookingId ->
                    navController.navigate(Screen.RideTracking.createRoute(bookingId))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLocationSearch = { isPickup ->
                    navController.navigate(Screen.LocationSearch.createRoute(isPickup, vehicleTypeStr))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToDriverMode = {
                    navController.navigate(Screen.DriverHome.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSubscription = {
                    navController.navigate(Screen.SubscriptionPlans.route)
                },
                selectedAddress = selectedAddress,
                selectedPlaceId = selectedPlaceId,
                isPickupResult = isPickupResult,
                onClearLocationResult = {
                    savedStateHandle.remove<String>("selectedAddress")
                    savedStateHandle.remove<String>("selectedPlaceId")
                    savedStateHandle.remove<Boolean>("isPickup")
                },
                requestDriverId = requestDriverId,
                requestDriverName = requestDriverName
            )
        }
        
        composable(
            route = Screen.LocationSearch.route,
            arguments = listOf(
                navArgument(NavArgs.IS_PICKUP) { type = NavType.BoolType },
                navArgument(NavArgs.VEHICLE_TYPE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isPickup = backStackEntry.arguments?.getBoolean(NavArgs.IS_PICKUP) ?: false
            val vehicleType = backStackEntry.arguments?.getString(NavArgs.VEHICLE_TYPE) ?: "boat"
            LocationSearchScreen(
                isPickup = isPickup,
                onLocationSelected = { placeId, address ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedAddress", address)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedPlaceId", placeId)
                    navController.previousBackStackEntry?.savedStateHandle?.set("isPickup", isPickup)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() },
                onChooseOnMap = {
                    navController.navigate(Screen.MapPicker.createRoute(isPickup, vehicleType))
                }
            )
        }
        
        // Map Picker Screen - tap on map to select location
        composable(
            route = Screen.MapPicker.route,
            arguments = listOf(
                navArgument(NavArgs.IS_PICKUP) { type = NavType.BoolType },
                navArgument(NavArgs.VEHICLE_TYPE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isPickup = backStackEntry.arguments?.getBoolean(NavArgs.IS_PICKUP) ?: false
            MapPickerScreen(
                isPickup = isPickup,
                onLocationSelected = { placeId, address, lat, lng ->
                    // Go back two screens (MapPicker -> LocationSearch -> BookRide)
                    // Set the result on the BookRide screen's saved state
                    navController.getBackStackEntry(Screen.BookRide.route.replace("{vehicleType}", backStackEntry.arguments?.getString(NavArgs.VEHICLE_TYPE) ?: "boat"))?.savedStateHandle?.apply {
                        set("selectedAddress", address)
                        set("selectedPlaceId", placeId)
                        set("isPickup", isPickup)
                    }
                    // Pop back to BookRide screen
                    navController.popBackStack(Screen.LocationSearch.route.replace("{isPickup}", isPickup.toString()).replace("{vehicleType}", backStackEntry.arguments?.getString(NavArgs.VEHICLE_TYPE) ?: "boat"), true)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.RideTracking.route,
            arguments = listOf(navArgument(NavArgs.BOOKING_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString(NavArgs.BOOKING_ID) ?: ""
            RideTrackingScreen(
                bookingId = bookingId,
                onRideComplete = {
                    navController.navigate(Screen.RideComplete.createRoute(bookingId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onCancelRide = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToDriverMode = {
                    navController.navigate(Screen.DriverHome.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(
            route = Screen.RideComplete.route,
            arguments = listOf(navArgument(NavArgs.BOOKING_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString(NavArgs.BOOKING_ID) ?: ""
            RideCompleteScreen(
                bookingId = bookingId,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Driver/Captain screens
        composable(Screen.DriverHome.route) {
            DriverHomeScreen(
                onNavigateToActiveRide = { bookingId ->
                    navController.navigate(Screen.ActiveRide.createRoute(bookingId))
                },
                onNavigateToEarnings = { navController.navigate(Screen.DriverEarnings.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToRiderMode = {
                    navController.navigate(Screen.BookRide.createRoute("boat")) {
                        launchSingleTop = true
                    }
                },
                viewModel = driverViewModel
            )
        }
        
        composable(Screen.DriverEarnings.route) {
            DriverEarningsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ActiveRide.route,
            arguments = listOf(navArgument(NavArgs.BOOKING_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString(NavArgs.BOOKING_ID) ?: ""
            ActiveRideScreen(
                bookingId = bookingId,
                onRideComplete = {
                    navController.navigate(Screen.DriverHome.route) {
                        popUpTo(Screen.DriverHome.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.DriverHome.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToRiderMode = {
                    navController.navigate(Screen.BookRide.createRoute("boat")) {
                        launchSingleTop = true
                    }
                },
                viewModel = driverViewModel
            )
        }
        
        // Subscription screens
        composable(Screen.Subscription.route) {
            SubscriptionScreen(
                onNavigateToPlans = { navController.navigate(Screen.SubscriptionPlans.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.SubscriptionPlans.route) {
            SubscriptionPlansScreen(
                onNavigateToPayment = { planId ->
                    navController.navigate(Screen.Payment.createRoute(planId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument(NavArgs.PLAN_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString(NavArgs.PLAN_ID) ?: ""
            PaymentScreen(
                planId = planId,
                onPaymentSuccess = {
                    navController.navigate(Screen.PaymentSuccess.route) {
                        popUpTo(Screen.Subscription.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.PaymentSuccess.route) {
            PaymentSuccessScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Advertisement screens
        composable(Screen.Advertisements.route) {
            AdvertisementsScreen(
                onNavigateToAdDetails = { adId ->
                    navController.navigate(Screen.AdDetails.createRoute(adId))
                },
                onNavigateToCreateAd = { navController.navigate(Screen.CreateAd.route) },
                onNavigateToMyAds = { 
                    // Replace AdvertisementsScreen with MyAds to avoid back button loop
                    navController.navigate(Screen.MyAds.route) {
                        popUpTo(Screen.Advertisements.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.AdDetails.route,
            arguments = listOf(navArgument(NavArgs.AD_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val adId = backStackEntry.arguments?.getString(NavArgs.AD_ID) ?: ""
            AdDetailsScreen(
                adId = adId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.CreateAd.route) {
            CreateAdScreen(
                onAdCreated = {
                    navController.navigate(Screen.MyAds.route) {
                        popUpTo(Screen.Advertisements.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.MyAds.route) {
            MyAdsScreen(
                onNavigateToAdDetails = { adId ->
                    navController.navigate(Screen.AdDetails.createRoute(adId))
                },
                onNavigateToCreateAd = { navController.navigate(Screen.CreateAd.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Profile screens
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToTripHistory = { navController.navigate(Screen.TripHistory.route) },
                onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToAdminVerifications = { navController.navigate(Screen.AdminVerifications.route) },
                onSignOut = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TripHistory.route) {
            TripHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onRequestDriver = { driverId, driverName, vehicleType ->
                    // Navigate to booking screen with the requested driver pre-selected
                    navController.navigate(
                        Screen.BookRide.createRoute(
                            vehicleType = vehicleType.name.lowercase(),
                            requestDriverId = driverId,
                            requestDriverName = driverName
                        )
                    )
                }
            )
        }
        
        composable(Screen.PaymentMethods.route) {
            PaymentMethodsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Help.route) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Admin screens
        composable(Screen.AdminVerifications.route) {
            com.boattaxie.app.ui.screens.admin.AdminVerificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
