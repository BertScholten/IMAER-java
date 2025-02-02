/*
 * Copyright the State of the Netherlands
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package nl.overheid.aerius.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import nl.overheid.aerius.gml.base.GMLLegacyCodeConverter.Conversion;
import nl.overheid.aerius.gml.base.GMLLegacyCodeConverter.GMLLegacyCodeType;
import nl.overheid.aerius.gml.base.conversion.MobileSourceOffRoadConversion;
import nl.overheid.aerius.shared.domain.Substance;
import nl.overheid.aerius.shared.domain.v2.geojson.Geometry;
import nl.overheid.aerius.shared.domain.v2.source.road.RoadSpeedType;
import nl.overheid.aerius.shared.domain.v2.source.road.RoadType;
import nl.overheid.aerius.shared.domain.v2.source.road.VehicleType;
import nl.overheid.aerius.shared.domain.v2.source.shipping.inland.InlandWaterway;
import nl.overheid.aerius.shared.domain.v2.source.shipping.inland.WaterwayDirection;
import nl.overheid.aerius.shared.domain.v2.source.shipping.maritime.ShippingMovementType;
import nl.overheid.aerius.shared.emissions.EmissionFactorSupplier;
import nl.overheid.aerius.shared.emissions.FarmLodgingEmissionFactorSupplier;
import nl.overheid.aerius.shared.emissions.InlandShippingEmissionFactorSupplier;
import nl.overheid.aerius.shared.emissions.MaritimeShippingEmissionFactorSupplier;
import nl.overheid.aerius.shared.emissions.OffRoadMobileEmissionFactorSupplier;
import nl.overheid.aerius.shared.emissions.PlanEmissionFactorSupplier;
import nl.overheid.aerius.shared.emissions.RoadEmissionFactorSupplier;
import nl.overheid.aerius.shared.emissions.shipping.InlandShippingRouteEmissionPoint;
import nl.overheid.aerius.shared.emissions.shipping.MaritimeShippingRouteEmissionPoint;
import nl.overheid.aerius.shared.emissions.shipping.ShippingLaden;
import nl.overheid.aerius.shared.exception.AeriusException;
import nl.overheid.aerius.validation.FarmLodgingValidationHelper;
import nl.overheid.aerius.validation.FarmlandValidationHelper;
import nl.overheid.aerius.validation.InlandShippingValidationHelper;
import nl.overheid.aerius.validation.MaritimeShippingValidationHelper;
import nl.overheid.aerius.validation.OffRoadValidationHelper;
import nl.overheid.aerius.validation.PlanValidationHelper;
import nl.overheid.aerius.validation.RoadValidationHelper;
import nl.overheid.aerius.validation.ValidationHelper;

/**
 * Test data for validation.
 */
public class TestValidationAndEmissionHelper implements ValidationHelper, EmissionFactorSupplier,
    FarmLodgingEmissionFactorSupplier, PlanEmissionFactorSupplier, OffRoadMobileEmissionFactorSupplier, RoadEmissionFactorSupplier,
    InlandShippingEmissionFactorSupplier, MaritimeShippingEmissionFactorSupplier,
    FarmLodgingValidationHelper, FarmlandValidationHelper, OffRoadValidationHelper, PlanValidationHelper, RoadValidationHelper,
    InlandShippingValidationHelper, MaritimeShippingValidationHelper {

  private static final List<Integer> SECTORS = Arrays.asList(
      1400,
      2100,
      3111,
      3112,
      3113,
      3210,
      3220,
      3230,
      4110,
      4140,
      4150,
      7510,
      7530,
      7610,
      7620,
      9000);

  private static final String FARM_ANIMAL_CODE = "D3";

  private static final List<FarmConstructHelper> FARM_LODGING_CATEGORIES = Arrays.asList(
      new FarmConstructHelper("A1.4", 9.2, false),
      new FarmConstructHelper("B1.100", 0.7, false),
      new FarmConstructHelper("C1.100", 1.9, false),
      new FarmConstructHelper("D3.1", 4.5, false, "BWL2001.21"),
      new FarmConstructHelper("D1.3.3", 2.5, false),
      new FarmConstructHelper("D3.2.7.2.1", 1.5, false),
      new FarmConstructHelper("F4.4", 0.2, true),
      new FarmConstructHelper("A4.2", 1.1, true, "BWL2011.12"),
      new FarmConstructHelper("A3.100", 4.4, false),
      new FarmConstructHelper("A2.100", 4.1, false),
      new FarmConstructHelper("A1.1", 5.7, false));

  private static final List<FarmConstructHelper> FARM_ADDITIONAL_SYSTEM_CATEGORIES = Arrays.asList(
      new FarmConstructHelper("E6.1.a", 0.01, false),
      new FarmConstructHelper("E6.5.b", 0.015, true));

  private static final List<FarmConstructHelper> FARM_REDUCTIVE_SYSTEM_CATEGORIES = Arrays.asList(
      new FarmConstructHelper("A4.3", 0.7, true),
      new FarmConstructHelper("G2.1.2", 0.7, true));

  private static final List<FarmFodderConstructHelper> FARM_FODDER_MEASURE_CATEGORIES = Arrays.asList(
      new FarmFodderConstructHelper("PAS2015.01-01", 0.16, 0.16, 0.16, 0.3, 0.7, true),
      new FarmFodderConstructHelper("PAS2015.05-01", 0.2, 0.2, 0.2, 0.3, 0.7, true),
      new FarmFodderConstructHelper("PAS2015.04-01", 0.1, 0.1, 0.1, 0.3, 0.7, false));

  private static final List<String> FARMLAND_CATEGORIES = Arrays.asList(
      "PASTURE",
      "MANURE");

  private static final List<String> INLAND_SHIPPING_TYPES = Arrays.asList(
      "BI",
      "BII-1",
      "BII-6L",
      "C1B",
      "C3B");

  private static final List<String> INLAND_WATERWAYS = Arrays.asList(
      "CEMT_VIb",
      "CEMT_Vb");

  private static final List<String> MARITIME_SHIPPING_TYPES = Arrays.asList(
      "GC3000",
      "GC5000",
      "KV10000",
      "OO100",
      "OO1600",
      "OO10000");

  private static final List<OffRoadConstructHelper> OFF_ROAD_MOBILE_SOURCE_CATEGORIES = Arrays.asList(
      new OffRoadConstructHelper("SI75560DSN", new EmissionHelper(0.03, 0.0000075), new EmissionHelper(0.005, 0.0), null),
      new OffRoadConstructHelper("SII75560DSN", new EmissionHelper(0.02, 0.0000075), new EmissionHelper(0.005, 0.0), null),
      new OffRoadConstructHelper("B4T", new EmissionHelper(0.004, 0.0000075), null, null),
      new OffRoadConstructHelper("SI75560DSN", new EmissionHelper(0.03, 0.0000075), new EmissionHelper(0.005, 0.0), null),
      new OffRoadConstructHelper("SV560DSJ", new EmissionHelper(0.025, 0.00024),
          new EmissionHelper(0.005, 0.0),
          new EmissionHelper(-0.46, 0.0)));

  private static final List<OffRoadOldCodesHelper> OFF_ROAD_MOBILE_SOURCE_OLD_CODES = Arrays.asList(
      new OffRoadOldCodesHelper("S1A", "SI75560DSN", 14.690993, 0.410843),
      new OffRoadOldCodesHelper("S2F", "SII75560DSN", 6.8731995, 0.410843),
      new OffRoadOldCodesHelper("SVM4C30", "B4T", 1.0687796, null),
      new OffRoadOldCodesHelper("P1980", "SI75560DSN", 16.208658, 0.410843));

  private static final List<GenericConstructHelper> PLAN_CATEGORIES = Arrays.asList(
      new GenericConstructHelper("PHA", new EmissionHelper(1.10997, 0.0)),
      new GenericConstructHelper("PHB", new EmissionHelper(1.55043, 0.0)),
      new GenericConstructHelper("POA", new EmissionHelper(0.161545, 0.0)),
      new GenericConstructHelper("PGA", new EmissionHelper(1004, 0.0)),
      new GenericConstructHelper("PEA", new EmissionHelper(0.3, 0.0)),
      new GenericConstructHelper("PIA", new EmissionHelper(0.675, 0.0)),
      new GenericConstructHelper("PWA", new EmissionHelper(341647, 0.0)),
      new GenericConstructHelper("PFA", new EmissionHelper(109614, 0.0)));

  private static final List<RoadConstructHelper> ROAD_CATEGORIES = Arrays.asList(
      new RoadConstructHelper("",
          new EmissionHelper(0.1021, 0.0466, 0.0261, 0.017),
          100, RoadType.FREEWAY, VehicleType.LIGHT_TRAFFIC, false, null,
          new EmissionHelper(0.1453, 0.0502, 0.0376, 0.029)),
      new RoadConstructHelper("",
          new EmissionHelper(0.0971, 0.046, 0.0235, 0.017),
          100, RoadType.FREEWAY, VehicleType.LIGHT_TRAFFIC, true, null,
          new EmissionHelper(0.1453, 0.0502, 0.0376, 0.029)),
      new RoadConstructHelper("",
          new EmissionHelper(0.124, 0.0476, 0.0315, 0.017),
          130, RoadType.FREEWAY, VehicleType.LIGHT_TRAFFIC, false, null,
          new EmissionHelper(0.1453, 0.0502, 0.0376, 0.029)),
      new RoadConstructHelper("",
          new EmissionHelper(1.1584, 0.0525, 0.1151, 0.0868),
          80, RoadType.FREEWAY, VehicleType.NORMAL_FREIGHT, false, null,
          new EmissionHelper(4.129, 0.0525, 0.1201, 0.146)),
      new RoadConstructHelper("",
          new EmissionHelper(1.4714, 0.0786, 0.1177, 0.0794),
          80, RoadType.FREEWAY, VehicleType.HEAVY_FREIGHT, false, null,
          new EmissionHelper(6.0702, 0.0786, 0.2166, 0.1566)),
      new RoadConstructHelper("",
          new EmissionHelper(1.4749, 0.0786, 0.1177, 0.0794),
          80, RoadType.FREEWAY, VehicleType.HEAVY_FREIGHT, true, null,
          new EmissionHelper(6.0702, 0.0786, 0.2166, 0.1566)),
      new RoadConstructHelper("",
          new EmissionHelper(1.4749, 0.0786, 0.1177, 0.0794),
          100, RoadType.FREEWAY, VehicleType.HEAVY_FREIGHT, false, null,
          new EmissionHelper(6.0702, 0.0786, 0.2166, 0.1566)),
      new RoadConstructHelper("",
          new EmissionHelper(1.4749, 0.0786, 0.1177, 0.0794),
          100, RoadType.FREEWAY, VehicleType.HEAVY_FREIGHT, true, null,
          new EmissionHelper(6.0702, 0.0786, 0.2166, 0.1566)),
      new RoadConstructHelper("",
          new EmissionHelper(0.1037, 0.0266, 0.0214, 0.0147),
          80, RoadType.NON_URBAN_ROAD, VehicleType.LIGHT_TRAFFIC, false, RoadSpeedType.NATIONAL_ROAD,
          new EmissionHelper(0.1037, 0.0266, 0.0214, 0.0147)),
      new RoadConstructHelper("",
          new EmissionHelper(2.9868, 0.0971, 0.1395, 0.0774),
          80, RoadType.NON_URBAN_ROAD, VehicleType.HEAVY_FREIGHT, false, RoadSpeedType.NATIONAL_ROAD,
          new EmissionHelper(2.9868, 0.0971, 0.1395, 0.0774)),
      new RoadConstructHelper("",
          new EmissionHelper(0.1425, 0.0146, 0.0227, 0.0288),
          0, RoadType.URBAN_ROAD, VehicleType.LIGHT_TRAFFIC, false, RoadSpeedType.URBAN_TRAFFIC_FREE_FLOW,
          new EmissionHelper(0.1884, 0.018, 0.0369, 0.0289)),
      new RoadConstructHelper("",
          new EmissionHelper(3.5597, 0.0772, 0.1612, 0.1318),
          0, RoadType.URBAN_ROAD, VehicleType.HEAVY_FREIGHT, false, RoadSpeedType.URBAN_TRAFFIC_FREE_FLOW,
          new EmissionHelper(5.5221, 0.0772, 0.351, 0.1639)),
      new RoadConstructHelper("",
          new EmissionHelper(0.1501, 0.016, 0.0276, 0.0287),
          0, RoadType.URBAN_ROAD, VehicleType.LIGHT_TRAFFIC, false, RoadSpeedType.URBAN_TRAFFIC_NORMAL,
          new EmissionHelper(0.1884, 0.018, 0.0369, 0.0289)));

  private static final List<String> ON_ROAD_MOBILE_SOURCE_CATEGORIES = Arrays.asList(
      "BA-B-E3",
      "BA-D-E6-ZW",
      "BA-L-E5");

  private static class EmissionHelper {
    final double emissionFactorNOx;
    final double emissionFactorNH3;
    final double emissionFactorNO2;
    final double emissionFactorPM10;

    EmissionHelper(final double emissionFactorNOx, final double emissionFactorNH3) {
      this(emissionFactorNOx, emissionFactorNH3, 0.0, 0.0);
    }

    EmissionHelper(final double emissionFactorNOx, final double emissionFactorNH3, final double emissionFactorNO2, final double emissionFactorPM10) {
      this.emissionFactorNOx = emissionFactorNOx;
      this.emissionFactorNH3 = emissionFactorNH3;
      this.emissionFactorNO2 = emissionFactorNO2;
      this.emissionFactorPM10 = emissionFactorPM10;
    }

    Map<Substance, Double> toEmissions() {
      final Map<Substance, Double> emissions = new HashMap<>();
      if (emissionFactorNOx != 0) {
        emissions.put(Substance.NOX, emissionFactorNOx);
      }
      if (emissionFactorNH3 != 0) {
        emissions.put(Substance.NH3, emissionFactorNH3);
      }
      if (emissionFactorNO2 != 0) {
        emissions.put(Substance.NO2, emissionFactorNO2);
      }
      if (emissionFactorPM10 != 0) {
        emissions.put(Substance.PM10, emissionFactorPM10);
      }
      return emissions;
    }

  }

  private static class GenericConstructHelper {

    final String code;
    final EmissionHelper emissions;

    GenericConstructHelper(final String code, final EmissionHelper emissions) {
      this.code = code;
      this.emissions = emissions;
    }
  }

  private static class OffRoadConstructHelper {

    final String code;
    final EmissionHelper emissionFactorsLiterFuel;
    final EmissionHelper emissionFactorsOperatingHours;
    final EmissionHelper emissionFactorsLiterAdBlue;

    public OffRoadConstructHelper(final String code, final EmissionHelper emissionFactorsLiterFuel,
        final EmissionHelper emissionFactorsOperatingHours, final EmissionHelper emissionFactorsAdBlue) {
      this.code = code;
      this.emissionFactorsLiterFuel = emissionFactorsLiterFuel;
      this.emissionFactorsOperatingHours = emissionFactorsOperatingHours;
      this.emissionFactorsLiterAdBlue = emissionFactorsAdBlue;
    }

  }

  private static class OffRoadOldCodesHelper {

    final String oldCode;
    final String newCode;
    final double fuelConsumptionUnderLoad;
    final Double fuelConsumptionIdle;

    public OffRoadOldCodesHelper(final String oldCode, final String newCode, final double fuelConsumptionUnderLoad,
        final Double fuelConsumptionIdle) {
      this.oldCode = oldCode;
      this.newCode = newCode;
      this.fuelConsumptionUnderLoad = fuelConsumptionUnderLoad;
      this.fuelConsumptionIdle = fuelConsumptionIdle;
    }

  }

  private static class RoadConstructHelper extends GenericConstructHelper {

    final int maximumSpeed;
    final RoadType roadType;
    final VehicleType vehicleType;
    final boolean strictEnforcement;
    final RoadSpeedType speedType;
    final EmissionHelper stagnatedEmissions;

    public RoadConstructHelper(final String code, final EmissionHelper emissions, final int maximumSpeed, final RoadType roadType,
        final VehicleType vehicleType,
        final boolean strictEnforcement, final RoadSpeedType speedType, final EmissionHelper stagnatedEmissions) {
      super(code, emissions);
      this.maximumSpeed = maximumSpeed;
      this.roadType = roadType;
      this.vehicleType = vehicleType;
      this.strictEnforcement = strictEnforcement;
      this.speedType = speedType;
      this.stagnatedEmissions = stagnatedEmissions;
    }

  }

  private static class FarmConstructHelper {

    final String code;
    final double emissionFactor;
    final boolean scrubber;
    final List<String> systemDefinitions;

    FarmConstructHelper(final String code, final double emissionFactor, final boolean scrubber, final String... systemDefinitions) {
      this.code = code;
      this.emissionFactor = emissionFactor;
      this.scrubber = scrubber;
      this.systemDefinitions = Arrays.asList(systemDefinitions);
    }
  }

  private static class FarmFodderConstructHelper {

    final String code;
    final double reductionTotal;
    final double reductionFloor;
    final double reductionCellar;
    final double proportionFloor;
    final double proportionCellar;
    final boolean matchLodging;

    FarmFodderConstructHelper(final String code, final double reductionTotal, final double reductionFloor, final double reductionCellar,
        final double proportionFloor, final double proportionCellar, final boolean matchLodging) {
      this.code = code;
      this.reductionTotal = reductionTotal;
      this.reductionFloor = reductionFloor;
      this.reductionCellar = reductionCellar;
      this.proportionFloor = proportionFloor;
      this.proportionCellar = proportionCellar;
      this.matchLodging = matchLodging;
    }
  }

  public static Map<GMLLegacyCodeType, Map<String, Conversion>> legacyCodes() {
    final Map<GMLLegacyCodeType, Map<String, Conversion>> legacyCodes = new HashMap<>();
    final Map<String, Conversion> onroadConversions = new HashMap<>();
    onroadConversions.put("27", new Conversion("BA-L-E5", true));
    legacyCodes.put(GMLLegacyCodeType.ON_ROAD_MOBILE_SOURCE, onroadConversions);
    final Map<String, Conversion> offroadConversions = new HashMap<>();
    OFF_ROAD_MOBILE_SOURCE_OLD_CODES.forEach(
        helper -> {
          offroadConversions.put(helper.oldCode, new Conversion(helper.newCode, true));
        });
    legacyCodes.put(GMLLegacyCodeType.OFF_ROAD_MOBILE_SOURCE, offroadConversions);
    return legacyCodes;
  }

  public static Map<String, MobileSourceOffRoadConversion> legacyMobileSourceOffRoadConversions() {
    final Map<String, MobileSourceOffRoadConversion> conversions = new HashMap<>();
    OFF_ROAD_MOBILE_SOURCE_OLD_CODES.forEach(
        helper -> conversions.put(helper.oldCode, new MobileSourceOffRoadConversion(helper.fuelConsumptionUnderLoad, helper.fuelConsumptionIdle)));
    return conversions;
  }

  @Override
  public FarmLodgingEmissionFactorSupplier farmLodging() {
    return this;
  }

  @Override
  public PlanEmissionFactorSupplier plan() {
    return this;
  }

  @Override
  public OffRoadMobileEmissionFactorSupplier offRoadMobile() {
    return this;
  }

  @Override
  public RoadEmissionFactorSupplier road() {
    return this;
  }

  @Override
  public InlandShippingEmissionFactorSupplier inlandShipping() {
    return this;
  }

  @Override
  public MaritimeShippingEmissionFactorSupplier maritimeShipping() {
    return this;
  }

  @Override
  public FarmLodgingValidationHelper farmLodgingValidation() {
    return this;
  }

  @Override
  public FarmlandValidationHelper farmlandValidation() {
    return this;
  }

  @Override
  public OffRoadValidationHelper offRoadMobileValidation() {
    return this;
  }

  @Override
  public PlanValidationHelper planValidation() {
    return this;
  }

  @Override
  public RoadValidationHelper roadValidation() {
    return this;
  }

  @Override
  public InlandShippingValidationHelper inlandShippingValidation() {
    return this;
  }

  @Override
  public MaritimeShippingValidationHelper maritimeShippingValidation() {
    return this;
  }

  @Override
  public Map<Substance, Double> getLodgingEmissionFactors(final String lodgingCode) {
    return farmLodging(lodgingCode)
        .map(c -> Map.of(Substance.NH3, c.emissionFactor))
        .orElse(Map.of());
  }

  @Override
  public boolean canLodgingEmissionFactorsBeConstrained(final String lodgingCode) {
    return false;
  }

  @Override
  public boolean isAdditionalSystemScrubber(final String additionalSystemCode) {
    return farmAdditionalSystem(additionalSystemCode).map(c -> c.scrubber).orElse(false);
  }

  @Override
  public boolean isReductiveSystemScrubber(final String reductiveSystemCode) {
    return farmReductiveSystem(reductiveSystemCode).map(c -> c.scrubber).orElse(false);
  }

  @Override
  public Map<Substance, Double> getLodgingConstrainedEmissionFactors(final String lodgingCode) {
    return farmLodging(lodgingCode)
        .map(c -> Map.of(Substance.NH3, c.emissionFactor))
        .orElse(Map.of());
  }

  @Override
  public Map<Substance, Double> getAdditionalSystemEmissionFactors(final String additionalSystemCode) {
    return farmAdditionalSystem(additionalSystemCode)
        .map(c -> Map.of(Substance.NH3, c.emissionFactor))
        .orElse(Map.of());
  }

  @Override
  public Map<Substance, Double> getReductiveSystemRemainingFractions(final String reductiveSystemCode) {
    return farmReductiveSystem(reductiveSystemCode)
        .map(c -> Map.of(Substance.NH3, 1 - c.emissionFactor))
        .orElse(Map.of());
  }

  @Override
  public Map<Substance, Double> getFodderRemainingFractionTotal(final String fodderMeasureCode) {
    return farmFodderMeasure(fodderMeasureCode)
        .map(c -> Map.of(Substance.NH3, 1 - c.reductionTotal))
        .orElse(Map.of());
  }

  @Override
  public boolean canFodderApplyToLodging(final String fodderMeasureCode, final String lodgingCode) {
    return farmFodderMeasure(fodderMeasureCode).map(c -> c.matchLodging).orElse(false);
  }

  @Override
  public Map<Substance, Double> getFodderProportionFloor(final String fodderMeasureCode, final String lodgingCode) {
    return farmFodderMeasure(fodderMeasureCode)
        .map(c -> Map.of(Substance.NH3, c.proportionFloor))
        .orElse(Map.of());
  }

  @Override
  public Map<Substance, Double> getFodderProportionCellar(final String fodderMeasureCode, final String lodgingCode) {
    return farmFodderMeasure(fodderMeasureCode)
        .map(c -> Map.of(Substance.NH3, c.proportionCellar))
        .orElse(Map.of());
  }

  @Override
  public Map<Substance, Double> getFodderRemainingFractionFloor(final String fodderMeasureCode) {
    return farmFodderMeasure(fodderMeasureCode)
        .map(c -> Map.of(Substance.NH3, 1 - c.reductionFloor))
        .orElse(Map.of());
  }

  @Override
  public Map<Substance, Double> getFodderRemainingFractionCellar(final String fodderMeasureCode) {
    return farmFodderMeasure(fodderMeasureCode)
        .map(c -> Map.of(Substance.NH3, 1 - c.reductionCellar))
        .orElse(Map.of());
  }

  @Override
  public boolean isValidFarmLodgingCode(final String lodgingCode) {
    return farmLodging(lodgingCode).isPresent();
  }

  @Override
  public boolean isValidFarmLodgingAdditionalSystemCode(final String systemCode) {
    return farmAdditionalSystem(systemCode).isPresent();
  }

  @Override
  public boolean isValidFarmLodgingReductiveSystemCode(final String systemCode) {
    return farmReductiveSystem(systemCode).isPresent();
  }

  @Override
  public boolean isValidFarmLodgingFodderMeasureCode(final String fodderMeasureCode) {
    return farmFodderMeasure(fodderMeasureCode).isPresent();
  }

  @Override
  public boolean isValidFarmlandActivityCode(final String activityCode) {
    return FARMLAND_CATEGORIES.stream()
        .anyMatch(c -> c.equalsIgnoreCase(activityCode));
  }

  @Override
  public boolean isValidPlanCode(final String planCode) {
    return plan(planCode).isPresent();
  }

  @Override
  public boolean isEmissionFactorPerUnits(final String planCode) {
    // We have no NO_UNIT plans (anymore), so always true
    return true;
  }

  @Override
  public Map<Substance, Double> getPlanEmissionFactors(final String planCode) {
    return plan(planCode)
        .map(c -> c.emissions.toEmissions())
        .orElse(Map.of());
  }

  @Override
  public boolean isValidOffRoadMobileSourceCode(final String offRoadMobileSourceCode) {
    return offRoad(offRoadMobileSourceCode).isPresent();
  }

  @Override
  public boolean expectsLiterFuelPerYear(final String offRoadMobileSourceCode) {
    return offRoad(offRoadMobileSourceCode)
        .map(c -> c.emissionFactorsLiterFuel != null)
        .orElse(false);
  }

  @Override
  public boolean expectsOperatingHoursPerYear(final String offRoadMobileSourceCode) {
    return offRoad(offRoadMobileSourceCode)
        .map(c -> c.emissionFactorsOperatingHours != null)
        .orElse(false);
  }

  @Override
  public OptionalDouble getMaxAdBlueFuelRatio(final String offRoadMobileSourceCode) {
    return expectsLiterAdBluePerYear(offRoadMobileSourceCode) ? OptionalDouble.of(0.07) : OptionalDouble.empty();
  }

  @Override
  public boolean expectsLiterAdBluePerYear(final String offRoadMobileSourceCode) {
    return offRoad(offRoadMobileSourceCode)
        .map(c -> c.emissionFactorsLiterAdBlue != null)
        .orElse(false);
  }

  @Override
  public Map<Substance, Double> getOffRoadMobileEmissionFactorsPerLiterFuel(final String offRoadMobileSourceCode) {
    return offRoad(offRoadMobileSourceCode)
        .map(c -> c.emissionFactorsLiterFuel)
        .map(d -> d.toEmissions())
        .orElse(Map.of());
  }

  @Override
  public Map<Substance, Double> getOffRoadMobileEmissionFactorsPerOperatingHour(final String offRoadMobileSourceCode) {
    return offRoad(offRoadMobileSourceCode)
        .map(c -> c.emissionFactorsOperatingHours)
        .map(d -> d.toEmissions())
        .orElse(Map.of());
  }

  @Override
  public Map<Substance, Double> getOffRoadMobileEmissionFactorsPerLiterAdBlue(final String offRoadMobileSourceCode) {
    return offRoad(offRoadMobileSourceCode)
        .map(c -> c.emissionFactorsLiterAdBlue)
        .map(d -> d.toEmissions())
        .orElse(Map.of());
  }

  @Override
  public boolean isValidRoadSpecificVehicleCode(final String specificVehicleCode) {
    return roadSpecific(specificVehicleCode).isPresent();
  }

  @Override
  public Map<Substance, Double> getRoadSpecificVehicleEmissionFactors(final String specificVehicleCode, final RoadType roadType) {
    return roadSpecific(specificVehicleCode)
        .map(c -> new HashMap<Substance, Double>())
        .orElse(null);
  }

  @Override
  public boolean isValidRoadStandardVehicleCombination(final RoadType roadType, final VehicleType vehicleType, final Integer maximumSpeed,
      final Boolean strictEnforced, final RoadSpeedType speedType) {
    return roadStandard(vehicleType, roadType, maximumSpeed, strictEnforced, speedType).isPresent();
  }

  @Override
  public Map<Substance, Double> getRoadStandardVehicleEmissionFactors(final VehicleType vehicleType, final RoadType roadType,
      final RoadSpeedType roadSpeedType, final Integer maximumSpeed, final Boolean strictEnforcement) {
    return roadStandard(vehicleType, roadType, maximumSpeed, strictEnforcement, roadSpeedType)
        .map(c -> c.emissions.toEmissions())
        .orElseThrow();
  }

  @Override
  public Map<Substance, Double> getRoadStandardVehicleStagnatedEmissionFactors(final VehicleType vehicleType, final RoadType roadType,
      final RoadSpeedType roadSpeedType, final Integer maximumSpeed, final Boolean strictEnforcement) {
    return roadStandard(vehicleType, roadType, maximumSpeed, strictEnforcement, roadSpeedType)
        .map(c -> c.stagnatedEmissions.toEmissions())
        .orElseThrow();
  }

  @Override
  public boolean isValidInlandShipCode(final String shipCode) {
    return INLAND_SHIPPING_TYPES.stream()
        .anyMatch(c -> c.equalsIgnoreCase(shipCode));
  }

  @Override
  public boolean isValidInlandWaterwayCode(final String waterwayCode) {
    return INLAND_WATERWAYS.stream()
        .anyMatch(c -> c.equalsIgnoreCase(waterwayCode));
  }

  @Override
  public boolean isValidInlandShipWaterwayCombination(final String shipCode, final String waterwayCode) {
    // All available combinations are valid
    return isValidInlandShipCode(shipCode) && isValidInlandWaterwayCode(waterwayCode);
  }

  @Override
  public List<InlandShippingRouteEmissionPoint> determineInlandRoutePoints(final Geometry geometry, final Optional<InlandWaterway> waterway) {
    // Not implemented
    return null;
  }

  @Override
  public Map<Substance, Double> getInlandShippingRouteEmissionFactors(final String waterwayCode, final WaterwayDirection direction,
      final ShippingLaden laden,
      final String shipCode) {
    // Not implemented
    return null;
  }

  @Override
  public Map<Substance, Double> getInlandShippingDockedEmissionFactors(final String shipCode) {
    // Not implemented
    return null;
  }

  @Override
  public boolean isValidMaritimeShipCode(final String shipCode) {
    return MARITIME_SHIPPING_TYPES.stream()
        .anyMatch(c -> c.equalsIgnoreCase(shipCode));
  }

  @Override
  public List<MaritimeShippingRouteEmissionPoint> determineMaritimeRoutePoints(final Geometry geometry) {
    // Not implemented
    return null;
  }

  @Override
  public List<MaritimeShippingRouteEmissionPoint> determineMaritimeMooringInlandRoutePoints(final Geometry geometry, final String shipCode,
      final boolean mooringOnA, final boolean mooringOnB) {
    // Not implemented
    return null;
  }

  @Override
  public List<MaritimeShippingRouteEmissionPoint> determineMaritimeMooringInlandRoutePointsForGrossTonnage(final Geometry geometry,
      final int grossTonnage, final boolean mooringOnA, final boolean mooringOnB) throws AeriusException {
    // Not implemented
    return null;
  }

  @Override
  public Map<Substance, Double> getMaritimeShippingRouteEmissionFactors(final ShippingMovementType movementType, final String shipCode) {
    // Not implemented
    return null;
  }

  @Override
  public Map<Substance, Double> getMaritimeShippingDockedEmissionFactors(final String shipCode) {
    // Not implemented
    return null;
  }

  private Optional<FarmConstructHelper> farmLodging(final String farmLodgingCode) {
    return FARM_LODGING_CATEGORIES.stream()
        .filter(c -> c.code.equalsIgnoreCase(farmLodgingCode))
        .findFirst();
  }

  private Optional<FarmConstructHelper> farmAdditionalSystem(final String additionalSystemCode) {
    return FARM_ADDITIONAL_SYSTEM_CATEGORIES.stream()
        .filter(c -> c.code.equalsIgnoreCase(additionalSystemCode))
        .findFirst();
  }

  private Optional<FarmConstructHelper> farmReductiveSystem(final String reductiveSystemCode) {
    return FARM_REDUCTIVE_SYSTEM_CATEGORIES.stream()
        .filter(c -> c.code.equalsIgnoreCase(reductiveSystemCode))
        .findFirst();
  }

  private Optional<FarmFodderConstructHelper> farmFodderMeasure(final String fodderMeasureCode) {
    return FARM_FODDER_MEASURE_CATEGORIES.stream()
        .filter(c -> c.code.equalsIgnoreCase(fodderMeasureCode))
        .findFirst();
  }

  private Optional<GenericConstructHelper> plan(final String planCode) {
    return PLAN_CATEGORIES.stream()
        .filter(c -> c.code.equalsIgnoreCase(planCode))
        .findFirst();
  }

  private Optional<OffRoadConstructHelper> offRoad(final String offRoadMobileSourceCode) {
    return OFF_ROAD_MOBILE_SOURCE_CATEGORIES.stream()
        .filter(c -> c.code.equalsIgnoreCase(offRoadMobileSourceCode))
        .findFirst();
  }

  private Optional<String> roadSpecific(final String specificVehicleCode) {
    return ON_ROAD_MOBILE_SOURCE_CATEGORIES.stream()
        .filter(c -> c.equalsIgnoreCase(specificVehicleCode))
        .findFirst();
  }

  private Optional<RoadConstructHelper> roadStandard(final VehicleType vehicleType, final RoadType roadType,
      final Integer maximumSpeed, final Boolean strictEnforcement, final RoadSpeedType roadSpeedType) {
    final List<RoadConstructHelper> applicable = ROAD_CATEGORIES.stream()
        .filter(c -> c.vehicleType == vehicleType
            && c.roadType == roadType
            && c.strictEnforcement == (strictEnforcement == null ? false : strictEnforcement))
        .collect(Collectors.toList());
    if (applicable.isEmpty()) {
      return Optional.empty();
    } else if (applicable.size() == 1) {
      return Optional.of(applicable.get(0));
    } else {
      final List<RoadConstructHelper> nextApplicable = applicable.stream()
          .filter(c -> c.speedType == roadSpeedType)
          .filter(c -> c.maximumSpeed >= (maximumSpeed == null ? 0 : maximumSpeed))
          .sorted((a, b) -> Integer.compare(a.maximumSpeed, b.maximumSpeed))
          .collect(Collectors.toList());
      if (nextApplicable.isEmpty()) {
        return applicable.stream()
            .sorted((a, b) -> Integer.compare(b.maximumSpeed, a.maximumSpeed))
            .findFirst();
      } else {
        return Optional.of(nextApplicable.get(0));
      }
    }
  }

}
