package com.boattaxie.app.ui.screens.verification;

import android.content.Intent;
import android.net.Uri;
import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.VerificationRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u001d\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001Bu\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\u0016\b\u0002\u0010\u0006\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0007\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u000e\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\u0002\u0010\u0013J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\t\u0010$\u001a\u00020\u0005H\u00c6\u0003J\u0017\u0010%\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0007H\u00c6\u0003J\u000b\u0010&\u001a\u0004\u0018\u00010\bH\u00c6\u0003J\t\u0010\'\u001a\u00020\fH\u00c6\u0003J\u000b\u0010(\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003J\u000b\u0010)\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010+\u001a\u0004\u0018\u00010\u0012H\u00c6\u0003Jy\u0010,\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u0016\b\u0002\u0010\u0006\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\t0\u00072\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\u000b\u001a\u00020\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u000e2\b\b\u0002\u0010\u0010\u001a\u00020\u00032\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u00c6\u0001J\u0013\u0010-\u001a\u00020\u00032\b\u0010.\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010/\u001a\u000200H\u00d6\u0001J\t\u00101\u001a\u00020\u000eH\u00d6\u0001R\u0011\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u001f\u0010\u0006\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0013\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0015R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001bR\u0013\u0010\n\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"\u00a8\u00062"}, d2 = {"Lcom/boattaxie/app/ui/screens/verification/VerificationUiState;", "", "isLoading", "", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "documents", "", "Lcom/boattaxie/app/data/model/DocumentType;", "Lcom/boattaxie/app/data/model/VerificationDocument;", "uploadingDocument", "verificationStatus", "Lcom/boattaxie/app/data/model/VerificationStatus;", "errorMessage", "", "successMessage", "canSubmit", "emailIntent", "Landroid/content/Intent;", "(ZLcom/boattaxie/app/data/model/VehicleType;Ljava/util/Map;Lcom/boattaxie/app/data/model/DocumentType;Lcom/boattaxie/app/data/model/VerificationStatus;Ljava/lang/String;Ljava/lang/String;ZLandroid/content/Intent;)V", "getCanSubmit", "()Z", "getDocuments", "()Ljava/util/Map;", "getEmailIntent", "()Landroid/content/Intent;", "getErrorMessage", "()Ljava/lang/String;", "getSuccessMessage", "getUploadingDocument", "()Lcom/boattaxie/app/data/model/DocumentType;", "getVehicleType", "()Lcom/boattaxie/app/data/model/VehicleType;", "getVerificationStatus", "()Lcom/boattaxie/app/data/model/VerificationStatus;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_release"})
public final class VerificationUiState {
    private final boolean isLoading = false;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.VehicleType vehicleType = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<com.boattaxie.app.data.model.DocumentType, com.boattaxie.app.data.model.VerificationDocument> documents = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.DocumentType uploadingDocument = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.VerificationStatus verificationStatus = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String successMessage = null;
    private final boolean canSubmit = false;
    @org.jetbrains.annotations.Nullable()
    private final android.content.Intent emailIntent = null;
    
    public VerificationUiState(boolean isLoading, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.boattaxie.app.data.model.DocumentType, com.boattaxie.app.data.model.VerificationDocument> documents, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.DocumentType uploadingDocument, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VerificationStatus verificationStatus, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, @org.jetbrains.annotations.Nullable()
    java.lang.String successMessage, boolean canSubmit, @org.jetbrains.annotations.Nullable()
    android.content.Intent emailIntent) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType getVehicleType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.boattaxie.app.data.model.DocumentType, com.boattaxie.app.data.model.VerificationDocument> getDocuments() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.DocumentType getUploadingDocument() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VerificationStatus getVerificationStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSuccessMessage() {
        return null;
    }
    
    public final boolean getCanSubmit() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.content.Intent getEmailIntent() {
        return null;
    }
    
    public VerificationUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.boattaxie.app.data.model.DocumentType, com.boattaxie.app.data.model.VerificationDocument> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.DocumentType component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VerificationStatus component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.content.Intent component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.ui.screens.verification.VerificationUiState copy(boolean isLoading, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.boattaxie.app.data.model.DocumentType, com.boattaxie.app.data.model.VerificationDocument> documents, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.DocumentType uploadingDocument, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VerificationStatus verificationStatus, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, @org.jetbrains.annotations.Nullable()
    java.lang.String successMessage, boolean canSubmit, @org.jetbrains.annotations.Nullable()
    android.content.Intent emailIntent) {
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