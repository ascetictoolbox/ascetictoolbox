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
package wattsup.jsdk.core.data;

import java.io.Serializable;
import java.util.UUID;

public final class ID implements Comparable<ID>, Serializable, Cloneable
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 756632588667280707L;

    /**
     * This ID value.
     */
    private final UUID value_;

    /**
     * The value of the hash code of this class, since this class is immutable.
     */
    private final int hashCode_;

    /**
     * Creates a new {@link ID}.
     * 
     * @param value
     *            The bytes that represents this ID. Might not be <code>null</code>.
     */
    public ID(UUID value)
    {
        this.value_ = value;
        this.hashCode_ = value.hashCode();
    }

    /**
     * Creates a new {@link ID} converting a {@link Long} value into a {@link UUID}.
     * 
     * @param value
     *            A {@link Long} that specifies a {@link ID}.
     */
    public ID(long value)
    {
        this(UUID.fromString(String.valueOf(value)));
    }

    /**
     * Creates a new ID with the same state of other ID.
     * 
     * @param other
     *            The ID to be copied. Might not be <code>null</code>.
     */
    public ID(ID other)
    {
        this(other.value_);
    }

    /**
     * 
     * @param value
     *            A {@link Long} that specifies a {@link ID}.
     * @return A {@link ID} with the given value.
     */
    public static ID fromLong(long value)
    {
        return new ID(UUID.nameUUIDFromBytes(Long.toHexString(value).getBytes()));
    }

    /**
     * Creates a {@link ID} from the {@link String} standard representation as described in the toString() method.
     * 
     * @param value
     *            A {@link String} that specifies a {@link ID}.
     * @return A {@link ID} with the given value.
     */
    public static ID fromString(String value)
    {
        return new ID(UUID.fromString(value));
    }

    /**
     * Static factory to create an {@link ID} using pseudo random number generator.
     * 
     * @return An {@link ID} generated with pseudo random number generator.
     */
    public static ID randomID()
    {
        return new ID(UUID.randomUUID());
    }

    @Override
    public int compareTo(ID other)
    {
        return this.value_.compareTo(other.value_);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (!(obj instanceof ID))
        {
            return false;
        }

        return this.compareTo((ID) obj) == 0;
    }

    @Override
    public int hashCode()
    {
        return this.hashCode_;
    }

    @Override
    public ID clone()
    {
        Object cloned;
        try
        {
            cloned = super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            cloned = new ID(this.value_);
        }

        return (ID) cloned;
    }

    @Override
    public String toString()
    {
        return this.value_.toString();
    }
}
