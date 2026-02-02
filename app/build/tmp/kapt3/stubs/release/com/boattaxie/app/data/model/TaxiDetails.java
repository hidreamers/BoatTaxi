package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Taxi-specific details
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0010\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0005H\u00c6\u0003J1\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00052\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0019"}, d2 = {"Lcom/boattaxie/app/data/model/TaxiDetails;", "", "taxiType", "Lcom/boattaxie/app/data/model/TaxiType;", "isWheelchairAccessible", "", "hasAirConditioning", "hasWifi", "(Lcom/boattaxie/app/data/model/TaxiType;ZZZ)V", "getHasAirConditioning", "()Z", "getHasWifi", "getTaxiType", "()Lcom/boattaxie/app/data/model/TaxiType;", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "", "app_release"})
public final class TaxiDetails {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.TaxiType taxiType = null;
    private final boolean isWheelchairAccessible = false;
    private final boolean hasAirConditioning = false;
    private final boolean hasWifi = false;
    
    public TaxiDetails(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.TaxiType taxiType, boolean isWheelchairAccessible, boolean hasAirConditioning, boolean hasWifi) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.TaxiType getTaxiType() {
        return null;
    }
    
    public final boolean isWheelchairAccessible() {
        return false;
    }
    
    public final boolean getHasAirConditioning() {
        return false;
    }
    
    public final boolean getHasWifi() {
        return false;
    }
    
    public TaxiDetails() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.TaxiType component1() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.TaxiDetails copy(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.TaxiType taxiType, boolean isWheelchairAccessible, boolean hasAirConditioning, boolean hasWifi) {
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