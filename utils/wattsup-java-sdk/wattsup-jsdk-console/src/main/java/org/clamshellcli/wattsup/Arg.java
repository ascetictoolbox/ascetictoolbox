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

import java.io.Serializable;

public class Arg implements Serializable
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = -4697235984142020158L;

    /**
     * The command parameter's name.
     */
    private final String name_;

    /**
     * The command parameter's description.
     */
    private String description_;

    /**
     * The value of this argument.
     */
    private String value_;

    /**
     * 
     * @param name
     *            The argument name. Might not be <code>null</code>.
     */
    public Arg(String name)
    {
        this.name_ = name;
    }

    /**
     * 
     * @param name
     *            The argument name. Might not be <code>null</code>.
     * @param description
     *            The argument description.
     */
    public Arg(String name, String description)
    {
        this(name);
        this.description_ = description;
    }

    /**
     * 
     * @param name
     *            The argument name. Might not be <code>null</code>.
     * @param description
     *            The argument description.
     * @param value
     *            The value for this argument.
     */
    public Arg(String name, String description, String value)
    {
        this.name_ = name;
        this.description_ = description;
        this.value_ = value;
    }

    /**
     * @return the name_
     */
    public String getName()
    {
        return name_;
    }

    /**
     * @return the description_
     */
    public String getDescription()
    {
        return description_;
    }

    /**
     * @return the value_
     */
    public String getValue()
    {
        return value_;
    }

    /**
     * @param value
     *            the value_ to set
     */
    public void setValue(String value)
    {
        this.value_ = value;
    }
}
