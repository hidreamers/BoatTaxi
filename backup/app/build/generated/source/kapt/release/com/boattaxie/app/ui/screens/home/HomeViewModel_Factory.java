package com.boattaxie.app.ui.screens.home;

import com.boattaxie.app.data.repository.AdvertisementRepository;
import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.BookingRepository;
import com.boattaxie.app.data.repository.SubscriptionRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SubscriptionRepository> subscriptionRepositoryProvider;

  private final Provider<BookingRepository> bookingRepositoryProvider;

  private final Provider<AdvertisementRepository> advertisementRepositoryProvider;

  public HomeViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<SubscriptionRepository> subscriptionRepositoryProvider,
      Provider<BookingRepository> bookingRepositoryProvider,
      Provider<AdvertisementRepository> advertisementRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.subscriptionRepositoryProvider = subscriptionRepositoryProvider;
    this.bookingRepositoryProvider = bookingRepositoryProvider;
    this.advertisementRepositoryProvider = advertisementRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(authRepositoryProvider.get(), subscriptionRepositoryProvider.get(), bookingRepositoryProvider.get(), advertisementRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<SubscriptionRepository> subscriptionRepositoryProvider,
      Provider<BookingRepository> bookingRepositoryProvider,
      Provider<AdvertisementRepository> advertisementRepositoryProvider) {
    return new HomeViewModel_Factory(authRepositoryProvider, subscriptionRepositoryProvider, bookingRepositoryProvider, advertisementRepositoryProvider);
  }

  public static HomeViewModel newInstance(AuthRepository authRepository,
      SubscriptionRepository subscriptionRepository, BookingRepository bookingRepository,
      AdvertisementRepository advertisementRepository) {
    return new HomeViewModel(authRepository, subscriptionRepository, bookingRepository, advertisementRepository);
  }
}
