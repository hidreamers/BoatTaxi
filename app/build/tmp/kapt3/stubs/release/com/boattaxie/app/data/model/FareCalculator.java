package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Fare Calculator based on Bocas del Toro, Panama pricing:
 *
 * WATER TAXIS (Boats):
 * - Almirante to Bocas Town: ~$6-$8
 * - Bocas Town to Bastimentos: ~$3-$5/person
 * - Bocas Town to Carenero: ~$2-$3/person
 * - Bocas Town to Solarte (Bambuda): ~$20 for 1-2 people
 * - Island Hopping: ~$5-$10+/person
 *
 * LAND TAXIS (Isla Colón):
 * - Within Bocas Town: $1-$2/person
 * - Bocas Town to Airport: ~$2/person
 * - Bocas Town to Boca del Drago: ~$2.50/person
 *
 * TOURIST PRICING: Tourists may be charged ~20-30% more
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J2\u0010\f\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00042\b\b\u0002\u0010\u0014\u001a\u00020\u0015J\u001e\u0010\u0016\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00040\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/boattaxie/app/data/model/FareCalculator;", "", "()V", "TOURIST_MARKUP", "", "boatFareConfig", "Lcom/boattaxie/app/data/model/FareConfig;", "fixedRoutes", "", "", "Lcom/boattaxie/app/data/model/RoutePrice;", "taxiFareConfig", "calculateFare", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "distanceKm", "", "durationMinutes", "", "surgeMultiplier", "isTourist", "", "getFareEstimate", "Lcom/boattaxie/app/data/model/FareEstimate;", "getMinimumFares", "app_release"})
public final class FareCalculator {
    @org.jetbrains.annotations.NotNull()
    private static final com.boattaxie.app.data.model.FareConfig boatFareConfig = null;
    @org.jetbrains.annotations.NotNull()
    private static final com.boattaxie.app.data.model.FareConfig taxiFareConfig = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, com.boattaxie.app.data.model.RoutePrice> fixedRoutes = null;
    private static final double TOURIST_MARKUP = 1.2;
    @org.jetbrains.annotations.NotNull()
    public static final com.boattaxie.app.data.model.FareCalculator INSTANCE = null;
    
    private FareCalculator() {
        super();
    }
    
    public final double calculateFare(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, float distanceKm, int durationMinutes, double surgeMultiplier, boolean isTourist) {
        return 0.0;
    }
    
    /**
     * Get fare estimate with local and tourist prices
     */
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.FareEstimate getFareEstimate(@org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, float distanceKm, int durationMinutes) {
        return null;
    }
    
    /**
     * Get minimum fare info for display
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.boattaxie.app.data.model.VehicleType, java.lang.Double> getMinimumFares() {
        return null;
    }
}