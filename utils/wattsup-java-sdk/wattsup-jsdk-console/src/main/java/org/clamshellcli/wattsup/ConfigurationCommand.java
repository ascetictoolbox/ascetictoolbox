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

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;

public class ConfigurationCommand implements Command
{
    /**
     * 
     */
    private static final Descriptor COMMAND_DESCRIPTOR = new CommandDescriptor("config", "Define or read the parameter of the device.",
            "config [port:<Serial Port name>]", new Arg("s", "Shows the parameters of the meter."), new Arg("d",
                    "Defines the parameters of the meter."));

    @Override
    public void plug(Context ctx)
    {
    }

    @Override
    public Object execute(Context ctx)
    {
        return null;
    }

    @Override
    public Descriptor getDescriptor()
    {
        return COMMAND_DESCRIPTOR;
    }
}
