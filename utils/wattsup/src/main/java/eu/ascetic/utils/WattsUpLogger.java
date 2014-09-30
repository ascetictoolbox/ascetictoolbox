/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import wattsup.jsdk.core.data.WattsUpPacket;

/**
 *
 * @author Richard
 */
public class WattsUpLogger extends GenericLogger<WattsUpPacket[]> {

    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");    
    
    public WattsUpLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }
    
    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Date");
        store.append("Watts");
        store.append("Volts");
        store.append("Amps");
    }

    @Override
    public void writebody(WattsUpPacket[] item, ResultsStore store) {
                String watts = item[0].get("watts").getValue();
                String volts = item[0].get("volts").getValue();
                String amps = item[0].get("amps").getValue();
                watts = "" + changeOrderOfMagnitude(watts, 1);
                volts = "" + changeOrderOfMagnitude(volts, 1);
                amps = "" + changeOrderOfMagnitude(amps, 3);

                store.add(format.format(new Date()));
                store.append(watts);
                store.append(volts);
                store.append(amps);
    }
    
    private static double changeOrderOfMagnitude(String str, int position) {
        double answer = Double.valueOf(str);
        if (position > 0) {
            answer = answer / Math.pow(10, position);
        }
        return answer;
    }    
    
}
