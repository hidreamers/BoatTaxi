package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Required documents based on vehicle type
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0005J\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\r\u001a\u00020\u000eR\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0007\u00a8\u0006\u0011"}, d2 = {"Lcom/boattaxie/app/data/model/VerificationRequirements;", "", "()V", "boatRequirements", "", "Lcom/boattaxie/app/data/model/DocumentType;", "getBoatRequirements", "()Ljava/util/List;", "taxiRequirements", "getTaxiRequirements", "getDocumentDescription", "", "documentType", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "getDocumentTitle", "getRequirements", "app_release"})
public final class VerificationRequirements {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.boattaxie.app.data.model.DocumentType> boatRequirements = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.boattaxie.app.data.model.DocumentType> taxiRequirements = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.boattaxie.app.data.model.VerificationRequirements INSTANCE = null;
    
    private VerificationRequirements() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.DocumentType> getBoatRequirements() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.DocumentType> getTaxiRequirements() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.DocumentType> getRequirements(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDocumentTitle(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.DocumentType documentType) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDocumentDescription(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.DocumentType documentType, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType) {
        return null;
    }
}