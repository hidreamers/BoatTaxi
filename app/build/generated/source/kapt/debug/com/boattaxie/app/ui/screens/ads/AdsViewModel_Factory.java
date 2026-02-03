package com.boattaxie.app.ui.screens.ads;

import com.boattaxie.app.data.payment.PaymentManager;
import com.boattaxie.app.data.repository.AdvertisementRepository;
import com.boattaxie.app.data.repository.PlacesRepository;
import com.google.firebase.auth.FirebaseAuth;
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
public final class AdsViewModel_Factory implements Factory<AdsViewModel> {
  private final Provider<AdvertisementRepository> advertisementRepositoryProvider;

  private final Provider<PlacesRepository> placesRepositoryProvider;

  private final Provider<PaymentManager> paymentManagerProvider;

  private final Provider<FirebaseAuth> authProvider;

  public AdsViewModel_Factory(Provider<AdvertisementRepository> advertisementRepositoryProvider,
      Provider<PlacesRepository> placesRepositoryProvider,
      Provider<PaymentManager> paymentManagerProvider, Provider<FirebaseAuth> authProvider) {
    this.advertisementRepositoryProvider = advertisementRepositoryProvider;
    this.placesRepositoryProvider = placesRepositoryProvider;
    this.paymentManagerProvider = paymentManagerProvider;
    this.authProvider = authProvider;
  }

  @Override
  public AdsViewModel get() {
    return newInstance(advertisementRepositoryProvider.get(), placesRepositoryProvider.get(), paymentManagerProvider.get(), authProvider.get());
  }

  public static AdsViewModel_Factory create(
      Provider<AdvertisementRepository> advertisementRepositoryProvider,
      Provider<PlacesRepository> placesRepositoryProvider,
      Provider<PaymentManager> paymentManagerProvider, Provider<FirebaseAuth> authProvider) {
    return new AdsViewModel_Factory(advertisementRepositoryProvider, placesRepositoryProvider, paymentManagerProvider, authProvider);
  }

  public static AdsViewModel newInstance(AdvertisementRepository advertisementRepository,
      PlacesRepository placesRepository, PaymentManager paymentManager, FirebaseAuth auth) {
    return new AdsViewModel(advertisementRepository, placesRepository, paymentManager, auth);
  }
}
