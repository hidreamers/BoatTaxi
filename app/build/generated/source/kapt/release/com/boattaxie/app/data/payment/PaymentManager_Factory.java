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
public final class PaymentManager_Factory implements Factory<PaymentManager> {
  private final Provider<Context> contextProvider;

  public PaymentManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PaymentManager get() {
    return newInstance(contextProvider.get());
  }

  public static PaymentManager_Factory create(Provider<Context> contextProvider) {
    return new PaymentManager_Factory(contextProvider);
  }

  public static PaymentManager newInstance(Context context) {
    return new PaymentManager(context);
  }
}
