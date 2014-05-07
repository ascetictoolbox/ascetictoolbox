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
package wattsup.jsdk.remote.data;

import java.io.Serializable;
import java.util.Objects;

import wattsup.jsdk.core.data.ID;

public class Request implements Serializable
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 3037120095297869152L;

    /**
     * The request id. It must not be <code>null</code>.
     */
    private ID id_;

    /**
     * 
     */
    private CommandType command_;

    /**
     * 
     */
    private String name_;

    /**
     * 
     */
    private long time_;

    /**
     * Creates a {@link Request} instance and assigned it a given id.
     * 
     * @param id
     *            The ID to assign. Might not be <code>null</code>.
     */
    public Request(ID id)
    {
        this.id_ = Objects.requireNonNull(id);
        this.time_ = System.currentTimeMillis();
    }

    /**
     * Creates a {@link Request} instance and assigned it a random ID's value.
     */
    public Request()
    {
        this(ID.randomID());
    }

    /**
     * Returns a {@link Request} instance with an ID.
     * 
     * @return A {@link Request} instance with a not <code>null</code> ID.
     */
    public static Request newRequest()
    {
        return new Request();
    }

    /**
     * 
     * @param command
     *            The command to execute.
     * @return This instance with the new command value.
     */
    public Request withCommand(CommandType command)
    {
        this.command_ = command;
        return this;
    }

    /**
     * 
     * @param id
     *            This request's ID. Might not be <code>null</code>.
     * @return This instance with the new ID value.
     */
    public Request withId(ID id)
    {
        this.id_ = Objects.requireNonNull(id);
        return this;
    }

    /**
     * 
     * @param name
     *            The region name.
     * @return This instance with the new region name.
     */
    public Request withName(String name)
    {
        this.name_ = name;
        return this;
    }

    /**
     * 
     * @param time
     *            The request time in milliseconds.
     * 
     * @return This instance with the new request time.
     */
    public Request withTime(long time)
    {
        this.time_ = time;
        return this;
    }

    /**
     * @return the id
     */
    public final ID getId()
    {
        return id_;
    }

    /**
     * @return the command
     */
    public CommandType getCommand()
    {
        return command_;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name_;
    }

    /**
     * @return the time
     */
    public long getTime()
    {
        return time_;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        return prime + id_.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (getClass() != obj.getClass())
        {
            return false;
        }

        Request other = (Request) obj;
        return this.getId().equals(other.getId());
    }
}
