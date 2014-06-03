package eu.ascetic.utils;

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

public final class WattsUpTest {

    private static WattsUp meter;
    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    /**
     * Connects to a WattsUp meter and outputs continously
     * the values recorded, to standard out.
     *
     * @param args The first argument should be the comp port in use.
     * The default is "COM9".
     * @throws IOException If the power meter is not connected.
     */
    public static void main(String[] args) throws IOException {

        String port = "COM9";
        if (args.length > 0) {
            port = args[0];
        }
        //Note: negative numbers for the schedule duration makes it run forever
        meter = new WattsUp(new WattsUpConfig().withPort(port).scheduleDuration(-1).withInternalLoggingInterval(1).withExternalLoggingInterval(1));
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

                System.out.println("Device: The current WattMeter Device");
                System.out.println("Date: " + format.format(new Date()));
                System.out.println("Watts: " + watts);
                System.out.println("Volts: " + volts);
                System.out.println("Amps: " + amps);
                System.out.print("\n\r");
            }
        });

        meter.registerListener(new WattsUpMemoryCleanListener() {
            @Override
            public void processWattsUpReset(WattsUpMemoryCleanEvent event) {
                System.out.println("Memory Just Cleaned");
            }
        });

        meter.registerListener(new WattsUpStopLoggingListener() {
            @Override
            public void processStopLogging(WattsUpStopLoggingEvent event) {
                System.out.println("Logging Stopped");
            }
        });

        meter.registerListener(new WattsUpDisconnectListener() {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event) {
                System.out.println("Application Exiting Due to Disconnect");
                System.exit(0);
            }
        });

        System.out.println("WattsUp Meter Connecting");
        meter.connect(true);
        meter.setLoggingModeSerial(1);
        System.out.println("WattsUp Meter Connected " + meter.isConnected());
    }

    private static double changeOrderOfMagnitude(String str, int position) {
        double answer = Double.valueOf(str);
        if (position > 0) {
            answer = answer / Math.pow(10, position);
        }
        return answer;
    }
}