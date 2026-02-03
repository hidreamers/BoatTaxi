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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0018\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BQ\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0007H\u00c6\u0003J\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\"\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\u000b\u0010#\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003J_\u0010%\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000eH\u00c6\u0001J\u0013\u0010&\u001a\u00020\'2\b\u0010(\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010)\u001a\u00020*H\u00d6\u0001J\t\u0010+\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0013\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u0013\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0015R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0015R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001c\u00a8\u0006,"}, d2 = {"Lcom/boattaxie/app/ui/screens/booking/SearchResult;", "", "id", "", "title", "subtitle", "type", "Lcom/boattaxie/app/ui/screens/booking/SearchResultType;", "icon", "location", "Lcom/google/android/gms/maps/model/LatLng;", "ad", "Lcom/boattaxie/app/data/model/Advertisement;", "driver", "Lcom/boattaxie/app/data/model/User;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/ui/screens/booking/SearchResultType;Ljava/lang/String;Lcom/google/android/gms/maps/model/LatLng;Lcom/boattaxie/app/data/model/Advertisement;Lcom/boattaxie/app/data/model/User;)V", "getAd", "()Lcom/boattaxie/app/data/model/Advertisement;", "getDriver", "()Lcom/boattaxie/app/data/model/User;", "getIcon", "()Ljava/lang/String;", "getId", "getLocation", "()Lcom/google/android/gms/maps/model/LatLng;", "getSubtitle", "getTitle", "getType", "()Lcom/boattaxie/app/ui/screens/booking/SearchResultType;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class SearchResult {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String subtitle = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.ui.screens.booking.SearchResultType type = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String icon = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.android.gms.maps.model.LatLng location = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.Advertisement ad = null;
    @org.jetbrains.annotations.Nullable()
    private final com.boattaxie.app.data.model.User driver = null;
    
    public SearchResult(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String subtitle, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.booking.SearchResultType type, @org.jetbrains.annotations.NotNull()
    java.lang.String icon, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng location, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Advertisement ad, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.User driver) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSubtitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.ui.screens.booking.SearchResultType getType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getIcon() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng getLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Advertisement getAd() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.User getDriver() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.ui.screens.booking.SearchResultType component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.android.gms.maps.model.LatLng component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.Advertisement component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.boattaxie.app.data.model.User component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.ui.screens.booking.SearchResult copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String subtitle, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.ui.screens.booking.SearchResultType type, @org.jetbrains.annotations.NotNull()
    java.lang.String icon, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng location, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.Advertisement ad, @org.jetbrains.annotations.Nullable()
    com.boattaxie.app.data.model.User driver) {
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