package com.boattaxie.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.Booking
import com.boattaxie.app.data.model.User
import com.boattaxie.app.data.repository.AuthRepository
import com.boattaxie.app.data.repository.BookingRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val user: User? = null,
    val tripHistory: List<Booking> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    
    // Settings
    val pushNotificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true,
    val locationSharingEnabled: Boolean = true,
    
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
        
        // Initialize with empty payment methods - real ones will come from payment provider
        _uiState.update {
            it.copy(paymentMethods = emptyList())
        }
    }
    
    fun updateProfile(
        fullName: String, 
        phone: String,
        licenseNumber: String? = null,
        vehiclePlate: String? = null,
        vehicleModel: String? = null,
        vehicleColor: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val currentUser = _uiState.value.user
            if (currentUser != null) {
                // Determine license type based on user type
                val licenseType = when (currentUser.userType) {
                    com.boattaxie.app.data.model.UserType.CAPTAIN -> "Boat Captain License"
                    com.boattaxie.app.data.model.UserType.DRIVER -> "Taxi Driver License"
                    else -> null
                }
                
                val updatedUser = currentUser.copy(
                    fullName = fullName,
                    phoneNumber = phone,
                    licenseNumber = licenseNumber ?: currentUser.licenseNumber,
                    licenseType = licenseType ?: currentUser.licenseType,
                    vehiclePlate = vehiclePlate ?: currentUser.vehiclePlate,
                    vehicleModel = vehicleModel ?: currentUser.vehicleModel,
                    vehicleColor = vehicleColor ?: currentUser.vehicleColor
                )
                val result = authRepository.updateUser(updatedUser)
                
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        }
                    }
                )
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "User not found"
                    )
                }
            }
        }
    }
    
    fun loadTripHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val trips = bookingRepository.getUserBookings()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tripHistory = trips
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
    
    fun addPaymentMethod(cardNumber: String, expiry: String, cvv: String) {
        // In real app, this would go through Stripe
        val newMethod = PaymentMethod(
            id = System.currentTimeMillis().toString(),
            lastFour = cardNumber.takeLast(4),
            brand = "Visa",
            expiryMonth = expiry.take(2),
            expiryYear = expiry.takeLast(2),
            isDefault = _uiState.value.paymentMethods.isEmpty()
        )
        
        _uiState.update {
            it.copy(paymentMethods = it.paymentMethods + newMethod)
        }
    }
    
    fun deletePaymentMethod(methodId: String) {
        _uiState.update {
            it.copy(paymentMethods = it.paymentMethods.filter { m -> m.id != methodId })
        }
    }
    
    fun togglePushNotifications() {
        _uiState.update {
            it.copy(pushNotificationsEnabled = !it.pushNotificationsEnabled)
        }
    }
    
    fun toggleEmailNotifications() {
        _uiState.update {
            it.copy(emailNotificationsEnabled = !it.emailNotificationsEnabled)
        }
    }
    
    fun toggleLocationSharing() {
        _uiState.update {
            it.copy(locationSharingEnabled = !it.locationSharingEnabled)
        }
    }
    
    fun signOut(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }
    
    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true) }
            
            try {
                val userId = _uiState.value.user?.id ?: run {
                    android.util.Log.e("ProfileVM", "uploadProfilePhoto: No user ID found")
                    _uiState.update { it.copy(isUploadingPhoto = false, errorMessage = "User not found") }
                    return@launch
                }
                
                android.util.Log.d("ProfileVM", "uploadProfilePhoto: Uploading to Firebase Storage for user $userId")
                
                // Upload to Firebase Storage
                val photoUrl = withContext(Dispatchers.IO) {
                    uploadImageToStorage(uri, "profile_photos/$userId.jpg")
                }
                
                if (photoUrl != null) {
                    android.util.Log.d("ProfileVM", "uploadProfilePhoto: Uploaded to $photoUrl")
                    
                    // Update user profile with Firebase Storage URL
                    val currentUser = _uiState.value.user
                    if (currentUser != null) {
                        val updatedUser = currentUser.copy(profilePhotoUrl = photoUrl)
                        android.util.Log.d("ProfileVM", "uploadProfilePhoto: Updating Firestore with photo URL")
                        val result = authRepository.updateUser(updatedUser)
                        
                        result.fold(
                            onSuccess = {
                                android.util.Log.d("ProfileVM", "uploadProfilePhoto: Success! Updating local state")
                                _uiState.update { state -> 
                                    state.copy(
                                        isUploadingPhoto = false,
                                        user = updatedUser
                                    ) 
                                }
                            },
                            onFailure = { error ->
                                android.util.Log.e("ProfileVM", "uploadProfilePhoto: Firestore update failed: ${error.message}")
                                _uiState.update {
                                    it.copy(
                                        isUploadingPhoto = false,
                                        errorMessage = "Failed to save photo: ${error.message}"
                                    )
                                }
                            }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage = "Failed to upload photo"
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "uploadProfilePhoto: Exception: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        errorMessage = "Failed to save photo: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.deleteAccount()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    onComplete()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to delete account"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Upload image to Firebase Storage and return the download URL
     */
    private suspend fun uploadImageToStorage(imageUri: Uri, path: String): String? {
        return try {
            android.util.Log.d("ProfileVM", "Uploading image to Firebase Storage: $path")
            val storageRef = storage.reference.child(path)
            
            // Upload the file
            storageRef.putFile(imageUri).await()
            
            // Get the download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            android.util.Log.d("ProfileVM", "Image uploaded successfully: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("ProfileVM", "Failed to upload image to Firebase Storage: ${e.message}", e)
            null
        }
    }
}
