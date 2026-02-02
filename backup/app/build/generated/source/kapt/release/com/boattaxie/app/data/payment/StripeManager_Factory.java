package com.boattaxie.app.data.payment;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class StripeManager_Factory implements Factory<StripeManager> {
  private final Provider<Context> contextProvider;

  public StripeManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public StripeManager get() {
    return newInstance(contextProvider.get());
  }

  public static StripeManager_Factory create(Provider<Context> contextProvider) {
    return new StripeManager_Factory(contextProvider);
  }

  public static StripeManager newInstance(Context context) {
    return new StripeManager(context);
  }
}
