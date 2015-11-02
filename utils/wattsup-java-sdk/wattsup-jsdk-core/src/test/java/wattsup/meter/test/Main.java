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
package wattsup.meter.test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.event.WattsUpDataAvailableEvent;
import wattsup.jsdk.core.listener.WattsUpDataAvailableListener;
import wattsup.jsdk.core.meter.WattsUp;

public final class Main
{
    /**
     * Private constructor to avoid instance of this class.
     */
    private Main()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an {@link WattsUp} for monitoring during one minute.
     * 
     * @param args
     *            The reference to the arguments.
     * @throws IOException
     *             If the power meter is not connected.
     */
    public static void main(String[] args) throws IOException
    {
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        final WattsUp meter = new WattsUp(new WattsUpConfig().withPort(args[0]).scheduleDuration(60));

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
