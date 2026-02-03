package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\t\u00a8\u0006\n"}, d2 = {"Lcom/boattaxie/app/data/model/BookingStatus;", "", "(Ljava/lang/String;I)V", "PENDING", "ACCEPTED", "ARRIVED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "NO_DRIVERS", "app_debug"})
public enum BookingStatus {
    @com.google.firebase.firestore.PropertyName(value = "pending")
    /*public static final*/ PENDING /* = new PENDING() */,
    @com.google.firebase.firestore.PropertyName(value = "accepted")
    /*public static final*/ ACCEPTED /* = new ACCEPTED() */,
    @com.google.firebase.firestore.PropertyName(value = "arrived")
    /*public static final*/ ARRIVED /* = new ARRIVED() */,
    @com.google.firebase.firestore.PropertyName(value = "in_progress")
    /*public static final*/ IN_PROGRESS /* = new IN_PROGRESS() */,
    @com.google.firebase.firestore.PropertyName(value = "completed")
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    @com.google.firebase.firestore.PropertyName(value = "cancelled")
    /*public static final*/ CANCELLED /* = new CANCELLED() */,
    @com.google.firebase.firestore.PropertyName(value = "no_drivers")
    /*public static final*/ NO_DRIVERS /* = new NO_DRIVERS() */;
    
    BookingStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.BookingStatus> getEntries() {
        return null;
    }
}