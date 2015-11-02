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
package wattsup.jsdk.server.app;

import java.io.IOException;

import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.meter.WattsUp;
import wattsup.jsdk.server.WattsUpServer;

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
     * Starts a server bound to the port given the system property <em>wattsup.server.port</em> or in the default port: 9090.
     * 
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

        final int port = Integer.valueOf(System.getProperty("wattsup.server.port", "9090"));
        final WattsUpServer server = new WattsUpServer(port, meter);

        meter.registerListener(new WattsUpDisconnectListener()
        {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event)
            {
                server.stop();
                System.exit(0);
            }
        });

        new Thread(server).start();
        meter.connect();
    }
}
