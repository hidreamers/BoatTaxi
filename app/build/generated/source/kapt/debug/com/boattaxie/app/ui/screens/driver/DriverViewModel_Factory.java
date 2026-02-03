package com.boattaxie.app.ui.screens.driver;

import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.BookingRepository;
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
public final class DriverViewModel_Factory implements Factory<DriverViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<BookingRepository> bookingRepositoryProvider;

  private final Provider<FusedLocationProviderClient> fusedLocationClientProvider;

  public DriverViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<BookingRepository> bookingRepositoryProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.bookingRepositoryProvider = bookingRepositoryProvider;
    this.fusedLocationClientProvider = fusedLocationClientProvider;
  }

  @Override
  public DriverViewModel get() {
    return newInstance(authRepositoryProvider.get(), bookingRepositoryProvider.get(), fusedLocationClientProvider.get());
  }

  public static DriverViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<BookingRepository> bookingRepositoryProvider,
      Provider<FusedLocationProviderClient> fusedLocationClientProvider) {
    return new DriverViewModel_Factory(authRepositoryProvider, bookingRepositoryProvider, fusedLocationClientProvider);
  }

  public static DriverViewModel newInstance(AuthRepository authRepository,
      BookingRepository bookingRepository, FusedLocationProviderClient fusedLocationClient) {
    return new DriverViewModel(authRepository, bookingRepository, fusedLocationClient);
  }
}
