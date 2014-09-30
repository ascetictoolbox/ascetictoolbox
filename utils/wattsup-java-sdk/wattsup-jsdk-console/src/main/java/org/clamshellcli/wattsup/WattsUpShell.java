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

import java.io.File;

import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.Shell;

public class WattsUpShell implements Shell
{
    @Override
    public void exec(Context ctx)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void plug(Context plug)
    {
        IOConsole console = plug.getIoConsole();
        if (console == null)
        {
            throw new RuntimeException(String.format(
                    "%nUnable to find required IOConsole component in" + " plugins directory [%s]." + "Exiting...%n",
                    ((File) plug.getValue(Configurator.KEY_CONFIG_PLUGINSDIR)).getName()));
        }
        // launch console
        console.plug(plug);
    }
}
