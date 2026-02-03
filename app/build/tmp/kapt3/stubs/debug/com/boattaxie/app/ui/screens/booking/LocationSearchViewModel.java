package com.boattaxie.app.ui.screens.booking;

import android.location.Geocoder;
import androidx.lifecycle.ViewModel;
import com.boattaxie.app.data.repository.PlaceResult;
import com.boattaxie.app.data.repository.PlacesRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u0013\u001a\u00020\u0014J\b\u0010\u0015\u001a\u00020\u0014H\u0002J\b\u0010\u0016\u001a\u00020\u0014H\u0002J\u0016\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\u000e\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u0019J\u0006\u0010\u001b\u001a\u00020\u0014R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001c"}, d2 = {"Lcom/boattaxie/app/ui/screens/booking/LocationSearchViewModel;", "Landroidx/lifecycle/ViewModel;", "placesRepository", "Lcom/boattaxie/app/data/repository/PlacesRepository;", "fusedLocationClient", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "geocoder", "Landroid/location/Geocoder;", "(Lcom/boattaxie/app/data/repository/PlacesRepository;Lcom/google/android/gms/location/FusedLocationProviderClient;Landroid/location/Geocoder;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/boattaxie/app/ui/screens/booking/LocationSearchUiState;", "panamaPlaces", "", "Lcom/boattaxie/app/data/repository/PlaceResult;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearSearch", "", "getCurrentLocation", "loadRecentPlaces", "searchLocalPlaces", "query", "", "searchPlaces", "useCurrentLocation", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LocationSearchViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.repository.PlacesRepository placesRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = null;
    @org.jetbrains.annotations.NotNull()
    private final android.location.Geocoder geocoder = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.boattaxie.app.ui.screens.booking.LocationSearchUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.booking.LocationSearchUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.repository.PlaceResult> panamaPlaces = null;
    
    @javax.inject.Inject()
    public LocationSearchViewModel(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.repository.PlacesRepository placesRepository, @org.jetbrains.annotations.NotNull()
    com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient, @org.jetbrains.annotations.NotNull()
    android.location.Geocoder geocoder) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.boattaxie.app.ui.screens.booking.LocationSearchUiState> getUiState() {
        return null;
    }
    
    private final void getCurrentLocation() {
    }
    
    private final void loadRecentPlaces() {
    }
    
    public final void searchPlaces(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    private final java.util.List<com.boattaxie.app.data.repository.PlaceResult> searchLocalPlaces(java.lang.String query) {
        return null;
    }
    
    public final void useCurrentLocation() {
    }
    
    public final void clearSearch() {
    }
}