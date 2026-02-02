package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/boattaxie/app/data/model/VehicleType;", "", "(Ljava/lang/String;I)V", "BOAT", "TAXI", "app_release"})
public enum VehicleType {
    @com.google.firebase.firestore.PropertyName(value = "boat")
    /*public static final*/ BOAT /* = new BOAT() */,
    @com.google.firebase.firestore.PropertyName(value = "taxi")
    /*public static final*/ TAXI /* = new TAXI() */;
    
    VehicleType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.VehicleType> getEntries() {
        return null;
    }
}