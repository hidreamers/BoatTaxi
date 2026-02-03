package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/boattaxie/app/data/model/UserType;", "", "(Ljava/lang/String;I)V", "RIDER", "CAPTAIN", "DRIVER", "app_debug"})
public enum UserType {
    @com.google.firebase.firestore.PropertyName(value = "rider")
    /*public static final*/ RIDER /* = new RIDER() */,
    @com.google.firebase.firestore.PropertyName(value = "captain")
    /*public static final*/ CAPTAIN /* = new CAPTAIN() */,
    @com.google.firebase.firestore.PropertyName(value = "driver")
    /*public static final*/ DRIVER /* = new DRIVER() */;
    
    UserType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.UserType> getEntries() {
        return null;
    }
}