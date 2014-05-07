package eu.ascetic.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.event.WattsUpDataAvailableEvent;
import wattsup.jsdk.core.listener.WattsUpDataAvailableListener;
import wattsup.jsdk.core.meter.WattsUp;

public final class WattsUpTest
{       
    /**
     * Creates an {@link WattsUp} for monitoring during three minutes.
     * 
     * @param args
     *            The reference to the arguments.
     * @throws IOException
     *             If the power meter is not connected.
     */
    public static void main(String[] args) throws IOException
    {
    final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    final WattsUp meter = new WattsUp(new WattsUpConfig().withPort(args[0]).scheduleDuration(3 * 60));

    meter.registerListener(new WattsUpDataAvailableListener()
    {
        @Override
        public void processDataAvailable(final WattsUpDataAvailableEvent event)
        {
            WattsUpPacket[] values = event.getValue();
            System.out.printf("[%s] %s\n", format.format(new Date()), Arrays.toString(values));
        }
    });
    meter.connect();
    }
}