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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.core.AnInputController;
import org.clamshellcli.core.ShellException;

public class WattsUpController extends AnInputController
{
    /**
     * The commands founds by this controller.
     */
    private Map<String, Command> commands_;

//    /**
//     * The library to work with JSON format.
//     */
//    private Gson gson_;

    @Override
    public boolean handle(Context ctx)
    {
        boolean handled = false;
        String cmdLine = (String) ctx.getValue(Context.KEY_COMMAND_LINE_INPUT);
        if (cmdLine != null && !cmdLine.trim().isEmpty())
        {
            String[] tokens = cmdLine.split("\\s+");
            String cmdName = tokens[0];
            Map<String, Arg> argsMap = new LinkedHashMap<String, Arg>();

            // Are there arguments?
            if (tokens.length > 1)
            {
                for (int i = 1; i < tokens.length; i += 2)
                {
                    Arg arg = new Arg(tokens[i]);
                    if (i < tokens.length - 1)
                    {
                        arg.setValue(tokens[i + 1]);
                    }
                    argsMap.put(arg.getName(), arg);
                }
            }

            Command cmd = commands_ != null ? commands_.get(cmdName) : null;

            if (cmd != null)
            {
                ctx.putValue(Context.KEY_COMMAND_LINE_ARGS, argsMap);
                try
                {
                    cmd.execute(ctx);
                }
                catch (ShellException se)
                {
                    ctx.getIoConsole().writeOutput(String.format("%n%s%n%n", se.getMessage()));
                }
                handled = true;
            }
            else
            {
                handled = false;
            }

        }
        return handled;
    }

    @Override
    public void plug(Context plug)
    {
        super.plug(plug);
//        gson_ = new GsonBuilder().create();

        List<Command> wattsUpsCommands = plug.getCommandsByNamespace("wattsup");

        if (!wattsUpsCommands.isEmpty())
        {
            commands_ = plug.mapCommands(wattsUpsCommands);

            Set<String> cmdHints = new TreeSet<String>();

            // plug each Command instance and collect input hints
            for (Command cmd : wattsUpsCommands)
            {
                cmd.plug(plug);
                cmdHints.addAll(collectInputHints(cmd));
            }

            // save expected command input hints
            setExpectedInputs(cmdHints.toArray(new String[0]));
        }
        else
        {
            plug.getIoConsole().writeOutput(String.format("%nNo commands were found for input controller" + " [%s].%n%n", this.getClass().getName()));
        }
    }
}
