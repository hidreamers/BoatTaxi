package com.boattaxie.app.ui.screens.booking;

import android.location.Geocoder;
import com.boattaxie.app.data.repository.PlacesRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class LocationSearchViewModel_Factory implements Factory<LocationSearchViewModel> {
  private final Provider<PlacesRepository> placesRepositoryProvider;

  private final Provider<FusedLocationProviderClient> fusedLocationClientProvider;

  private final Provider<Geocoder> geocoderProvider;

  public LocationSearchViewModel_Factory(Provider<PlacesRepository> placesRepositoryProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<Geocoder> geocoderProvider) {
    this.placesRepositoryProvider = placesRepositoryProvider;
    this.fusedLocationClientProvider = fusedLocationClientProvider;
    this.geocoderProvider = geocoderProvider;
  }

  @Override
  public LocationSearchViewModel get() {
    return newInstance(placesRepositoryProvider.get(), fusedLocationClientProvider.get(), geocoderProvider.get());
  }

  public static LocationSearchViewModel_Factory create(
      Provider<PlacesRepository> placesRepositoryProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<Geocoder> geocoderProvider) {
    return new LocationSearchViewModel_Factory(placesRepositoryProvider, fusedLocationClientProvider, geocoderProvider);
  }

  public static LocationSearchViewModel newInstance(PlacesRepository placesRepository,
      FusedLocationProviderClient fusedLocationClient, Geocoder geocoder) {
    return new LocationSearchViewModel(placesRepository, fusedLocationClient, geocoder);
  }
}
