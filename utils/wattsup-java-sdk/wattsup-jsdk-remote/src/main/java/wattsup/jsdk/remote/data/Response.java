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

import wattsup.jsdk.core.data.ID;

public class Response implements Serializable
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = -3731516867482319587L;

    /**
     * Response's id.
     */
    private ID id_;

    /**
     * The response's data.
     */
    private Serializable data_;

    /**
     * Response's time.
     */
    private long time_;

    /**
     * Creates a new {@link Response} instance with a given ID.
     * 
     * @param id
     *            Response'id. Might not be <code>null</code>.
     */
    public Response(ID id)
    {
        this();
        this.id_ = id;
    }

    /**
     * Default constructor.
     */
    public Response()
    {
        super();
        this.time_ = System.currentTimeMillis();
    }

    /**
     * Creates a {@link Response} with a given ID.
     * 
     * @param id
     *            Response's id. Might not be <code>null</code>.
     * @return This response with the given ID.
     */
    public static Response newResponse(ID id)
    {
        return new Response(id);
    }

    /**
     * 
     * @param id
     *            Response's id. Might not be <code>null</code>.
     * @return This response with the new ID value.
     */
    public Response withId(ID id)
    {
        this.id_ = id;
        return this;
    }

    /**
     * 
     * @param data
     *            Response's data to assign.
     * @return This response with the new data value.
     */
    public Response withData(Serializable data)
    {
        this.data_ = data;
        return this;
    }

    /**
     * 
     * @param time
     *            Response's time value.
     * @return This instance with the new time.
     */
    public Response withTime(long time)
    {
        this.time_ = time;
        return this;
    }

    /**
     * @return the id
     */
    public ID getId()
    {
        return id_;
    }

    /**
     * @return the data
     * @param <T>
     *            The data's type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T getData()
    {
        return (T) data_;
    }

    /**
     * @return the response's time.
     */
    public long getTime()
    {
        return time_;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id_ == null) ? 0 : id_.hashCode());
        return result;
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
        Response other = (Response) obj;
        if (id_ == null)
        {
            if (other.id_ != null)
            {
                return false;
            }
        }
        else if (!id_.equals(other.id_))
        {
            return false;
        }
        return true;
    }

}
