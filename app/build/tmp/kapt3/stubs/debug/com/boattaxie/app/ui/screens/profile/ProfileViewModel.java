package com.boattaxie.app.ui.screens.profile;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.model.Booking;
import com.boattaxie.app.data.model.User;
import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.BookingRepository;
import com.google.firebase.storage.FirebaseStorage;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B)\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u001e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00152\u0006\u0010\u0017\u001a\u00020\u0015J\u0014\u0010\u0018\u001a\u00020\u00132\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00130\u001aJ\u000e\u0010\u001b\u001a\u00020\u00132\u0006\u0010\u001c\u001a\u00020\u0015J\u0006\u0010\u001d\u001a\u00020\u0013J\b\u0010\u001e\u001a\u00020\u0013H\u0002J\u0016\u0010\u001f\u001a\u00020\u00132\u000e\b\u0002\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00130\u001aJ\u0006\u0010 \u001a\u00020\u0013J\u0006\u0010!\u001a\u00020\u0013J\u0006\u0010\"\u001a\u00020\u0013JF\u0010#\u001a\u00020\u00132\u0006\u0010$\u001a\u00020\u00152\u0006\u0010%\u001a\u00020\u00152\n\b\u0002\u0010&\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010\'\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010(\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010)\u001a\u0004\u0018\u00010\u0015J \u0010*\u001a\u0004\u0018\u00010\u00152\u0006\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020\u0015H\u0082@\u00a2\u0006\u0002\u0010.J\u000e\u0010/\u001a\u00020\u00132\u0006\u00100\u001a\u00020,R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u00061"}, d2 = {"Lcom/boattaxie/app/ui/screens/profile/ProfileViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/boattaxie/app/data/repository/AuthRepository;", "bookingRepository", "Lcom/boattaxie/app/data/repository/BookingRepository;", "storage", "Lcom/google/firebase/storage/FirebaseStorage;", "context", "Landroid/content/Context;", "(Lcom/boattaxie/app/data/repository/AuthRepository;Lcom/boattaxie/app/data/repository/BookingRepository;Lcom/google/firebase/storage/FirebaseStorage;Landroid/content/Context;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/profile/ProfileUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "addPaymentMethod", "", "cardNumber", "", "expiry", "cvv", "deleteAccount", "onComplete", "Lkotlin/Function0;", "deletePaymentMethod", "methodId", "loadTripHistory", "loadUserData", "signOut", "toggleEmailNotifications", "toggleLocationSharing", "togglePushNotifications", "updateProfile", "fullName", "phone", "licenseNumber", "vehiclePlate", "vehicleModel", "vehicleColor", "uploadImageToStorage", "imageUri", "Landroid/net/Uri;", "path", "(Landroid/net/Uri;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadProfilePhoto", "uri", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ProfileViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.BookingRepository bookingRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.storage.FirebaseStorage storage = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.profile.ProfileUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.profile.ProfileUiState> uiState = null;
    
    @javax.inject.Inject()
    public ProfileViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.BookingRepository bookingRepository, @org.jetbrains.annotations.NotNull()
    com.google.firebase.storage.FirebaseStorage storage, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.profile.ProfileUiState> getUiState() {
        return null;
    }
    
    private final void loadUserData() {
    }
    
    public final void updateProfile(@org.jetbrains.annotations.NotNull()
    java.lang.String fullName, @org.jetbrains.annotations.NotNull()
    java.lang.String phone, @org.jetbrains.annotations.Nullable()
    java.lang.String licenseNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePlate, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleModel, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleColor) {
    }
    
    public final void loadTripHistory() {
    }
    
    public final void addPaymentMethod(@org.jetbrains.annotations.NotNull()
    java.lang.String cardNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String expiry, @org.jetbrains.annotations.NotNull()
    java.lang.String cvv) {
    }
    
    public final void deletePaymentMethod(@org.jetbrains.annotations.NotNull()
    java.lang.String methodId) {
    }
    
    public final void togglePushNotifications() {
    }
    
    public final void toggleEmailNotifications() {
    }
    
    public final void toggleLocationSharing() {
    }
    
    public final void signOut(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onComplete) {
    }
    
    public final void uploadProfilePhoto(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    public final void deleteAccount(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onComplete) {
    }
    
    /**
     * Upload image to Firebase Storage and return the download URL
     */
    private final java.lang.Object uploadImageToStorage(android.net.Uri imageUri, java.lang.String path, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
}