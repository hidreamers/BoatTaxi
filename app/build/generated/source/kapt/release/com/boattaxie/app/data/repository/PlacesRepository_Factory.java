package com.boattaxie.app.data.repository;

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
public final class PlacesRepository_Factory implements Factory<PlacesRepository> {
  private final Provider<Context> contextProvider;

  public PlacesRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PlacesRepository get() {
    return newInstance(contextProvider.get());
  }

  public static PlacesRepository_Factory create(Provider<Context> contextProvider) {
    return new PlacesRepository_Factory(contextProvider);
  }

  public static PlacesRepository newInstance(Context context) {
    return new PlacesRepository(context);
  }
}
