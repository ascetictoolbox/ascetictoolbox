/**
 *     Copyright (C) 2013 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package wattsup.jsdk.core.convert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import wattsup.jsdk.core.data.Measurement;
import wattsup.jsdk.core.data.WattsUpPacket;

public class WattsUpPacketToMeasurementConverter implements Converter<WattsUpPacket, Measurement>
{
    @Override
    public Measurement convert(WattsUpPacket input)
    {
        final Measurement measurement = new Measurement();

        Map<String, Object> wattsupFields = input.toMap(true);

        for (Field f : measurement.getClass().getDeclaredFields())
        {
            if (Modifier.isStatic(f.getModifiers()))
            {
                continue;
            }
            
            f.setAccessible(true);

            try
            {
                f.set(measurement, wattsupFields.get(f.getName().replaceAll("_", "").toLowerCase()));
            }
            catch (IllegalArgumentException | IllegalAccessException exception)
            {
                throw new RuntimeException(exception.getMessage(), exception);
            }
        }

        return measurement;
    }
}
