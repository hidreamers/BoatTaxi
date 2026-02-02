package com.boattaxie.app.ui.screens.verification;

import android.Manifest;
import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.core.content.FileProvider;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.ui.theme.*;
import java.io.File;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u001aF\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0003\u001a6\u0010\r\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0007\u00a8\u0006\u0013"}, d2 = {"DocumentUploadCard", "", "documentType", "Lcom/boattaxie/app/data/model/DocumentType;", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "document", "Lcom/boattaxie/app/data/model/VerificationDocument;", "isUploading", "", "onUploadClick", "Lkotlin/Function0;", "onDeleteClick", "VerificationScreen", "", "onNavigateToVerificationStatus", "onNavigateBack", "viewModel", "Lcom/boattaxie/app/ui/screens/verification/VerificationViewModel;", "app_release"})
public final class VerificationScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void VerificationScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String vehicleType, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToVerificationStatus, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.verification.VerificationViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void DocumentUploadCard(com.boattaxie.app.data.model.DocumentType documentType, com.boattaxie.app.data.model.VehicleType vehicleType, com.boattaxie.app.data.model.VerificationDocument document, boolean isUploading, kotlin.jvm.functions.Function0<kotlin.Unit> onUploadClick, kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteClick) {
    }
}