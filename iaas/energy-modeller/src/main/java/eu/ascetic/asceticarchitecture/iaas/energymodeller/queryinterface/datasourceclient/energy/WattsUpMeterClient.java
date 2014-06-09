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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.energy;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.event.WattsUpDataAvailableEvent;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.event.WattsUpMemoryCleanEvent;
import wattsup.jsdk.core.event.WattsUpStopLoggingEvent;
import wattsup.jsdk.core.listener.WattsUpDataAvailableListener;
import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.listener.WattsUpMemoryCleanListener;
import wattsup.jsdk.core.listener.WattsUpStopLoggingListener;
import wattsup.jsdk.core.meter.WattsUp;

/**
 * A client that connects to an WattsUp meter data source for providing energy
 * readings of the infrastructure.
 *
 * @author Richard
 */
public class WattsUpMeterClient extends DefaultEnergyClient {

    private WattsUp meter;
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    public WattsUpMeterClient() throws IOException {
         this("COM9", -1, 1);
    }
    
    /**
     * This creates a new WattsUp Meter client, that is capable of taking
     * data from a WattsUp Meter, for use inside the ASCETiC architecture.
     * @param port The port to connect to
     * @param duration The duration to connect for (< 0 means forever)
     * @param interval The interval at which to take logging data.
     * @throws IOException 
     */
    public WattsUpMeterClient(String port, int duration, int interval) throws IOException {

        WattsUpConfig config = new WattsUpConfig().withPort(port).scheduleDuration(duration).withInternalLoggingInterval(interval).withExternalLoggingInterval(interval);
        meter = new WattsUp(config);
        System.out.println("WattsUp Meter Created");

        meter.registerListener(new WattsUpDataAvailableListener() {
            @Override
            public void processDataAvailable(final WattsUpDataAvailableEvent event) {
                WattsUpPacket[] values = event.getValue();
                //System.out.printf("[%s] %s\n", format.format(new Date()), Arrays.toString(values));
                String watts = values[0].get("watts").getValue();
                String volts = values[0].get("volts").getValue();
                String amps = values[0].get("amps").getValue();
                watts = "" + changeOrderOfMagnitude(watts, 1);
                volts = "" + changeOrderOfMagnitude(volts, 1);
                amps = "" + changeOrderOfMagnitude(amps, 3);

                //TODO change to make the output go to a DB etc
                // This is a list of likely data values that need storing.
                System.out.println("Device id: The current WattMeter Device");
                System.out.println("Date: " + format.format(new Date()));
                System.out.println("Watts: " + watts);
                System.out.println("Volts: " + volts);
                System.out.println("Amps: " + amps);
//                System.out.println("Power Ratio: " + Power Ratio);
                System.out.print("\n\r");
            }
        });

        meter.registerListener(new WattsUpMemoryCleanListener() {
            @Override
            public void processWattsUpReset(WattsUpMemoryCleanEvent event) {
                System.out.println("WattsUp Meter Memory Just Cleaned");
            }
        });

        meter.registerListener(new WattsUpStopLoggingListener() {
            @Override
            public void processStopLogging(WattsUpStopLoggingEvent event) {
                System.out.println("WattsUp Meter Logging Stopped");
            }
        });

        meter.registerListener(new WattsUpDisconnectListener() {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event) {
                System.out.println("WattsUp Meter Client Exiting");
            }
        });

        System.out.println("WattsUp Meter Connecting");
        meter.connect(true);
        meter.setLoggingModeSerial(1);
        System.out.println("WattsUp Meter Connected " + meter.isConnected());
    }

    /**
     * The output of a WattsUp? meter has no decimal places. This shifts
     * the output by the correct magnitude in order that the value makes sense.
     * @param meterOutput The output from the WattsUp? meter.
     * @param position The order of magnitude to reduce the size of the value by.
     * @return The double value of the meters output string.
     */
    private static double changeOrderOfMagnitude(String meterOutput, int position) {
        double answer = Double.valueOf(meterOutput);
        if (position > 0) {
            answer = answer / Math.pow(10, position);
        }
        return answer;
    }
}
