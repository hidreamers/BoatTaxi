package com.boattaxie.app.data.repository;

import android.content.Context;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J\u0018\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0086@\u00a2\u0006\u0002\u0010\u0016J \u0010\u0017\u001a\u0004\u0018\u00010\u00182\u0006\u0010\u0019\u001a\u00020\u00102\u0006\u0010\u001a\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u000e\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0015J(\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020!0 2\u0006\u0010\"\u001a\u00020\u00152\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u0010H\u0086@\u00a2\u0006\u0002\u0010$R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\n\u00a8\u0006%"}, d2 = {"Lcom/boattaxie/app/data/repository/PlacesRepository;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "isInitialized", "", "placesClient", "Lcom/google/android/libraries/places/api/net/PlacesClient;", "getPlacesClient", "()Lcom/google/android/libraries/places/api/net/PlacesClient;", "placesClient$delegate", "Lkotlin/Lazy;", "calculateDistance", "", "from", "Lcom/google/android/gms/maps/model/LatLng;", "to", "getPlaceDetails", "Lcom/boattaxie/app/data/repository/PlaceDetails;", "placeId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRoute", "Lcom/boattaxie/app/data/repository/RouteResult;", "origin", "destination", "(Lcom/google/android/gms/maps/model/LatLng;Lcom/google/android/gms/maps/model/LatLng;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "initialize", "", "apiKey", "searchPlaces", "", "Lcom/boattaxie/app/data/repository/PlaceResult;", "query", "currentLocation", "(Ljava/lang/String;Lcom/google/android/gms/maps/model/LatLng;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class PlacesRepository {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy placesClient$delegate = null;
    private boolean isInitialized = false;
    
    @javax.inject.Inject()
    public PlacesRepository(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final com.google.android.libraries.places.api.net.PlacesClient getPlacesClient() {
        return null;
    }
    
    public final void initialize(@org.jetbrains.annotations.NotNull()
    java.lang.String apiKey) {
    }
    
    /**
     * Search for places by query text
     * Biased towards Panama location - includes islands, restaurants, hotels, homes, businesses
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object searchPlaces(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.Nullable()
    com.google.android.gms.maps.model.LatLng currentLocation, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.boattaxie.app.data.repository.PlaceResult>> $completion) {
        return null;
    }
    
    /**
     * Get place details including coordinates
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getPlaceDetails(@org.jetbrains.annotations.NotNull()
    java.lang.String placeId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.repository.PlaceDetails> $completion) {
        return null;
    }
    
    /**
     * Calculate route between two points using Directions API
     * Returns distance in miles and duration in minutes
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getRoute(@org.jetbrains.annotations.NotNull()
    com.google.android.gms.maps.model.LatLng origin, @org.jetbrains.annotations.NotNull()
    com.google.android.gms.maps.model.LatLng destination, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.boattaxie.app.data.repository.RouteResult> $completion) {
        return null;
    }
    
    private final double calculateDistance(com.google.android.gms.maps.model.LatLng from, com.google.android.gms.maps.model.LatLng to) {
        return 0.0;
    }
}