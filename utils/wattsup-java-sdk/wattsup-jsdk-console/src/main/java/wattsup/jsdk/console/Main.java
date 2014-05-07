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
package wattsup.jsdk.console;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.listener.impl.DefaultWattsUpDataAvailableListener;
import wattsup.jsdk.core.meter.WattsUp;

public final class Main
{

    /**
     * Constructor.
     */
    private Main()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @param args
     *            The serial port where the meter is connected.
     * @throws IOException
     *             If the meter is not connected or if it's impossible to connect to it.
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length == 0)
        {
            throw new IllegalArgumentException("The the serial port is required!");
        }

        WattsUp meter = new WattsUp(new WattsUpConfig().withPort(args[0]).scheduleDuration(
                Integer.valueOf(System.getProperty("measure.duration", "0"))));

        OutputStream out = System.out;

        final String exportFilePath = System.getProperty("export.file.path");
        if (exportFilePath != null && !exportFilePath.isEmpty())
        {
            out = new FileOutputStream(new File(exportFilePath));
        }

        meter.registerListener(new WattsUpDisconnectListener()
        {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event)
            {
                System.exit(0);
            }
        });

        meter.registerListener(new DefaultWattsUpDataAvailableListener());
        meter.connect();
    }
}
