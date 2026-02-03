package com.boattaxie.app.data.repository;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.boattaxie.app.data.model.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.flow.Flow;
import java.io.File;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B)\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ0\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\f2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0014\u0010\u0015J$\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0017\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0018\u0010\u0019J\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001bH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001cH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001bH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0014\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u001b2\u0006\u0010\"\u001a\u00020#J\u0014\u0010$\u001a\b\u0012\u0004\u0012\u00020%0\u001bH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0016\u0010&\u001a\u00020\'2\u0006\u0010\"\u001a\u00020#H\u0086@\u00a2\u0006\u0002\u0010(J\u000e\u0010)\u001a\u00020\'H\u0086@\u00a2\u0006\u0002\u0010\u001dJ&\u0010*\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\f2\u0006\u0010+\u001a\u00020\f2\u0006\u0010\"\u001a\u00020#H\u0082@\u00a2\u0006\u0002\u0010,J\f\u0010-\u001a\b\u0012\u0004\u0012\u00020/0.J,\u00100\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\f2\u0006\u00101\u001a\u00020\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b2\u0010\u0015J$\u00103\u001a\u0002042\f\u00105\u001a\b\u0012\u0004\u0012\u00020%0\u001b2\u0006\u0010\"\u001a\u00020#2\u0006\u0010+\u001a\u00020\fJ>\u00106\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00102\u0006\u0010\"\u001a\u00020#2\f\u00105\u001a\b\u0012\u0004\u0012\u00020%0\u001b2\n\b\u0002\u00107\u001a\u0004\u0018\u00010\fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b8\u00109J4\u0010:\u001a\b\u0012\u0004\u0012\u00020%0\u00102\u0006\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b>\u0010?J&\u0010@\u001a\u00020\f2\u0006\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020!2\u0006\u0010A\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010BR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\u0004\u0018\u00010\f8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000e\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006C"}, d2 = {"Lcom/boattaxie/app/data/repository/VerificationRepository;", "", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "firestore", "Lcom/google/firebase/firestore/FirebaseFirestore;", "storage", "Lcom/google/firebase/storage/FirebaseStorage;", "context", "Landroid/content/Context;", "(Lcom/google/firebase/auth/FirebaseAuth;Lcom/google/firebase/firestore/FirebaseFirestore;Lcom/google/firebase/storage/FirebaseStorage;Landroid/content/Context;)V", "userId", "", "getUserId", "()Ljava/lang/String;", "approveSubmission", "Lkotlin/Result;", "", "submissionId", "adminNotes", "approveSubmission-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteDocument", "documentId", "deleteDocument-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllSubmissions", "", "Lcom/boattaxie/app/data/model/VerificationSubmission;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLatestSubmission", "getPendingSubmissions", "getRequiredDocuments", "Lcom/boattaxie/app/data/model/DocumentType;", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "getUserDocuments", "Lcom/boattaxie/app/data/model/VerificationDocument;", "hasAllRequiredDocuments", "", "(Lcom/boattaxie/app/data/model/VehicleType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isVerifiedLocalDriver", "notifyAdminNewVerification", "userName", "(Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/data/model/VehicleType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeVerificationStatus", "Lkotlinx/coroutines/flow/Flow;", "Lcom/boattaxie/app/data/model/VerificationStatus;", "rejectSubmission", "reason", "rejectSubmission-0E7RQCE", "sendVerificationEmail", "Landroid/content/Intent;", "documents", "submitVerification", "phoneNumber", "submitVerification-BWLJW6A", "(Lcom/boattaxie/app/data/model/VehicleType;Ljava/util/List;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadDocument", "imageUri", "Landroid/net/Uri;", "documentType", "uploadDocument-BWLJW6A", "(Landroid/net/Uri;Lcom/boattaxie/app/data/model/DocumentType;Lcom/boattaxie/app/data/model/VehicleType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadToFirebaseStorage", "uid", "(Landroid/net/Uri;Lcom/boattaxie/app/data/model/DocumentType;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class VerificationRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.firestore.FirebaseFirestore firestore = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.storage.FirebaseStorage storage = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    
    @javax.inject.Inject()
    public VerificationRepository(@org.jetbrains.annotations.NotNull()
    com.google.firebase.auth.FirebaseAuth auth, @org.jetbrains.annotations.NotNull()
    com.google.firebase.firestore.FirebaseFirestore firestore, @org.jetbrains.annotations.NotNull()
    com.google.firebase.storage.FirebaseStorage storage, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final java.lang.String getUserId() {
        return null;
    }
    
    /**
     * Upload image to Firebase Storage and return the download URL
     */
    private final java.lang.Object uploadToFirebaseStorage(android.net.Uri imageUri, com.boattaxie.app.data.model.DocumentType documentType, java.lang.String uid, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Send email with verification documents attached
     */
    @org.jetbrains.annotations.NotNull()
    public final android.content.Intent sendVerificationEmail(@org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.VerificationDocument> documents, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, @org.jetbrains.annotations.NotNull()
    java.lang.String userName) {
        return null;
    }
    
    /**
     * Send push notification to admin about new verification request
     */
    private final java.lang.Object notifyAdminNewVerification(java.lang.String submissionId, java.lang.String userName, com.boattaxie.app.data.model.VehicleType vehicleType, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Get all verification documents for current user
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getUserDocuments(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.VerificationDocument>> $completion) {
        return null;
    }
    
    /**
     * Get the latest verification submission for current user
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getLatestSubmission(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.model.VerificationSubmission> $completion) {
        return null;
    }
    
    /**
     * Observe verification status changes
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.boattaxie.app.data.model.VerificationStatus> observeVerificationStatus() {
        return null;
    }
    
    /**
     * Get required documents for a vehicle type
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.DocumentType> getRequiredDocuments(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType) {
        return null;
    }
    
    /**
     * Check if all required documents are uploaded
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object hasAllRequiredDocuments(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Check if current user is verified local driver (for free subscription)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object isVerifiedLocalDriver(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Get all pending verification submissions (Admin only)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getPendingSubmissions(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.VerificationSubmission>> $completion) {
        return null;
    }
    
    /**
     * Get all verification submissions (Admin only)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllSubmissions(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.model.VerificationSubmission>> $completion) {
        return null;
    }
}