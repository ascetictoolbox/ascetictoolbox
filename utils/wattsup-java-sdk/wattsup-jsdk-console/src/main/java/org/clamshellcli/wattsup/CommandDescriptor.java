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

import java.util.HashMap;
import java.util.Map;

import org.clamshellcli.api.Command;

public class CommandDescriptor implements Command.Descriptor
{
    /**
     * 
     */
    public static final String NAMESPACE = "wattsup";

    /**
     * The command arguments.
     */
    private final Map<String, String> arguments_ = new HashMap<>();

    /**
     * The command's description.
     */
    private String description_;

    /**
     * The command's name.
     */
    private String name_;

    /**
     * The command's usage.
     */
    private String usage_;

    /**
     * 
     * @param name
     *            The command name. Might not be <code>null</code> or empty.
     * @param description
     *            This command description. It's useful to user.
     * @param usage
     *            The command usage.
     */
    public CommandDescriptor(String name, String description, String usage)
    {
        this.description_ = description;
        this.name_ = name;
        this.usage_ = usage;
    }

    /**
     * 
     * @param name
     *            The command name. Might not be <code>null</code> or empty.
     * @param description
     *            This command description. It's useful to user.
     * @param usage
     *            The command usage.
     * @param args
     *            The command's arguments.
     */
    public CommandDescriptor(String name, String description, String usage, Arg... args)
    {
        this(name, description, usage);

        for (Arg arg : args)
        {
            this.arguments_.put(arg.getName(), arg.getName());
        }
    }

    @Override
    public Map<String, String> getArguments()
    {
        return this.arguments_;
    }

    @Override
    public String getDescription()
    {
        return this.description_;
    }

    @Override
    public String getName()
    {
        return this.name_;
    }

    @Override
    public String getNamespace()
    {
        return NAMESPACE;
    }

    @Override
    public String getUsage()
    {
        return this.usage_;
    }
}
