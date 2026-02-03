package com.boattaxie.app.ui.screens.booking;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import android.content.Intent;
import android.net.Uri;
import androidx.compose.ui.text.style.TextDecoration;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.layout.ContentScale;
import androidx.core.content.ContextCompat;
import coil.compose.AsyncImagePainter;
import coil.request.ImageRequest;
import androidx.compose.ui.graphics.painter.Painter;
import com.boattaxie.app.R;
import com.boattaxie.app.data.model.*;
import com.boattaxie.app.ui.components.*;
import com.boattaxie.app.ui.theme.*;
import com.boattaxie.app.util.LanguageManager;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.io.File;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.compose.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/boattaxie/app/ui/screens/booking/SearchResultType;", "", "(Ljava/lang/String;I)V", "BUSINESS", "DRIVER", "LOCATION", "app_debug"})
public enum SearchResultType {
    /*public static final*/ BUSINESS /* = new BUSINESS() */,
    /*public static final*/ DRIVER /* = new DRIVER() */,
    /*public static final*/ LOCATION /* = new LOCATION() */;
    
    SearchResultType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.boattaxie.app.ui.screens.booking.SearchResultType> getEntries() {
        return null;
    }
}