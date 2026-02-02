package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/boattaxie/app/data/model/VerificationStatus;", "", "(Ljava/lang/String;I)V", "NONE", "PENDING", "APPROVED", "REJECTED", "app_release"})
public enum VerificationStatus {
    @com.google.firebase.firestore.PropertyName(value = "none")
    /*public static final*/ NONE /* = new NONE() */,
    @com.google.firebase.firestore.PropertyName(value = "pending")
    /*public static final*/ PENDING /* = new PENDING() */,
    @com.google.firebase.firestore.PropertyName(value = "approved")
    /*public static final*/ APPROVED /* = new APPROVED() */,
    @com.google.firebase.firestore.PropertyName(value = "rejected")
    /*public static final*/ REJECTED /* = new REJECTED() */;
    
    VerificationStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.VerificationStatus> getEntries() {
        return null;
    }
}