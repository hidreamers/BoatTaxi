package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\t\u00a8\u0006\n"}, d2 = {"Lcom/boattaxie/app/data/model/BoatType;", "", "(Ljava/lang/String;I)V", "SPEEDBOAT", "PONTOON", "YACHT", "FISHING_BOAT", "SAILBOAT", "FERRY", "WATER_TAXI", "app_release"})
public enum BoatType {
    @com.google.firebase.firestore.PropertyName(value = "speedboat")
    /*public static final*/ SPEEDBOAT /* = new SPEEDBOAT() */,
    @com.google.firebase.firestore.PropertyName(value = "pontoon")
    /*public static final*/ PONTOON /* = new PONTOON() */,
    @com.google.firebase.firestore.PropertyName(value = "yacht")
    /*public static final*/ YACHT /* = new YACHT() */,
    @com.google.firebase.firestore.PropertyName(value = "fishing_boat")
    /*public static final*/ FISHING_BOAT /* = new FISHING_BOAT() */,
    @com.google.firebase.firestore.PropertyName(value = "sailboat")
    /*public static final*/ SAILBOAT /* = new SAILBOAT() */,
    @com.google.firebase.firestore.PropertyName(value = "ferry")
    /*public static final*/ FERRY /* = new FERRY() */,
    @com.google.firebase.firestore.PropertyName(value = "water_taxi")
    /*public static final*/ WATER_TAXI /* = new WATER_TAXI() */;
    
    BoatType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.BoatType> getEntries() {
        return null;
    }
}