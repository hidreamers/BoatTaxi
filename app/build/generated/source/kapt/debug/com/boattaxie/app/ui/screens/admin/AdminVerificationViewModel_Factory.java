package com.boattaxie.app.ui.screens.admin;

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
public final class AdminVerificationViewModel_Factory implements Factory<AdminVerificationViewModel> {
  private final Provider<VerificationRepository> verificationRepositoryProvider;

  public AdminVerificationViewModel_Factory(
      Provider<VerificationRepository> verificationRepositoryProvider) {
    this.verificationRepositoryProvider = verificationRepositoryProvider;
  }

  @Override
  public AdminVerificationViewModel get() {
    return newInstance(verificationRepositoryProvider.get());
  }

  public static AdminVerificationViewModel_Factory create(
      Provider<VerificationRepository> verificationRepositoryProvider) {
    return new AdminVerificationViewModel_Factory(verificationRepositoryProvider);
  }

  public static AdminVerificationViewModel newInstance(
      VerificationRepository verificationRepository) {
    return new AdminVerificationViewModel(verificationRepository);
  }
}
