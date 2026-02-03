package com.boattaxie.app.ui.screens.verification;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.boattaxie.app.data.model.VerificationStatus;
import com.boattaxie.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000 \n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u0016\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a\u0016\u0010\u0004\u001a\u00020\u00012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a\b\u0010\u0006\u001a\u00020\u0001H\u0003\u001a\u0016\u0010\u0007\u001a\u00020\u00012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a4\u0010\t\u001a\u00020\u00012\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0012\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00010\f2\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0007\u00a8\u0006\u0010"}, d2 = {"ApprovedVerificationContent", "", "onContinue", "Lkotlin/Function0;", "NoVerificationContent", "onStartVerification", "PendingVerificationContent", "RejectedVerificationContent", "onResubmit", "VerificationStatusScreen", "onNavigateToDriverHome", "onNavigateToVerification", "Lkotlin/Function1;", "", "viewModel", "Lcom/boattaxie/app/ui/screens/verification/VerificationViewModel;", "app_debug"})
public final class VerificationStatusScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void VerificationStatusScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToDriverHome, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToVerification, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.verification.VerificationViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void PendingVerificationContent() {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void RejectedVerificationContent(kotlin.jvm.functions.Function0<kotlin.Unit> onResubmit) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ApprovedVerificationContent(kotlin.jvm.functions.Function0<kotlin.Unit> onContinue) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void NoVerificationContent(kotlin.jvm.functions.Function0<kotlin.Unit> onStartVerification) {
    }
}