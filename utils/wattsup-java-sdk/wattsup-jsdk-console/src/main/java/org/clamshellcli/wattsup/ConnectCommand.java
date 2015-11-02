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
package org.clamshellcli.wattsup;

import java.io.IOException;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;

import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.meter.WattsUp;

public class ConnectCommand implements Command
{
    
    /**
     * The key to get the Watts Up? device instance.
     */
    public static final String KEY_DEVICE_INSTANCE = "wattsUpInstance";
    
    /**
     * The required argument for this command.
     */
    private static final Arg PORT = new Arg("port", "The serial port name to connect to device. Example: /dev/ttyUSB0 or COMM3.");
    /**
     * The {@link Descriptor} for this {@link Command}.
     */
    private static final CommandDescriptor DESCRIPTOR = new CommandDescriptor("connect", "Connects to the power meter device.",
            "connect [port:<Serial Port name>]", PORT);
    
    @Override
    public void plug(Context ctx)
    {
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Context ctx)
    {
        WattsUp wattsUp = null;

        if (ctx.getValue(KEY_DEVICE_INSTANCE) == null)
        {
            Map<String, Arg> args = (Map<String, Arg>) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);

            Arg port = args != null ? args.get(PORT.getName()) : null;

            if (port != null)
            {
                wattsUp = new WattsUp(new WattsUpConfig().withPort(port.getValue()));

                try
                {
                    wattsUp.connect();
                    ctx.putValue(KEY_DEVICE_INSTANCE, wattsUp);
                }
                catch (IOException exception)
                {
                    ctx.getIoConsole().writeOutput(exception.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public Descriptor getDescriptor()
    {
        return DESCRIPTOR;
    }
}
