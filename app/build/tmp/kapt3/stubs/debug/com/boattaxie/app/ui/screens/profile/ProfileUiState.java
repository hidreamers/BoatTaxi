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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0019\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001Bo\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u0012\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\b\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\r\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\u0002\u0010\u0011J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000f\u0010 \u001a\b\u0012\u0004\u0012\u00020\t0\bH\u00c6\u0003J\u000f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u000b0\bH\u00c6\u0003J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010%\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003Js\u0010&\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\b2\b\b\u0002\u0010\f\u001a\u00020\u00032\b\b\u0002\u0010\r\u001a\u00020\u00032\b\b\u0002\u0010\u000e\u001a\u00020\u00032\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u00c6\u0001J\u0013\u0010\'\u001a\u00020\u00032\b\u0010(\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010)\u001a\u00020*H\u00d6\u0001J\t\u0010+\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\r\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0013R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u0013R\u0011\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0013R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0013R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0018R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001c\u00a8\u0006,"}, d2 = {"Lcom/boattaxie/app/ui/screens/profile/ProfileUiState;", "", "isLoading", "", "isUploadingPhoto", "user", "Lcom/boattaxie/app/data/model/User;", "tripHistory", "", "Lcom/boattaxie/app/data/model/Booking;", "paymentMethods", "Lcom/boattaxie/app/ui/screens/profile/PaymentMethod;", "pushNotificationsEnabled", "emailNotificationsEnabled", "locationSharingEnabled", "errorMessage", "", "(ZZLcom/boattaxie/app/data/model/User;Ljava/util/List;Ljava/util/List;ZZZLjava/lang/String;)V", "getEmailNotificationsEnabled", "()Z", "getErrorMessage", "()Ljava/lang/String;", "getLocationSharingEnabled", "getPaymentMethods", "()Ljava/util/List;", "getPushNotificationsEnabled", "getTripHistory", "getUser", "()Lcom/boattaxie/app/data/model/User;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class ProfileUiState {
    private final boolean isLoading = false;
    private final boolean isUploadingPhoto = false;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.User user = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.model.Booking> tripHistory = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.ui.screens.profile.PaymentMethod> paymentMethods = null;
    private final boolean pushNotificationsEnabled = false;
    private final boolean emailNotificationsEnabled = false;
    private final boolean locationSharingEnabled = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    
    public ProfileUiState(boolean isLoading, boolean isUploadingPhoto, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.User user, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Booking> tripHistory, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.ui.screens.profile.PaymentMethod> paymentMethods, boolean pushNotificationsEnabled, boolean emailNotificationsEnabled, boolean locationSharingEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    public final boolean isUploadingPhoto() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.User getUser() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Booking> getTripHistory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.ui.screens.profile.PaymentMethod> getPaymentMethods() {
        return null;
    }
    
    public final boolean getPushNotificationsEnabled() {
        return false;
    }
    
    public final boolean getEmailNotificationsEnabled() {
        return false;
    }
    
    public final boolean getLocationSharingEnabled() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public ProfileUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final boolean component2() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.User component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.Booking> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.ui.screens.profile.PaymentMethod> component5() {
        return null;
    }
    
    public final boolean component6() {
        return false;
    }
    
    public final boolean component7() {
        return false;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.ui.screens.profile.ProfileUiState copy(boolean isLoading, boolean isUploadingPhoto, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.User user, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.Booking> tripHistory, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.ui.screens.profile.PaymentMethod> paymentMethods, boolean pushNotificationsEnabled, boolean emailNotificationsEnabled, boolean locationSharingEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}