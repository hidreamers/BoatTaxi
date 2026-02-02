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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u0010\u001a\u00020\u0011J\u0006\u0010\u0012\u001a\u00020\u0011J\u0006\u0010\u0013\u001a\u00020\u0011J\u000e\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u0016J\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00160\u0018J\b\u0010\u0019\u001a\u00020\u0011H\u0002J\b\u0010\u001a\u001a\u00020\u0011H\u0002J\u000e\u0010\u001b\u001a\u00020\u00112\u0006\u0010\u001c\u001a\u00020\u001dJ\u0006\u0010\u001e\u001a\u00020\u0011J\u0016\u0010\u001f\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010 \u001a\u00020!R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/boattaxie/app/ui/screens/verification/VerificationViewModel;", "Landroidx/lifecycle/ViewModel;", "verificationRepository", "Lcom/boattaxie/app/data/repository/VerificationRepository;", "authRepository", "Lcom/boattaxie/app/data/repository/AuthRepository;", "(Lcom/boattaxie/app/data/repository/VerificationRepository;Lcom/boattaxie/app/data/repository/AuthRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/verification/VerificationUiState;", "currentVehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearEmailIntent", "", "clearError", "clearSuccess", "deleteDocument", "documentType", "Lcom/boattaxie/app/data/model/DocumentType;", "getRequiredDocuments", "", "loadExistingDocuments", "observeVerificationStatus", "setVehicleType", "type", "", "submitVerification", "uploadDocument", "imageUri", "Landroid/net/Uri;", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class VerificationViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.VerificationRepository verificationRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.verification.VerificationUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.verification.VerificationUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private com.boattaxie.app.data.model.VehicleType currentVehicleType = com.boattaxie.app.data.model.VehicleType.BOAT;
    
    @javax.inject.Inject()
    public VerificationViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.VerificationRepository verificationRepository, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.verification.VerificationUiState> getUiState() {
        return null;
    }
    
    public final void setVehicleType(@org.jetbrains.annotations.NotNull()
    java.lang.String type) {
    }
    
    private final void observeVerificationStatus() {
    }
    
    private final void loadExistingDocuments() {
    }
    
    public final void uploadDocument(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.DocumentType documentType, @org.jetbrains.annotations.NotNull()
    android.net.Uri imageUri) {
    }
    
    public final void deleteDocument(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.DocumentType documentType) {
    }
    
    public final void submitVerification() {
    }
    
    public final void clearEmailIntent() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.DocumentType> getRequiredDocuments() {
        return null;
    }
    
    public final void clearError() {
    }
    
    public final void clearSuccess() {
    }
}