package com.boattaxie.app.ui.screens.auth;

import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.model.User;
import com.boattaxie.app.data.model.UserType;
import com.boattaxie.app.data.model.VehicleType;
import com.boattaxie.app.data.model.ResidencyType;
import com.boattaxie.app.data.repository.AuthRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u001c\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0017\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u00107\u001a\u0002082\u0006\u00109\u001a\u000208H\u0082@\u00a2\u0006\u0002\u0010:J\b\u0010;\u001a\u00020<H\u0002J\u0006\u0010=\u001a\u00020<J\b\u0010>\u001a\u00020<H\u0002J\u0006\u0010?\u001a\u00020<J\u0006\u0010@\u001a\u00020<J\u0006\u0010A\u001a\u00020<J\u0006\u0010B\u001a\u00020<J\u000e\u0010B\u001a\u00020<2\u0006\u0010 \u001a\u00020\tJ\u000e\u0010C\u001a\u00020<2\u0006\u0010D\u001a\u00020\tJ\u0016\u0010E\u001a\u00020<2\u0006\u0010&\u001a\u00020\u000e2\u0006\u0010(\u001a\u00020\u000eJ\u0006\u0010F\u001a\u00020<J&\u0010F\u001a\u00020<2\u0006\u0010 \u001a\u00020\t2\u0006\u0010+\u001a\u00020\t2\u0006\u0010$\u001a\u00020\t2\u0006\u0010-\u001a\u00020\tJ\u000e\u0010G\u001a\u00020<2\u0006\u0010H\u001a\u00020\tJ\u000e\u0010I\u001a\u00020<2\u0006\u0010H\u001a\u00020\tJ\u000e\u0010J\u001a\u00020<2\u0006\u0010H\u001a\u00020\tJ\u000e\u0010K\u001a\u00020<2\u0006\u0010H\u001a\u00020\tJ\u000e\u0010L\u001a\u00020<2\u0006\u0010H\u001a\u00020\tJ\u000e\u0010M\u001a\u00020<2\u0006\u0010N\u001a\u00020\u0014J\u000e\u0010O\u001a\u00020<2\u0006\u0010P\u001a\u00020\u0016J\u000e\u0010Q\u001a\u00020<2\u0006\u0010R\u001a\u00020\u0018R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00160\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00180\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0019\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00070\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\t0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001dR\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\t0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001dR\u0019\u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001dR\u0017\u0010$\u001a\b\u0012\u0004\u0012\u00020\t0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001dR\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\u000e0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001dR\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020\u000e0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001dR\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020\u000e0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001dR\u0017\u0010+\u001a\b\u0012\u0004\u0012\u00020\t0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001dR\u0017\u0010-\u001a\b\u0012\u0004\u0012\u00020\t0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u001dR\u0017\u0010/\u001a\b\u0012\u0004\u0012\u00020\u00140\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\u001dR\u0019\u00101\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00160\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\u001dR\u0019\u00103\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00180\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010\u001dR\u0019\u00105\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010\u001d\u00a8\u0006S"}, d2 = {"Lcom/boattaxie/app/ui/screens/auth/AuthViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/boattaxie/app/data/repository/AuthRepository;", "(Lcom/boattaxie/app/data/repository/AuthRepository;)V", "_authState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/auth/AuthState;", "_confirmPassword", "", "_email", "_errorMessage", "_fullName", "_hasBoat", "", "_hasTaxi", "_isLoading", "_password", "_phoneNumber", "_selectedResidencyType", "Lcom/boattaxie/app/data/model/ResidencyType;", "_selectedUserType", "Lcom/boattaxie/app/data/model/UserType;", "_selectedVehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "_successMessage", "authState", "Lkotlinx/coroutines/flow/StateFlow;", "getAuthState", "()Lkotlinx/coroutines/flow/StateFlow;", "confirmPassword", "getConfirmPassword", "email", "getEmail", "errorMessage", "getErrorMessage", "fullName", "getFullName", "hasBoat", "getHasBoat", "hasTaxi", "getHasTaxi", "isLoading", "password", "getPassword", "phoneNumber", "getPhoneNumber", "selectedResidencyType", "getSelectedResidencyType", "selectedUserType", "getSelectedUserType", "selectedVehicleType", "getSelectedVehicleType", "successMessage", "getSuccessMessage", "autoFixUserType", "Lcom/boattaxie/app/data/model/User;", "user", "(Lcom/boattaxie/app/data/model/User;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "checkAuthState", "", "clearError", "clearFormFields", "clearSuccess", "login", "logout", "resetPassword", "setError", "message", "setVehicleSelections", "signUp", "updateConfirmPassword", "value", "updateEmail", "updateFullName", "updatePassword", "updatePhoneNumber", "updateSelectedResidencyType", "residencyType", "updateSelectedUserType", "userType", "updateSelectedVehicleType", "vehicleType", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AuthViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.auth.AuthState> _authState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.auth.AuthState> authState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _errorMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> errorMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _successMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> successMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _email = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> email = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _password = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> password = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _confirmPassword = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> confirmPassword = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _fullName = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> fullName = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _phoneNumber = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> phoneNumber = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.data.model.UserType> _selectedUserType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.data.model.UserType> selectedUserType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.data.model.VehicleType> _selectedVehicleType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.data.model.VehicleType> selectedVehicleType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.data.model.ResidencyType> _selectedResidencyType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.data.model.ResidencyType> selectedResidencyType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _hasBoat = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> hasBoat = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _hasTaxi = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> hasTaxi = null;
    
    @javax.inject.Inject()
    public AuthViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.auth.AuthState> getAuthState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getErrorMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSuccessMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getPassword() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getConfirmPassword() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getFullName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getPhoneNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.data.model.UserType> getSelectedUserType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.data.model.VehicleType> getSelectedVehicleType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.data.model.ResidencyType> getSelectedResidencyType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getHasBoat() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getHasTaxi() {
        return null;
    }
    
    private final void checkAuthState() {
    }
    
    /**
     * Auto-fix userType if it doesn't match vehicleType
     * TAXI vehicleType should have DRIVER userType
     * BOAT vehicleType should have CAPTAIN userType
     */
    private final java.lang.Object autoFixUserType(com.boattaxie.app.data.model.User user, kotlin.coroutines.Continuation<? super com.boattaxie.app.data.model.User> $completion) {
        return null;
    }
    
    public final void updateEmail(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void updatePassword(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void updateConfirmPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void updateFullName(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void updatePhoneNumber(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void updateSelectedUserType(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.UserType userType) {
    }
    
    public final void updateSelectedVehicleType(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType) {
    }
    
    public final void updateSelectedResidencyType(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.ResidencyType residencyType) {
    }
    
    /**
     * Set which vehicles the driver has (multi-select)
     */
    public final void setVehicleSelections(boolean hasBoat, boolean hasTaxi) {
    }
    
    public final void clearError() {
    }
    
    public final void setError(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    public final void clearSuccess() {
    }
    
    public final void signUp() {
    }
    
    public final void login() {
    }
    
    public final void resetPassword() {
    }
    
    public final void resetPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String email) {
    }
    
    public final void signUp(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String fullName, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber) {
    }
    
    public final void logout() {
    }
    
    private final void clearFormFields() {
    }
}