package com.boattaxie.app.data.repository;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
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
public final class AdvertisementRepository_Factory implements Factory<AdvertisementRepository> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<Context> contextProvider;

  public AdvertisementRepository_Factory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseStorage> storageProvider,
      Provider<Context> contextProvider) {
    this.authProvider = authProvider;
    this.firestoreProvider = firestoreProvider;
    this.storageProvider = storageProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public AdvertisementRepository get() {
    return newInstance(authProvider.get(), firestoreProvider.get(), storageProvider.get(), contextProvider.get());
  }

  public static AdvertisementRepository_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseStorage> storageProvider,
      Provider<Context> contextProvider) {
    return new AdvertisementRepository_Factory(authProvider, firestoreProvider, storageProvider, contextProvider);
  }

  public static AdvertisementRepository newInstance(FirebaseAuth auth, FirebaseFirestore firestore,
      FirebaseStorage storage, Context context) {
    return new AdvertisementRepository(auth, firestore, storage, context);
  }
}
