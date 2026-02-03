package com.boattaxie.app.ui.screens.admin;

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
import com.boattaxie.app.data.model.VerificationSubmission;
import com.boattaxie.app.data.model.VerificationStatus;
import com.boattaxie.app.data.model.VehicleType;
import com.boattaxie.app.data.model.DocumentType;
import com.boattaxie.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a \u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a*\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\r\u0010\u000e\u001a:\u0010\u000f\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u0016H\u0003\u001a\u0012\u0010\u0017\u001a\u00020\b2\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001a"}, d2 = {"AdminVerificationScreen", "", "onNavigateBack", "Lkotlin/Function0;", "viewModel", "Lcom/boattaxie/app/ui/screens/admin/AdminVerificationViewModel;", "StatItem", "label", "", "count", "", "color", "Landroidx/compose/ui/graphics/Color;", "StatItem-mxwnekA", "(Ljava/lang/String;IJ)V", "VerificationCard", "submission", "Lcom/boattaxie/app/data/model/VerificationSubmission;", "isProcessing", "", "onApprove", "onReject", "Lkotlin/Function1;", "formatTimestamp", "timestamp", "Lcom/google/firebase/Timestamp;", "app_debug"})
public final class AdminVerificationScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AdminVerificationScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.admin.AdminVerificationViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void VerificationCard(com.boattaxie.app.data.model.VerificationSubmission submission, boolean isProcessing, kotlin.jvm.functions.Function0<kotlin.Unit> onApprove, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onReject) {
    }
    
    private static final java.lang.String formatTimestamp(com.google.firebase.Timestamp timestamp) {
        return null;
    }
}