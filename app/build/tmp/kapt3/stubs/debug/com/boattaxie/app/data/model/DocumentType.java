package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\n\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\n\u00a8\u0006\u000b"}, d2 = {"Lcom/boattaxie/app/data/model/DocumentType;", "", "(Ljava/lang/String;I)V", "VEHICLE_PHOTO", "REGISTRATION", "LICENSE", "INSURANCE", "PERMIT", "SAFETY_CERTIFICATE", "COAST_GUARD_DOC", "TAXI_MEDALLION", "app_debug"})
public enum DocumentType {
    @com.google.firebase.firestore.PropertyName(value = "vehicle_photo")
    /*public static final*/ VEHICLE_PHOTO /* = new VEHICLE_PHOTO() */,
    @com.google.firebase.firestore.PropertyName(value = "registration")
    /*public static final*/ REGISTRATION /* = new REGISTRATION() */,
    @com.google.firebase.firestore.PropertyName(value = "license")
    /*public static final*/ LICENSE /* = new LICENSE() */,
    @com.google.firebase.firestore.PropertyName(value = "insurance")
    /*public static final*/ INSURANCE /* = new INSURANCE() */,
    @com.google.firebase.firestore.PropertyName(value = "permit")
    /*public static final*/ PERMIT /* = new PERMIT() */,
    @com.google.firebase.firestore.PropertyName(value = "safety_certificate")
    /*public static final*/ SAFETY_CERTIFICATE /* = new SAFETY_CERTIFICATE() */,
    @com.google.firebase.firestore.PropertyName(value = "coast_guard_doc")
    /*public static final*/ COAST_GUARD_DOC /* = new COAST_GUARD_DOC() */,
    @com.google.firebase.firestore.PropertyName(value = "taxi_medallion")
    /*public static final*/ TAXI_MEDALLION /* = new TAXI_MEDALLION() */;
    
    DocumentType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.data.model.DocumentType> getEntries() {
        return null;
    }
}