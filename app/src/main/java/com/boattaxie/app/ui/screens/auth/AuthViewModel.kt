package com.boattaxie.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boattaxie.app.data.model.User
import com.boattaxie.app.data.model.UserType
import com.boattaxie.app.data.model.VehicleType
import com.boattaxie.app.data.model.ResidencyType
import com.boattaxie.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Loading : AuthState()
    object LoggedOut : AuthState()
    data class LoggedIn(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class AuthEvent {
    data class SignUp(
        val email: String,
        val password: String,
        val fullName: String,
        val phoneNumber: String,
        val userType: UserType,
        val vehicleType: VehicleType? = null
    ) : AuthEvent()
    
    data class Login(val email: String, val password: String) : AuthEvent()
    data class ResetPassword(val email: String) : AuthEvent()
    object Logout : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    // Form states
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()
    
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()
    
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    
    private val _selectedUserType = MutableStateFlow<UserType?>(null)
    val selectedUserType: StateFlow<UserType?> = _selectedUserType.asStateFlow()
    
    private val _selectedVehicleType = MutableStateFlow<VehicleType?>(null)
    val selectedVehicleType: StateFlow<VehicleType?> = _selectedVehicleType.asStateFlow()
    
    private val _selectedResidencyType = MutableStateFlow<ResidencyType>(ResidencyType.LOCAL)
    val selectedResidencyType: StateFlow<ResidencyType> = _selectedResidencyType.asStateFlow()
    
    // Vehicle ownership for drivers (multi-select)
    private val _hasBoat = MutableStateFlow(false)
    val hasBoat: StateFlow<Boolean> = _hasBoat.asStateFlow()
    
    private val _hasTaxi = MutableStateFlow(false)
    val hasTaxi: StateFlow<Boolean> = _hasTaxi.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { firebaseUser ->
                if (firebaseUser != null) {
                    val user = authRepository.getCurrentUser()
                    if (user != null) {
                        // Auto-fix userType if it doesn't match vehicleType
                        val correctedUser = autoFixUserType(user)
                        _authState.value = AuthState.LoggedIn(correctedUser)
                    } else {
                        _authState.value = AuthState.LoggedOut
                    }
                } else {
                    _authState.value = AuthState.LoggedOut
                }
            }
        }
    }
    
    /**
     * Auto-fix userType if it doesn't match vehicleType
     * TAXI vehicleType should have DRIVER userType
     * BOAT vehicleType should have CAPTAIN userType
     */
    private suspend fun autoFixUserType(user: User): User {
        val vehicleType = user.vehicleType ?: return user
        
        val expectedUserType = when (vehicleType) {
            VehicleType.TAXI -> UserType.DRIVER
            VehicleType.BOAT -> UserType.CAPTAIN
        }
        
        // If userType doesn't match expected, fix it
        if (user.userType != expectedUserType && user.userType != UserType.RIDER) {
            android.util.Log.d("AuthViewModel", "Auto-fixing userType from ${user.userType} to $expectedUserType for vehicle $vehicleType")
            val result = authRepository.updateUserType(expectedUserType, vehicleType)
            return result.getOrNull() ?: user
        }
        
        return user
    }
    
    fun updateEmail(value: String) {
        _email.value = value
    }
    
    fun updatePassword(value: String) {
        _password.value = value
    }
    
    fun updateConfirmPassword(value: String) {
        _confirmPassword.value = value
    }
    
    fun updateFullName(value: String) {
        _fullName.value = value
    }
    
    fun updatePhoneNumber(value: String) {
        _phoneNumber.value = value
    }
    
    fun updateSelectedUserType(userType: UserType) {
        _selectedUserType.value = userType
    }
    
    fun updateSelectedVehicleType(vehicleType: VehicleType) {
        _selectedVehicleType.value = vehicleType
        // Automatically set the correct userType based on vehicle type
        // TAXI drivers should be DRIVER, BOAT drivers should be CAPTAIN
        _selectedUserType.value = when (vehicleType) {
            VehicleType.TAXI -> UserType.DRIVER
            VehicleType.BOAT -> UserType.CAPTAIN
        }
        android.util.Log.d("AuthViewModel", "Vehicle type set to $vehicleType, userType set to ${_selectedUserType.value}")
    }
    
    fun updateSelectedResidencyType(residencyType: ResidencyType) {
        _selectedResidencyType.value = residencyType
    }
    
    /**
     * Set which vehicles the driver has (multi-select)
     */
    fun setVehicleSelections(hasBoat: Boolean, hasTaxi: Boolean) {
        _hasBoat.value = hasBoat
        _hasTaxi.value = hasTaxi
        
        // Set the primary vehicle type (boat takes priority if both)
        if (hasBoat) {
            _selectedVehicleType.value = VehicleType.BOAT
            _selectedUserType.value = UserType.CAPTAIN
        } else if (hasTaxi) {
            _selectedVehicleType.value = VehicleType.TAXI
            _selectedUserType.value = UserType.DRIVER
        }
        
        android.util.Log.d("AuthViewModel", "Vehicle selections: hasBoat=$hasBoat, hasTaxi=$hasTaxi")
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun setError(message: String) {
        _errorMessage.value = message
    }
    
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    fun signUp() {
        val emailValue = _email.value
        val passwordValue = _password.value
        val confirmPasswordValue = _confirmPassword.value
        val fullNameValue = _fullName.value
        val phoneNumberValue = _phoneNumber.value
        val userType = _selectedUserType.value ?: UserType.RIDER
        val vehicleType = _selectedVehicleType.value
        val residencyType = _selectedResidencyType.value
        val hasBoatValue = _hasBoat.value
        val hasTaxiValue = _hasTaxi.value
        
        android.util.Log.d("AuthViewModel", "signUp called - userType: $userType, vehicleType: $vehicleType, hasBoat: $hasBoatValue, hasTaxi: $hasTaxiValue, fullName: $fullNameValue")
        
        // Validation
        when {
            fullNameValue.isBlank() -> {
                _errorMessage.value = "Name is required"
                return
            }
            emailValue.isBlank() -> {
                _errorMessage.value = "Email is required"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> {
                _errorMessage.value = "Please enter a valid email"
                return
            }
            phoneNumberValue.isBlank() -> {
                _errorMessage.value = "Phone number is required"
                return
            }
            passwordValue.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                return
            }
            passwordValue != confirmPasswordValue -> {
                _errorMessage.value = "Passwords do not match"
                return
            }
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = authRepository.signUp(
                email = emailValue,
                password = passwordValue,
                fullName = fullNameValue,
                phoneNumber = phoneNumberValue,
                userType = userType,
                vehicleType = vehicleType,
                residencyType = residencyType,
                hasBoat = hasBoatValue,
                hasTaxi = hasTaxiValue
            )
            
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.LoggedIn(user)
                    clearFormFields()
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Sign up failed"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun login() {
        val emailValue = _email.value
        val passwordValue = _password.value
        val selectedType = _selectedUserType.value
        val selectedVehicle = _selectedVehicleType.value
        
        // Validation
        when {
            emailValue.isBlank() -> {
                _errorMessage.value = "Email is required"
                return
            }
            passwordValue.isBlank() -> {
                _errorMessage.value = "Password is required"
                return
            }
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = authRepository.signIn(emailValue, passwordValue)
            
            result.fold(
                onSuccess = { user ->
                    // If user selected a type on welcome screen, update their type
                    if (selectedType != null && selectedType != user.userType) {
                        android.util.Log.d("AuthVM", "Updating user type from ${user.userType} to $selectedType")
                        val updateResult = authRepository.updateUserType(selectedType, selectedVehicle)
                        updateResult.fold(
                            onSuccess = { updatedUser ->
                                _authState.value = AuthState.LoggedIn(updatedUser)
                            },
                            onFailure = {
                                // Use original user if update fails
                                _authState.value = AuthState.LoggedIn(user)
                            }
                        )
                    } else {
                        _authState.value = AuthState.LoggedIn(user)
                    }
                    clearFormFields()
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Login failed"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun resetPassword() {
        resetPassword(_email.value)
    }
    
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Email is required"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = authRepository.resetPassword(email)
            
            result.fold(
                onSuccess = {
                    _successMessage.value = "Password reset email sent"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to send reset email"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun signUp(email: String, password: String, fullName: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _errorMessage.value = null
            
            val result = authRepository.signUp(
                email = email,
                password = password,
                fullName = fullName,
                phoneNumber = phoneNumber,
                userType = UserType.RIDER, // Default, will be updated in UserTypeSelection
                vehicleType = null
            )
            
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.LoggedIn(user)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Sign up failed")
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.LoggedOut
            clearFormFields()
        }
    }
    
    private fun clearFormFields() {
        _email.value = ""
        _password.value = ""
        _confirmPassword.value = ""
        _fullName.value = ""
        _phoneNumber.value = ""
        _selectedUserType.value = null
        _selectedVehicleType.value = null
    }
}
