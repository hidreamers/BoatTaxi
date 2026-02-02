package com.boattaxie.app.ui.screens.verification;

import com.boattaxie.app.data.repository.AuthRepository;
import com.boattaxie.app.data.repository.VerificationRepository;
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
public final class VerificationViewModel_Factory implements Factory<VerificationViewModel> {
  private final Provider<VerificationRepository> verificationRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public VerificationViewModel_Factory(
      Provider<VerificationRepository> verificationRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.verificationRepositoryProvider = verificationRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public VerificationViewModel get() {
    return newInstance(verificationRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static VerificationViewModel_Factory create(
      Provider<VerificationRepository> verificationRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new VerificationViewModel_Factory(verificationRepositoryProvider, authRepositoryProvider);
  }

  public static VerificationViewModel newInstance(VerificationRepository verificationRepository,
      AuthRepository authRepository) {
    return new VerificationViewModel(verificationRepository, authRepository);
  }
}
