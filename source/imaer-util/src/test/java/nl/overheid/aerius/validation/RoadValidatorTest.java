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
package nl.overheid.aerius.validation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.overheid.aerius.shared.domain.v2.base.TimeUnit;
import nl.overheid.aerius.shared.domain.v2.source.RoadEmissionSource;
import nl.overheid.aerius.shared.domain.v2.source.SRM1RoadEmissionSource;
import nl.overheid.aerius.shared.domain.v2.source.SRM2RoadEmissionSource;
import nl.overheid.aerius.shared.domain.v2.source.road.CustomVehicles;
import nl.overheid.aerius.shared.domain.v2.source.road.RoadSpeedType;
import nl.overheid.aerius.shared.domain.v2.source.road.RoadType;
import nl.overheid.aerius.shared.domain.v2.source.road.SRM1LinearReference;
import nl.overheid.aerius.shared.domain.v2.source.road.SRM2LinearReference;
import nl.overheid.aerius.shared.domain.v2.source.road.SpecificVehicles;
import nl.overheid.aerius.shared.domain.v2.source.road.StandardVehicles;
import nl.overheid.aerius.shared.domain.v2.source.road.ValuesPerVehicleType;
import nl.overheid.aerius.shared.domain.v2.source.road.VehicleType;
import nl.overheid.aerius.shared.exception.AeriusException;
import nl.overheid.aerius.shared.exception.ImaerExceptionReason;

@ExtendWith(MockitoExtension.class)
class RoadValidatorTest {

  private static final String SOURCE_ID = "OurSourceId";
  private static final String SOURCE_LABEL = "Source label";

  @Mock RoadValidationHelper validationHelper;

  @Test
  void testValidSpecificCode() {
    final RoadEmissionSource source = constructSrm1Source();
    final SpecificVehicles subSource = new SpecificVehicles();
    final String specificCode = "SomePlanCode";
    mockSpecificVehicleCategory(specificCode);
    subSource.setVehicleCode(specificCode);
    subSource.setTimeUnit(TimeUnit.DAY);
    subSource.setVehiclesPerTimeUnit(200);
    source.getSubSources().add(subSource);

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertTrue(valid, "Valid test case");
    assertTrue(errors.isEmpty(), "No errors");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  @Test
  void testInvalidSpecificCode() {
    final RoadEmissionSource source = constructSrm1Source();
    final SpecificVehicles subSource = new SpecificVehicles();
    final String specificCode = "SomeSpecificVehicleCode";
    subSource.setVehicleCode(specificCode);
    subSource.setTimeUnit(TimeUnit.DAY);
    subSource.setVehiclesPerTimeUnit(200);
    source.getSubSources().add(subSource);

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertFalse(valid, "Invalid test case");
    assertEquals(1, errors.size(), "2 errors");
    assertEquals(ImaerExceptionReason.GML_UNKNOWN_MOBILE_SOURCE_CODE, errors.get(0).getReason(), "Error reason");
    assertArrayEquals(new Object[] {
        SOURCE_LABEL,
        specificCode
    }, errors.get(0).getArgs(), "Arguments");
  }

  @Test
  void testValidStandardCombination() {
    final RoadEmissionSource source = constructSrm1Source();
    final StandardVehicles subSource = new StandardVehicles();
    final VehicleType vehicleType = VehicleType.LIGHT_TRAFFIC;
    final Integer maximumSpeed = 80;
    final Boolean strictEnforcement = false;
    mockStandardCombinationSrm1(vehicleType, maximumSpeed, strictEnforcement);
    subSource.setTimeUnit(TimeUnit.DAY);
    final ValuesPerVehicleType valuesPerVehicleType = new ValuesPerVehicleType();
    valuesPerVehicleType.setVehiclesPerTimeUnit(200);
    subSource.getValuesPerVehicleTypes().put(vehicleType, valuesPerVehicleType);
    subSource.setMaximumSpeed(maximumSpeed);
    subSource.setStrictEnforcement(strictEnforcement);
    source.getSubSources().add(subSource);

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertTrue(valid, "Valid test case");
    assertTrue(errors.isEmpty(), "No errors");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  @Test
  void testInvalidStandardCombination() {
    final RoadEmissionSource source = constructSrm1Source();
    final StandardVehicles subSource = new StandardVehicles();
    final VehicleType vehicleType = VehicleType.LIGHT_TRAFFIC;
    final Integer maximumSpeed = 80;
    final Boolean strictEnforcement = false;
    subSource.setTimeUnit(TimeUnit.DAY);
    final ValuesPerVehicleType valuesPerVehicleType = new ValuesPerVehicleType();
    valuesPerVehicleType.setVehiclesPerTimeUnit(200);
    subSource.getValuesPerVehicleTypes().put(vehicleType, valuesPerVehicleType);
    subSource.setMaximumSpeed(maximumSpeed);
    subSource.setStrictEnforcement(strictEnforcement);
    source.getSubSources().add(subSource);

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertFalse(valid, "Invalid test case");
    assertEquals(1, errors.size(), "Number of errors");
    assertEquals(ImaerExceptionReason.GML_UNKNOWN_ROAD_CATEGORY, errors.get(0).getReason(), "Error reason");
    assertArrayEquals(new Object[] {
        SOURCE_LABEL,
        String.valueOf(RoadType.NON_URBAN_ROAD.getSectorId()),
        String.valueOf(maximumSpeed),
        String.valueOf(strictEnforcement),
        String.valueOf(vehicleType)
    }, errors.get(0).getArgs(), "Arguments");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  @Test
  void testInvalidVehicleAmount() {
    final RoadEmissionSource source = constructSrm1Source();
    final CustomVehicles subSource = new CustomVehicles();
    subSource.setTimeUnit(TimeUnit.DAY);
    subSource.setVehiclesPerTimeUnit(-200);
    source.getSubSources().add(subSource);

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertFalse(valid, "Invalid test case");
    assertEquals(1, errors.size(), "Number of errors");
    assertEquals(ImaerExceptionReason.SRM2_SOURCE_NEGATIVE_VEHICLES, errors.get(0).getReason(), "Error reason");
    assertArrayEquals(new Object[] {
        SOURCE_LABEL
    }, errors.get(0).getArgs(), "Arguments");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  @Test
  void testValidNoVehicleAmount() {
    final RoadEmissionSource source = constructSrm1Source();
    final CustomVehicles subSource = new CustomVehicles();
    subSource.setTimeUnit(TimeUnit.DAY);
    subSource.setVehiclesPerTimeUnit(0);
    source.getSubSources().add(subSource);

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertTrue(valid, "Valid test case");
    assertTrue(errors.isEmpty(), "No errors");
    assertEquals(1, warnings.size(), "No warnings");
    assertEquals(ImaerExceptionReason.SRM2_SOURCE_NO_VEHICLES, warnings.get(0).getReason(), "Error reason");
    assertArrayEquals(new Object[] {
        SOURCE_LABEL
    }, warnings.get(0).getArgs(), "Arguments");
  }

  @Test
  void testValidSrm1LinearReference() {
    final SRM1RoadEmissionSource source = constructSrm1Source();
    final StandardVehicles subSource = new StandardVehicles();
    final VehicleType vehicleType = VehicleType.LIGHT_TRAFFIC;
    final Integer maximumSpeed = 80;
    final Boolean strictEnforcement = false;
    mockStandardCombinationSrm1(vehicleType, maximumSpeed, strictEnforcement);
    subSource.setTimeUnit(TimeUnit.DAY);
    final ValuesPerVehicleType valuesPerVehicleType = new ValuesPerVehicleType();
    valuesPerVehicleType.setVehiclesPerTimeUnit(200);
    subSource.getValuesPerVehicleTypes().put(vehicleType, valuesPerVehicleType);
    subSource.setMaximumSpeed(maximumSpeed);
    subSource.setStrictEnforcement(strictEnforcement);
    source.getSubSources().add(subSource);
    final SRM1LinearReference linearReference = new SRM1LinearReference();
    linearReference.setFromPosition(0.5);
    linearReference.setToPosition(0.7);
    source.setPartialChanges(List.of(linearReference));

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertTrue(valid, "Valid test case");
    assertTrue(errors.isEmpty(), "No errors");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  @Test
  void testInvalidSrm1LinearReferenceFrom() {
    final SRM1RoadEmissionSource source = constructSrm1Source();
    final StandardVehicles subSource = new StandardVehicles();
    final VehicleType vehicleType = VehicleType.LIGHT_TRAFFIC;
    final Integer maximumSpeed = 80;
    final Boolean strictEnforcement = false;
    mockStandardCombinationSrm1(vehicleType, maximumSpeed, strictEnforcement);
    subSource.setTimeUnit(TimeUnit.DAY);
    final ValuesPerVehicleType valuesPerVehicleType = new ValuesPerVehicleType();
    valuesPerVehicleType.setVehiclesPerTimeUnit(200);
    subSource.getValuesPerVehicleTypes().put(vehicleType, valuesPerVehicleType);
    subSource.setMaximumSpeed(maximumSpeed);
    subSource.setStrictEnforcement(strictEnforcement);
    source.getSubSources().add(subSource);
    final SRM1LinearReference linearReference = new SRM1LinearReference();
    linearReference.setFromPosition(-0.5);
    linearReference.setToPosition(0.7);
    source.setPartialChanges(List.of(linearReference));

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertFalse(valid, "Invalid test case");
    assertEquals(1, errors.size(), "Number of errors");
    assertEquals(ImaerExceptionReason.GML_ROAD_SEGMENT_POSITION_NOT_FRACTION, errors.get(0).getReason(), "Error reason");
    assertArrayEquals(new Object[] {
        String.valueOf(-0.5)
    }, errors.get(0).getArgs(), "Arguments");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  @Test
  void testInvalidSrm1LinearReferenceTo() {
    final SRM1RoadEmissionSource source = constructSrm1Source();
    final StandardVehicles subSource = new StandardVehicles();
    final VehicleType vehicleType = VehicleType.LIGHT_TRAFFIC;
    final Integer maximumSpeed = 80;
    final Boolean strictEnforcement = false;
    mockStandardCombinationSrm1(vehicleType, maximumSpeed, strictEnforcement);
    subSource.setTimeUnit(TimeUnit.DAY);
    final ValuesPerVehicleType valuesPerVehicleType = new ValuesPerVehicleType();
    valuesPerVehicleType.setVehiclesPerTimeUnit(200);
    subSource.getValuesPerVehicleTypes().put(vehicleType, valuesPerVehicleType);
    subSource.setMaximumSpeed(maximumSpeed);
    subSource.setStrictEnforcement(strictEnforcement);
    source.getSubSources().add(subSource);
    final SRM1LinearReference linearReference = new SRM1LinearReference();
    linearReference.setFromPosition(0.5);
    linearReference.setToPosition(1.1);
    source.setPartialChanges(List.of(linearReference));

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertFalse(valid, "Invalid test case");
    assertEquals(1, errors.size(), "Number of errors");
    assertEquals(ImaerExceptionReason.GML_ROAD_SEGMENT_POSITION_NOT_FRACTION, errors.get(0).getReason(), "Error reason");
    assertArrayEquals(new Object[] {
        String.valueOf(1.1)
    }, errors.get(0).getArgs(), "Arguments");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  @Test
  void testValidSrm2LinearReference() {
    final SRM2RoadEmissionSource source = constructSrm2Source();
    final StandardVehicles subSource = new StandardVehicles();
    final VehicleType vehicleType = VehicleType.LIGHT_TRAFFIC;
    final Integer maximumSpeed = 80;
    final Boolean strictEnforcement = false;
    mockStandardCombinationSrm2(vehicleType, maximumSpeed, strictEnforcement);
    subSource.setTimeUnit(TimeUnit.DAY);
    final ValuesPerVehicleType valuesPerVehicleType = new ValuesPerVehicleType();
    valuesPerVehicleType.setVehiclesPerTimeUnit(200);
    subSource.getValuesPerVehicleTypes().put(vehicleType, valuesPerVehicleType);
    subSource.setMaximumSpeed(maximumSpeed);
    subSource.setStrictEnforcement(strictEnforcement);
    source.getSubSources().add(subSource);
    final SRM2LinearReference linearReference = new SRM2LinearReference();
    linearReference.setFromPosition(0.5);
    linearReference.setToPosition(0.7);
    source.setPartialChanges(List.of(linearReference));

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertTrue(valid, "Valid test case");
    assertTrue(errors.isEmpty(), "No errors");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  @Test
  void testInvalidSrm2LinearReference() {
    final SRM2RoadEmissionSource source = constructSrm2Source();
    final StandardVehicles subSource = new StandardVehicles();
    final VehicleType vehicleType = VehicleType.LIGHT_TRAFFIC;
    final Integer maximumSpeed = 80;
    final Boolean strictEnforcement = false;
    mockStandardCombinationSrm2(vehicleType, maximumSpeed, strictEnforcement);
    subSource.setTimeUnit(TimeUnit.DAY);
    final ValuesPerVehicleType valuesPerVehicleType = new ValuesPerVehicleType();
    valuesPerVehicleType.setVehiclesPerTimeUnit(200);
    subSource.getValuesPerVehicleTypes().put(vehicleType, valuesPerVehicleType);
    subSource.setMaximumSpeed(maximumSpeed);
    subSource.setStrictEnforcement(strictEnforcement);
    source.getSubSources().add(subSource);
    final SRM2LinearReference linearReference = new SRM2LinearReference();
    linearReference.setFromPosition(-0.5);
    linearReference.setToPosition(1.7);
    source.setPartialChanges(List.of(linearReference));

    final List<AeriusException> errors = new ArrayList<>();
    final List<AeriusException> warnings = new ArrayList<>();
    final RoadValidator validator = new RoadValidator(errors, warnings, validationHelper);

    final boolean valid = validator.validate(source);

    assertFalse(valid, "Invalid test case");
    assertEquals(2, errors.size(), "Number of errors");
    assertEquals(ImaerExceptionReason.GML_ROAD_SEGMENT_POSITION_NOT_FRACTION, errors.get(0).getReason(), "Error reason");
    assertArrayEquals(new Object[] {
        String.valueOf(-0.5)
    }, errors.get(0).getArgs(), "Arguments");
    assertEquals(ImaerExceptionReason.GML_ROAD_SEGMENT_POSITION_NOT_FRACTION, errors.get(1).getReason(), "Error reason");
    assertArrayEquals(new Object[] {
        String.valueOf(1.7)
    }, errors.get(1).getArgs(), "Arguments");
    assertTrue(warnings.isEmpty(), "No warnings");
  }

  private SRM1RoadEmissionSource constructSrm1Source() {
    final SRM1RoadEmissionSource source = new SRM1RoadEmissionSource();
    source.setGmlId(SOURCE_ID);
    source.setLabel(SOURCE_LABEL);
    source.setSectorId(RoadType.NON_URBAN_ROAD.getSectorId());
    source.setRoadSpeedType(RoadSpeedType.NATIONAL_ROAD);
    return source;
  }

  private SRM2RoadEmissionSource constructSrm2Source() {
    final SRM2RoadEmissionSource source = new SRM2RoadEmissionSource();
    source.setGmlId(SOURCE_ID);
    source.setLabel(SOURCE_LABEL);
    source.setSectorId(RoadType.FREEWAY.getSectorId());
    return source;
  }

  private void mockSpecificVehicleCategory(final String code) {
    when(validationHelper.isValidRoadSpecificVehicleCode(code)).thenReturn(true);
  }

  private void mockStandardCombinationSrm1(final VehicleType vehicleType, final Integer maximumSpeed, final Boolean strictEnforcement) {
    when(validationHelper.isValidRoadStandardVehicleCombination(RoadType.NON_URBAN_ROAD, vehicleType, maximumSpeed, strictEnforcement,
        RoadSpeedType.NATIONAL_ROAD))
            .thenReturn(true);
  }

  private void mockStandardCombinationSrm2(final VehicleType vehicleType, final Integer maximumSpeed, final Boolean strictEnforcement) {
    when(validationHelper.isValidRoadStandardVehicleCombination(RoadType.FREEWAY, vehicleType, maximumSpeed, strictEnforcement, null))
        .thenReturn(true);
  }

}
