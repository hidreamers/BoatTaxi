package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0005H\u00c6\u0003J\'\u0010\u0010\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0018"}, d2 = {"Lcom/boattaxie/app/data/model/RoutePrice;", "", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "localPrice", "", "touristPrice", "(Lcom/boattaxie/app/data/model/VehicleType;DD)V", "getLocalPrice", "()D", "getTouristPrice", "getVehicleType", "()Lcom/boattaxie/app/data/model/VehicleType;", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
public final class RoutePrice {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.VehicleType vehicleType = null;
    private final double localPrice = 0.0;
    private final double touristPrice = 0.0;
    
    public RoutePrice(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, double localPrice, double touristPrice) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType getVehicleType() {
        return null;
    }
    
    public final double getLocalPrice() {
        return 0.0;
    }
    
    public final double getTouristPrice() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType component1() {
        return null;
    }
    
    public final double component2() {
        return 0.0;
    }
    
    public final double component3() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.RoutePrice copy(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, double localPrice, double touristPrice) {
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