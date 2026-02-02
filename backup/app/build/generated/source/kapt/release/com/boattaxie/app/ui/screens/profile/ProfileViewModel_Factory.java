package com.boattaxie.app.ui.screens.profile;

import android.content.Context;
import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.BookingRepository;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<BookingRepository> bookingRepositoryProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<Context> contextProvider;

  public ProfileViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<BookingRepository> bookingRepositoryProvider,
      Provider<FirebaseStorage> storageProvider, Provider<Context> contextProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.bookingRepositoryProvider = bookingRepositoryProvider;
    this.storageProvider = storageProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(authRepositoryProvider.get(), bookingRepositoryProvider.get(), storageProvider.get(), contextProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<BookingRepository> bookingRepositoryProvider,
      Provider<FirebaseStorage> storageProvider, Provider<Context> contextProvider) {
    return new ProfileViewModel_Factory(authRepositoryProvider, bookingRepositoryProvider, storageProvider, contextProvider);
  }

  public static ProfileViewModel newInstance(AuthRepository authRepository,
      BookingRepository bookingRepository, FirebaseStorage storage, Context context) {
    return new ProfileViewModel(authRepository, bookingRepository, storage, context);
  }
}
