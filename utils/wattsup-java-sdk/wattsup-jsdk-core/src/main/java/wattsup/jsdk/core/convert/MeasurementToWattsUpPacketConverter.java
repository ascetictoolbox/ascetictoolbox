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

import wattsup.jsdk.core.data.Measurement;
import wattsup.jsdk.core.data.WattsUpConfig.Delimiter;
import wattsup.jsdk.core.data.WattsUpPacket;

public class MeasurementToWattsUpPacketConverter implements Converter<Measurement, WattsUpPacket>
{
    @Override
    public WattsUpPacket convert(Measurement input)
    {
        /**
         * wattsup: [debug] Have 18 parameters (cmd = 'd')
         * wattsup: [debug] Packet - Command 'd' 18 parameters
         * wattsup: [debug] [ 0] [               watts] = "965"
         * wattsup: [debug] [ 1] [               volts] = "1204"
         * wattsup: [debug] [ 2] [                amps] = "792"
         * wattsup: [debug] [ 3] [          watt hours] = "1"
         * wattsup: [debug] [ 4] [                cost] = "0"
         * wattsup: [debug] [ 5] [             mo. kWh] = "69384"
         * wattsup: [debug] [ 6] [            mo. cost] = "5550"
         * wattsup: [debug] [ 7] [           max watts] = "965"
         * wattsup: [debug] [ 8] [           max volts] = "1205"
         * wattsup: [debug] [ 9] [            max amps] = "833"
         * wattsup: [debug] [10] [           min watts] = "965"
         * wattsup: [debug] [11] [           min volts] = "1203"
         * wattsup: [debug] [12] [            min amps] = "779"
         * wattsup: [debug] [13] [        power factor] = "100"
         * wattsup: [debug] [14] [          duty cycle] = "0"
         * wattsup: [debug] [15] [         power cycle] = "0"
         * wattsup: [debug] [16] [              (null)] = "500"
         * wattsup: [debug] [17] [              (null)] = "965"
         * 
         */
        
        //"#d,-,18,965,1204,792,1,0,69384,5550,965,1205,833,965,1203,779,100,0,0,500,965;";
        
        StringBuilder data = new StringBuilder("#d,-,");
        data.append((int)(input.getWatts() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getVolts() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getAmps() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getWattsKWh() * 10)).append(Delimiter.COMMA);
        data.append("0").append(Delimiter.COMMA); // cost
        data.append("0").append(Delimiter.COMMA); // mo. kWh
        data.append("0").append(Delimiter.COMMA); // mo. cost
        data.append((int)(input.getMaxWatts() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getMaxVolts() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getMaxAmps() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getMinWatts() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getMinVolts() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getMinAmps() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getPowerFactor() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getDutyCycle() * 10)).append(Delimiter.COMMA);
        data.append((int)(input.getPowerCycle() * 10)).append(Delimiter.COMMA);
        data.append("0").append(Delimiter.COMMA); // (null)
        data.append((int)(input.getWatts() * 10)).append(Delimiter.COMMA); // (null)
        data.append(Delimiter.SEMICOLON);
        
        return WattsUpPacket.parser(data.toString(), Delimiter.COMMA, input.getTime())[0];
    }

}
