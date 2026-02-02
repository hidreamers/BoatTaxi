package com.boattaxie.app.ui.screens.booking;

import android.location.Geocoder;
import com.boattaxie.app.data.repository.AdvertisementRepository;
import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.BookingRepository;
import com.boattaxie.app.data.repository.SubscriptionRepository;
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
public final class BookingViewModel_Factory implements Factory<BookingViewModel> {
  private final Provider<BookingRepository> bookingRepositoryProvider;

  private final Provider<AdvertisementRepository> advertisementRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SubscriptionRepository> subscriptionRepositoryProvider;

  private final Provider<FusedLocationProviderClient> fusedLocationClientProvider;

  private final Provider<Geocoder> geocoderProvider;

  public BookingViewModel_Factory(Provider<BookingRepository> bookingRepositoryProvider,
      Provider<AdvertisementRepository> advertisementRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<SubscriptionRepository> subscriptionRepositoryProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<Geocoder> geocoderProvider) {
    this.bookingRepositoryProvider = bookingRepositoryProvider;
    this.advertisementRepositoryProvider = advertisementRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.subscriptionRepositoryProvider = subscriptionRepositoryProvider;
    this.fusedLocationClientProvider = fusedLocationClientProvider;
    this.geocoderProvider = geocoderProvider;
  }

  @Override
  public BookingViewModel get() {
    return newInstance(bookingRepositoryProvider.get(), advertisementRepositoryProvider.get(), authRepositoryProvider.get(), subscriptionRepositoryProvider.get(), fusedLocationClientProvider.get(), geocoderProvider.get());
  }

  public static BookingViewModel_Factory create(
      Provider<BookingRepository> bookingRepositoryProvider,
      Provider<AdvertisementRepository> advertisementRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<SubscriptionRepository> subscriptionRepositoryProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<Geocoder> geocoderProvider) {
    return new BookingViewModel_Factory(bookingRepositoryProvider, advertisementRepositoryProvider, authRepositoryProvider, subscriptionRepositoryProvider, fusedLocationClientProvider, geocoderProvider);
  }

  public static BookingViewModel newInstance(BookingRepository bookingRepository,
      AdvertisementRepository advertisementRepository, AuthRepository authRepository,
      SubscriptionRepository subscriptionRepository,
      FusedLocationProviderClient fusedLocationClient, Geocoder geocoder) {
    return new BookingViewModel(bookingRepository, advertisementRepository, authRepository, subscriptionRepository, fusedLocationClient, geocoder);
  }
}
