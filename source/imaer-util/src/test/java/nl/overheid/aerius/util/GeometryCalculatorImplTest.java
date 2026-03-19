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
package nl.overheid.aerius.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nl.overheid.aerius.shared.domain.v2.geojson.LineString;
import nl.overheid.aerius.shared.domain.v2.geojson.Point;
import nl.overheid.aerius.shared.domain.v2.geojson.Polygon;
import nl.overheid.aerius.shared.geometry.GeometryCalculator;

class GeometryCalculatorImplTest {

  @Test
  void testDetermineMeasureNullGeometry() {
    final GeometryCalculator calculator = new GeometryCalculatorImpl();
    assertEquals(0.0, calculator.determineMeasure(null));
  }

  @Test
  void testDetermineMeasurePoint() {
    final GeometryCalculator calculator = new GeometryCalculatorImpl();
    assertEquals(1.0, calculator.determineMeasure(new Point(432, 23)));
  }

  @Test
  void testDetermineMeasureLineString() {
    final GeometryCalculator calculator = new GeometryCalculatorImpl();
    final LineString lineString = new LineString();
    lineString.setCoordinates(new double[][] {{100, 100}, {100, 300}});
    assertEquals(200.0, calculator.determineMeasure(lineString));
  }

  @Test
  void testDetermineMeasureLineStringIncomplete() {
    final GeometryCalculator calculator = new GeometryCalculatorImpl();
    final LineString lineString = new LineString();
    lineString.setCoordinates(new double[][] {{100, 100}});
    assertEquals(0.0, calculator.determineMeasure(lineString));
  }

  @Test
  void testDetermineMeasurePolygon() {
    final GeometryCalculator calculator = new GeometryCalculatorImpl();
    final Polygon polygon = new Polygon();
    polygon.setCoordinates(new double[][][] {{{100, 100}, {100, 300}, {200, 300}, {200, 100}, {100, 100}}});
    assertEquals(200.0 * 100.0, calculator.determineMeasure(polygon));
  }

  @Test
  void testDetermineMeasurePolygonIncomplete() {
    final GeometryCalculator calculator = new GeometryCalculatorImpl();
    final Polygon polygon = new Polygon();
    polygon.setCoordinates(new double[][][] {{{100, 100}, {100, 300}}});
    assertEquals(0.0, calculator.determineMeasure(polygon));
  }

  @Test
  void testDetermineMeasurePolygonWithHole() {
    final GeometryCalculator calculator = new GeometryCalculatorImpl();
    final Polygon polygon = new Polygon();
    // Outer ring: 100x100 square = area 10000
    // Inner ring (hole): 50x50 square = area 2500
    // Net area: 7500
    polygon.setCoordinates(new double[][][] {
        {{0, 0}, {0, 100}, {100, 100}, {100, 0}, {0, 0}},
        {{25, 25}, {25, 75}, {75, 75}, {75, 25}, {25, 25}}
    });
    assertEquals(7500.0, calculator.determineMeasure(polygon));
  }

}
