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
package nl.overheid.aerius.shared.domain.v2.characteristics;

public enum CharacteristicsType {
  OPS(Names.OPS);

  public static final class Names {
    public static final String OPS = "OPS";
    private Names() {}
  }

  private final String name;

  private CharacteristicsType(final String name) {
    this.name = name;
  }

  public static final CharacteristicsType safeValueOf(final String value) {
    CharacteristicsType correct = null;
    if (value != null) {
      for (final CharacteristicsType type : values()) {
        if (type.name.equalsIgnoreCase(value)) {
          correct = type;
        }
      }
    }
    return correct;
  }

  public String getName() {
    return name;
  }
}
