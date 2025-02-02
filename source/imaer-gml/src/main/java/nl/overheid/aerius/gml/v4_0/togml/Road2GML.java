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
package nl.overheid.aerius.gml.v4_0.togml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import nl.overheid.aerius.gml.v4_0.source.TimeUnit;
import nl.overheid.aerius.gml.v4_0.source.road.CustomVehicle;
import nl.overheid.aerius.gml.v4_0.source.road.RoadEmissionSource;
import nl.overheid.aerius.gml.v4_0.source.road.RoadSideBarrierProperty;
import nl.overheid.aerius.gml.v4_0.source.road.SRM1Road;
import nl.overheid.aerius.gml.v4_0.source.road.SRM1RoadLinearReference;
import nl.overheid.aerius.gml.v4_0.source.road.SRM1RoadLinearReferenceProperty;
import nl.overheid.aerius.gml.v4_0.source.road.SRM2Road;
import nl.overheid.aerius.gml.v4_0.source.road.SRM2RoadLinearReference;
import nl.overheid.aerius.gml.v4_0.source.road.SRM2RoadLinearReferenceProperty;
import nl.overheid.aerius.gml.v4_0.source.road.SpecificVehicle;
import nl.overheid.aerius.gml.v4_0.source.road.StandardVehicle;
import nl.overheid.aerius.gml.v4_0.source.road.VehiclesProperty;
import nl.overheid.aerius.shared.domain.Substance;
import nl.overheid.aerius.shared.domain.v2.source.SRM1RoadEmissionSource;
import nl.overheid.aerius.shared.domain.v2.source.SRM2RoadEmissionSource;
import nl.overheid.aerius.shared.domain.v2.source.road.CustomVehicles;
import nl.overheid.aerius.shared.domain.v2.source.road.RoadElevation;
import nl.overheid.aerius.shared.domain.v2.source.road.RoadSideBarrier;
import nl.overheid.aerius.shared.domain.v2.source.road.SRM1LinearReference;
import nl.overheid.aerius.shared.domain.v2.source.road.SRM2LinearReference;
import nl.overheid.aerius.shared.domain.v2.source.road.SpecificVehicles;
import nl.overheid.aerius.shared.domain.v2.source.road.StandardVehicles;
import nl.overheid.aerius.shared.domain.v2.source.road.ValuesPerVehicleType;
import nl.overheid.aerius.shared.domain.v2.source.road.VehicleType;
import nl.overheid.aerius.shared.domain.v2.source.road.Vehicles;

/**
 * Converts {@link nl.overheid.aerius.shared.domain.v2.source.RoadEmissionSource} to GML data object.
 */
class Road2GML extends SpecificSource2GML<nl.overheid.aerius.shared.domain.v2.source.RoadEmissionSource> {

  @Override
  public nl.overheid.aerius.gml.v4_0.source.EmissionSource convert(
      final nl.overheid.aerius.shared.domain.v2.source.RoadEmissionSource emissionSource) {
    final nl.overheid.aerius.gml.v4_0.source.EmissionSource returnSource;

    if (emissionSource instanceof SRM1RoadEmissionSource) {
      returnSource = convertSrm1((SRM1RoadEmissionSource) emissionSource);
    } else if (emissionSource instanceof SRM2RoadEmissionSource) {
      returnSource = convertSrm2((SRM2RoadEmissionSource) emissionSource);
    } else {
      returnSource = null;
    }

    return returnSource;
  }

  private SRM2Road convertSrm2(final SRM2RoadEmissionSource emissionSource) {
    final SRM2Road returnSource = new SRM2Road();

    returnSource.setVehicles(toVehicleProperties(emissionSource.getSubSources(), emissionSource.isFreeway()));

    handleFreeWay(emissionSource, returnSource);
    handleTunnel(emissionSource, returnSource);
    handleElevation(emissionSource, returnSource);
    handleBarriers(emissionSource, returnSource);
    handleRoadManager(emissionSource, returnSource);
    handleTrafficDirection(emissionSource, returnSource);

    returnSource.setDynamicSegments(getSrm2DynamicSegments(emissionSource));

    return returnSource;
  }

  private SRM1Road convertSrm1(final SRM1RoadEmissionSource emissionSource) {
    final SRM1Road returnSource = new SRM1Road();

    returnSource.setVehicles(toVehicleProperties(emissionSource.getSubSources(), false));

    handleTunnel(emissionSource, returnSource);
    handleSpeedProfile(emissionSource, returnSource);
    handleRoadManager(emissionSource, returnSource);
    handleTrafficDirection(emissionSource, returnSource);

    returnSource.setDynamicSegments(getSrm1DynamicSegments(emissionSource));

    return returnSource;
  }

  private List<VehiclesProperty> toVehicleProperties(final List<Vehicles> vehicleGroups, final boolean isFreeway) {
    final List<VehiclesProperty> vehiclesList = new ArrayList<>(vehicleGroups.size());

    for (final Vehicles vehicleGroup : vehicleGroups) {
      if (vehicleGroup instanceof StandardVehicles) {
        addVehicleEmissionSource(vehiclesList, (StandardVehicles) vehicleGroup, isFreeway);
      } else if (vehicleGroup instanceof SpecificVehicles) {
        addVehicleEmissionSource(vehiclesList, (SpecificVehicles) vehicleGroup);
      } else if (vehicleGroup instanceof CustomVehicles) {
        addVehicleEmissionSource(vehiclesList, (CustomVehicles) vehicleGroup);
      } else {
        throw new IllegalArgumentException("EmissionCategory for traffic not allowed to be null: " + vehicleGroup);
      }
    }
    return vehiclesList;
  }

  private void handleFreeWay(final nl.overheid.aerius.shared.domain.v2.source.SRM2RoadEmissionSource emissionSource, final SRM2Road returnSource) {
    returnSource.setFreeway(emissionSource.isFreeway());
  }

  private void handleTunnel(final nl.overheid.aerius.shared.domain.v2.source.SRM2RoadEmissionSource emissionSource, final SRM2Road returnSource) {
    //don't return tunnel factor if it's the default value.
    if (Double.doubleToLongBits(emissionSource.getTunnelFactor()) != Double.doubleToLongBits(1.0)) {
      returnSource.setTunnelFactor(emissionSource.getTunnelFactor());
    }
  }

  private void handleTunnel(final nl.overheid.aerius.shared.domain.v2.source.SRM1RoadEmissionSource emissionSource, final SRM1Road returnSource) {
    //don't return tunnel factor if it's the default value.
    if (Double.doubleToLongBits(emissionSource.getTunnelFactor()) != Double.doubleToLongBits(1.0)) {
      returnSource.setTunnelFactor(emissionSource.getTunnelFactor());
    }
  }

  private void handleElevation(final nl.overheid.aerius.shared.domain.v2.source.SRM2RoadEmissionSource emissionSource, final SRM2Road returnSource) {
    returnSource.setElevation(emissionSource.getElevation());
    //don't set the elevation height if it's normal elevation.
    if (returnSource.getElevation() != RoadElevation.NORMAL) {
      returnSource.setElevationHeight(emissionSource.getElevationHeight());
    }
  }

  private void handleBarriers(final nl.overheid.aerius.shared.domain.v2.source.SRM2RoadEmissionSource emissionSource, final SRM2Road returnSource) {
    if (emissionSource.getBarrierLeft() != null) {
      returnSource.setBarrierLeft(toGMLRoadSideBarrier(emissionSource.getBarrierLeft()));
    }
    if (emissionSource.getBarrierRight() != null) {
      returnSource.setBarrierRight(toGMLRoadSideBarrier(emissionSource.getBarrierRight()));
    }
  }

  private RoadSideBarrierProperty toGMLRoadSideBarrier(final RoadSideBarrier barrier) {
    final nl.overheid.aerius.gml.v4_0.source.road.RoadSideBarrier gmlBarrier = new nl.overheid.aerius.gml.v4_0.source.road.RoadSideBarrier();
    gmlBarrier.setBarrierType(barrier.getBarrierType());
    gmlBarrier.setHeight(barrier.getHeight());
    gmlBarrier.setDistance(barrier.getDistance());
    return new RoadSideBarrierProperty(gmlBarrier);
  }

  private void handleRoadManager(final nl.overheid.aerius.shared.domain.v2.source.RoadEmissionSource emissionSource,
      final RoadEmissionSource returnSource) {
    returnSource.setRoadManager(emissionSource.getRoadManager());
  }

  private void handleTrafficDirection(final nl.overheid.aerius.shared.domain.v2.source.RoadEmissionSource emissionSource,
      final RoadEmissionSource returnSource) {
    returnSource.setTrafficDirection(emissionSource.getTrafficDirection());
  }

  private void handleSpeedProfile(final nl.overheid.aerius.shared.domain.v2.source.SRM1RoadEmissionSource emissionSource,
      final SRM1Road returnSource) {
    returnSource.setSpeedProfile(emissionSource.getRoadSpeedType());
  }

  private void addVehicleEmissionSource(final List<VehiclesProperty> vehiclesList, final StandardVehicles vse, final boolean isFreeway) {
    for (final Entry<VehicleType, ValuesPerVehicleType> entry : vse.getValuesPerVehicleTypes().entrySet()) {
      final StandardVehicle sv = new StandardVehicle();
      sv.setVehiclesPerTimeUnit(entry.getValue().getVehiclesPerTimeUnit());
      sv.setStagnationFactor(entry.getValue().getStagnationFraction());
      sv.setVehicleType(entry.getKey());
      sv.setMaximumSpeed(vse.getMaximumSpeed());
      if (isFreeway) {
        sv.setStrictEnforcement(vse.getStrictEnforcement());
      }
      sv.setTimeUnit(TimeUnit.from(vse.getTimeUnit()));
      vehiclesList.add(new VehiclesProperty(sv));
    }
  }

  private void addVehicleEmissionSource(final List<VehiclesProperty> vehiclesList, final SpecificVehicles vse) {
    final SpecificVehicle sv = new SpecificVehicle();

    sv.setCode(vse.getVehicleCode());
    sv.setVehiclesPerTimeUnit(vse.getVehiclesPerTimeUnit());
    sv.setTimeUnit(TimeUnit.from(vse.getTimeUnit()));
    vehiclesList.add(new VehiclesProperty(sv));
  }

  private void addVehicleEmissionSource(final List<VehiclesProperty> vehiclesList, final CustomVehicles vce) {
    final CustomVehicle cv = new CustomVehicle();
    cv.setDescription(vce.getDescription());
    cv.setEmissions(getEmissions(vce.getEmissionFactors(), Substance.NOX));
    cv.setVehiclesPerTimeUnit(vce.getVehiclesPerTimeUnit());
    cv.setTimeUnit(TimeUnit.from(vce.getTimeUnit()));
    vehiclesList.add(new VehiclesProperty(cv));
  }

  private List<SRM2RoadLinearReferenceProperty> getSrm2DynamicSegments(final SRM2RoadEmissionSource emissionSource) {
    final List<SRM2RoadLinearReferenceProperty> partialChangeProperties = new ArrayList<>();
    if (emissionSource.getPartialChanges() != null && !emissionSource.getPartialChanges().isEmpty()) {
      for (final SRM2LinearReference dynamicSegment : emissionSource.getPartialChanges()) {
        addSrm2PartialChanges(dynamicSegment, partialChangeProperties);
      }

    }
    return partialChangeProperties;
  }

  private void addSrm2PartialChanges(final SRM2LinearReference dynamicSegment,
      final List<SRM2RoadLinearReferenceProperty> partialChangeProperties) {
    final SRM2RoadLinearReference dynamicSegmentGML = new SRM2RoadLinearReference();

    dynamicSegmentGML.setFromPosition(dynamicSegment.getFromPosition());
    dynamicSegmentGML.setToPosition(dynamicSegment.getToPosition());
    dynamicSegmentGML.setFreeway(dynamicSegment.getFreeway());
    dynamicSegmentGML.setTunnelFactor(dynamicSegment.getTunnelFactor());
    dynamicSegmentGML.setElevation(dynamicSegment.getElevation());
    dynamicSegmentGML.setElevationHeight(dynamicSegment.getElevationHeight());

    if (dynamicSegment.getBarrierLeft() != null) {
      dynamicSegmentGML.setBarrierLeft(toGMLRoadSideBarrier(dynamicSegment.getBarrierLeft()));
    }

    if (dynamicSegment.getBarrierRight() != null) {
      dynamicSegmentGML.setBarrierRight(toGMLRoadSideBarrier(dynamicSegment.getBarrierRight()));
    }

    partialChangeProperties.add(new SRM2RoadLinearReferenceProperty(dynamicSegmentGML));
  }

  private List<SRM1RoadLinearReferenceProperty> getSrm1DynamicSegments(final SRM1RoadEmissionSource emissionSource) {
    final List<SRM1RoadLinearReferenceProperty> partialChangeProperties = new ArrayList<>();
    if (emissionSource.getPartialChanges() != null && !emissionSource.getPartialChanges().isEmpty()) {
      for (final SRM1LinearReference dynamicSegment : emissionSource.getPartialChanges()) {
        addSrm1PartialChanges(dynamicSegment, partialChangeProperties);
      }

    }
    return partialChangeProperties;
  }

  private void addSrm1PartialChanges(final SRM1LinearReference dynamicSegment,
      final List<SRM1RoadLinearReferenceProperty> partialChangeProperties) {
    final SRM1RoadLinearReference dynamicSegmentGML = new SRM1RoadLinearReference();

    dynamicSegmentGML.setFromPosition(dynamicSegment.getFromPosition());
    dynamicSegmentGML.setToPosition(dynamicSegment.getToPosition());
    dynamicSegmentGML.setTunnelFactor(dynamicSegment.getTunnelFactor());

    partialChangeProperties.add(new SRM1RoadLinearReferenceProperty(dynamicSegmentGML));
  }

}
