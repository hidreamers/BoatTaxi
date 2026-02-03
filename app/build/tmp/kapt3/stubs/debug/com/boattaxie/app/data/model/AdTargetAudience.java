package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/boattaxie/app/data/model/AdTargetAudience;", "", "(Ljava/lang/String;I)V", "ALL", "BOAT_RIDERS", "TAXI_RIDERS", "CAPTAINS", "DRIVERS", "app_debug"})
public enum AdTargetAudience {
    @com.google.firebase.firestore.PropertyName(value = "all")
    /*public static final*/ ALL /* = new ALL() */,
    @com.google.firebase.firestore.PropertyName(value = "boat_riders")
    /*public static final*/ BOAT_RIDERS /* = new BOAT_RIDERS() */,
    @com.google.firebase.firestore.PropertyName(value = "taxi_riders")
    /*public static final*/ TAXI_RIDERS /* = new TAXI_RIDERS() */,
    @com.google.firebase.firestore.PropertyName(value = "captains")
    /*public static final*/ CAPTAINS /* = new CAPTAINS() */,
    @com.google.firebase.firestore.PropertyName(value = "drivers")
    /*public static final*/ DRIVERS /* = new DRIVERS() */;
    
    AdTargetAudience() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.AdTargetAudience> getEntries() {
        return null;
    }
}