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
public final class BoatTaxieMessagingService_MembersInjector implements MembersInjector<BoatTaxieMessagingService> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public BoatTaxieMessagingService_MembersInjector(
      Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  public static MembersInjector<BoatTaxieMessagingService> create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new BoatTaxieMessagingService_MembersInjector(authRepositoryProvider);
  }

  @Override
  public void injectMembers(BoatTaxieMessagingService instance) {
    injectAuthRepository(instance, authRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.boattaxie.app.service.BoatTaxieMessagingService.authRepository")
  public static void injectAuthRepository(BoatTaxieMessagingService instance,
      AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }
}
