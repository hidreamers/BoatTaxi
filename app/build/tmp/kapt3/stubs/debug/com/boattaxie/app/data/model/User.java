package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * User model representing app users (riders and drivers/captains)
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\bO\b\u0086\b\u0018\u00002\u00020\u0001B\u00b3\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\r\u0012\b\b\u0002\u0010\u000f\u001a\u00020\r\u0012\b\b\u0002\u0010\u0010\u001a\u00020\r\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0017\u001a\u00020\r\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0019\u0012\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u001b\u001a\u00020\r\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u001d\u0012\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010 \u001a\u0004\u0018\u00010!\u0012\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010&\u001a\u00020!\u0012\b\b\u0002\u0010\'\u001a\u00020!\u00a2\u0006\u0002\u0010(J\t\u0010O\u001a\u00020\u0003H\u00c6\u0003J\t\u0010P\u001a\u00020\rH\u00c6\u0003J\t\u0010Q\u001a\u00020\rH\u00c6\u0003J\t\u0010R\u001a\u00020\u0012H\u00c6\u0003J\t\u0010S\u001a\u00020\u0014H\u00c6\u0003J\t\u0010T\u001a\u00020\u0016H\u00c6\u0003J\t\u0010U\u001a\u00020\rH\u00c6\u0003J\u000b\u0010V\u001a\u0004\u0018\u00010\u0019H\u00c6\u0003J\u000b\u0010W\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010X\u001a\u00020\rH\u00c6\u0003J\t\u0010Y\u001a\u00020\u001dH\u00c6\u0003J\t\u0010Z\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010[\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\\\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010]\u001a\u0004\u0018\u00010!H\u00c6\u0003J\u000b\u0010^\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010_\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010`\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010a\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010b\u001a\u00020!H\u00c6\u0003J\t\u0010c\u001a\u00020!H\u00c6\u0003J\t\u0010d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010e\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010g\u001a\u00020\tH\u00c6\u0003J\u000b\u0010h\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\t\u0010i\u001a\u00020\rH\u00c6\u0003J\t\u0010j\u001a\u00020\rH\u00c6\u0003J\u00b7\u0002\u0010k\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\b\u001a\u00020\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\r2\b\b\u0002\u0010\u000f\u001a\u00020\r2\b\b\u0002\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u0017\u001a\u00020\r2\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00192\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u001b\u001a\u00020\r2\b\b\u0002\u0010\u001c\u001a\u00020\u001d2\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010 \u001a\u0004\u0018\u00010!2\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010&\u001a\u00020!2\b\b\u0002\u0010\'\u001a\u00020!H\u00c6\u0001J\u0013\u0010l\u001a\u00020\r2\b\u0010m\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010n\u001a\u00020\u0016H\u00d6\u0001J\t\u0010o\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0011\u0010&\u001a\u00020!\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0013\u0010\u0018\u001a\u0004\u0018\u00010\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0013\u0010\u001a\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00100R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00100R\u0011\u0010\u000e\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010*R\u0011\u0010\u000f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010*R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u00100R\u0011\u0010\u001b\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010*R\u001e\u0010\u0017\u001a\u00020\r8\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010*\"\u0004\b6\u00107R\u001e\u0010\u0010\u001a\u00020\r8\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010*\"\u0004\b8\u00107R\u0013\u0010 \u001a\u0004\u0018\u00010!\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010,R\u0013\u0010\u001e\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u00100R\u0013\u0010\u001f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u00100R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u00100R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u00100R\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010?R\u0011\u0010\u001c\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010AR\u0011\u0010\u0015\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010CR\u0011\u0010\'\u001a\u00020!\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010,R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010FR\u0013\u0010$\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u00100R\u0013\u0010#\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bH\u00100R\u0013\u0010%\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u00100R\u0013\u0010\"\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u00100R\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u0010LR\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\bM\u0010N\u00a8\u0006p"}, d2 = {"Lcom/boattaxie/app/data/model/User;", "", "id", "", "email", "fullName", "phoneNumber", "profilePhotoUrl", "userType", "Lcom/boattaxie/app/data/model/UserType;", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "canBeDriver", "", "hasBoat", "hasTaxi", "isVerified", "verificationStatus", "Lcom/boattaxie/app/data/model/VerificationStatus;", "rating", "", "totalTrips", "", "isOnline", "currentLocation", "Lcom/boattaxie/app/data/model/GeoLocation;", "fcmToken", "isLocalResident", "residencyType", "Lcom/boattaxie/app/data/model/ResidencyType;", "licenseNumber", "licenseType", "licenseExpiry", "Lcom/google/firebase/Timestamp;", "vehiclePlate", "vehicleModel", "vehicleColor", "vehiclePhoto", "createdAt", "updatedAt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/data/model/UserType;Lcom/boattaxie/app/data/model/VehicleType;ZZZZLcom/boattaxie/app/data/model/VerificationStatus;FIZLcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;ZLcom/boattaxie/app/data/model/ResidencyType;Ljava/lang/String;Ljava/lang/String;Lcom/google/firebase/Timestamp;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;)V", "getCanBeDriver", "()Z", "getCreatedAt", "()Lcom/google/firebase/Timestamp;", "getCurrentLocation", "()Lcom/boattaxie/app/data/model/GeoLocation;", "getEmail", "()Ljava/lang/String;", "getFcmToken", "getFullName", "getHasBoat", "getHasTaxi", "getId", "setOnline", "(Z)V", "setVerified", "getLicenseExpiry", "getLicenseNumber", "getLicenseType", "getPhoneNumber", "getProfilePhotoUrl", "getRating", "()F", "getResidencyType", "()Lcom/boattaxie/app/data/model/ResidencyType;", "getTotalTrips", "()I", "getUpdatedAt", "getUserType", "()Lcom/boattaxie/app/data/model/UserType;", "getVehicleColor", "getVehicleModel", "getVehiclePhoto", "getVehiclePlate", "getVehicleType", "()Lcom/boattaxie/app/data/model/VehicleType;", "getVerificationStatus", "()Lcom/boattaxie/app/data/model/VerificationStatus;", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class User {
    @com.google.firebase.firestore.DocumentId()
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String email = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fullName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String phoneNumber = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String profilePhotoUrl = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.UserType userType = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.VehicleType vehicleType = null;
    private final boolean canBeDriver = false;
    private final boolean hasBoat = false;
    private final boolean hasTaxi = false;
    private boolean isVerified;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.VerificationStatus verificationStatus = null;
    private final float rating = 0.0F;
    private final int totalTrips = 0;
    private boolean isOnline;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.GeoLocation currentLocation = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String fcmToken = null;
    private final boolean isLocalResident = false;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.ResidencyType residencyType = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String licenseNumber = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String licenseType = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp licenseExpiry = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehiclePlate = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehicleModel = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehicleColor = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehiclePhoto = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.Timestamp createdAt = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.Timestamp updatedAt = null;
    
    public User(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String fullName, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String profilePhotoUrl, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.UserType userType, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.VehicleType vehicleType, boolean canBeDriver, boolean hasBoat, boolean hasTaxi, boolean isVerified, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VerificationStatus verificationStatus, float rating, int totalTrips, boolean isOnline, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.GeoLocation currentLocation, @org.jetbrains.annotations.Nullable()
    java.lang.String fcmToken, boolean isLocalResident, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.ResidencyType residencyType, @org.jetbrains.annotations.Nullable()
    java.lang.String licenseNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String licenseType, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp licenseExpiry, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePlate, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleModel, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleColor, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePhoto, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp createdAt, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp updatedAt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFullName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPhoneNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getProfilePhotoUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.UserType getUserType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.VehicleType getVehicleType() {
        return null;
    }
    
    public final boolean getCanBeDriver() {
        return false;
    }
    
    public final boolean getHasBoat() {
        return false;
    }
    
    public final boolean getHasTaxi() {
        return false;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "verified")
    public final boolean isVerified() {
        return false;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "verified")
    public final void setVerified(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VerificationStatus getVerificationStatus() {
        return null;
    }
    
    public final float getRating() {
        return 0.0F;
    }
    
    public final int getTotalTrips() {
        return 0;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "isOnline")
    public final boolean isOnline() {
        return false;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "isOnline")
    public final void setOnline(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.GeoLocation getCurrentLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFcmToken() {
        return null;
    }
    
    public final boolean isLocalResident() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.ResidencyType getResidencyType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLicenseNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLicenseType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getLicenseExpiry() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehiclePlate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehicleModel() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehicleColor() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehiclePhoto() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp getCreatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp getUpdatedAt() {
        return null;
    }
    
    public User() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final boolean component10() {
        return false;
    }
    
    public final boolean component11() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VerificationStatus component12() {
        return null;
    }
    
    public final float component13() {
        return 0.0F;
    }
    
    public final int component14() {
        return 0;
    }
    
    public final boolean component15() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.GeoLocation component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component17() {
        return null;
    }
    
    public final boolean component18() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.ResidencyType component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component20() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component21() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component22() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component23() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component24() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component25() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component26() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp component27() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp component28() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.UserType component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.VehicleType component7() {
        return null;
    }
    
    public final boolean component8() {
        return false;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.User copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String fullName, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String profilePhotoUrl, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.UserType userType, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.VehicleType vehicleType, boolean canBeDriver, boolean hasBoat, boolean hasTaxi, boolean isVerified, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VerificationStatus verificationStatus, float rating, int totalTrips, boolean isOnline, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.GeoLocation currentLocation, @org.jetbrains.annotations.Nullable()
    java.lang.String fcmToken, boolean isLocalResident, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.ResidencyType residencyType, @org.jetbrains.annotations.Nullable()
    java.lang.String licenseNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String licenseType, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp licenseExpiry, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePlate, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleModel, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleColor, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePhoto, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp createdAt, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp updatedAt) {
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