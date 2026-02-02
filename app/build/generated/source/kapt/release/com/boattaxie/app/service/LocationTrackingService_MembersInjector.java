package com.boattaxie.app.service;

import com.boattaxie.app.data.repository.AuthRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class LocationTrackingService_MembersInjector implements MembersInjector<LocationTrackingService> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public LocationTrackingService_MembersInjector(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  public static MembersInjector<LocationTrackingService> create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new LocationTrackingService_MembersInjector(authRepositoryProvider);
  }

  @Override
  public void injectMembers(LocationTrackingService instance) {
    injectAuthRepository(instance, authRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.boattaxie.app.service.LocationTrackingService.authRepository")
  public static void injectAuthRepository(LocationTrackingService instance,
      AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }
}
