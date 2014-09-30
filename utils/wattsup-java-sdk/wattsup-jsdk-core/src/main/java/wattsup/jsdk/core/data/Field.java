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

public final class Field implements Cloneable, java.io.Serializable
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 5626553339380120311L;

    /**
     * The name of the field. Might not be <code>null</code> or empty.
     */
    private final String name_;

    /**
     * The value of the field. Empty or <code>null</code> value is converted to zero.
     */
    private final String value_;

    /**
     * 
     * @param name
     *            The name of the this field. Might not be <code>null</code>.
     * @param value
     *            The value for this field.
     */
    public Field(final String name, final String value)
    {
        this.name_ = name;
        this.value_ = value;
    }

    /**
     * Creates an instance of this class copying the state of {@code other}.
     * 
     * @param other
     *            The instance to be cloned.
     */
    public Field(Field other)
    {
        this(other.name_, other.value_);
    }

    /**
     * Returns an instance of {@link Field} with the name and value.
     * 
     * @param name
     *            The name to be assigned to the field. Might not be <code>null</code>.
     * @param value
     *            The value to be assigned to the field.
     * @return An instance of {@link Field} with the name and value.
     */
    public static Field valueOf(String name, String value)
    {
        return new Field(name, value);
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value_;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name_;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name_ == null) ? 0 : name_.hashCode());
        result = prime * result + ((value_ == null) ? 0 : value_.hashCode());
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

        Field other = (Field) obj;
        if (name_ == null)
        {
            if (other.name_ != null)
            {
                return false;
            }
        }
        else if (!name_.equals(other.name_))
        {
            return false;
        }

        if (value_ == null)
        {
            if (other.value_ != null)
            {
                return false;
            }
        }
        else if (!value_.equals(other.value_))
        {
            return false;
        }

        return true;
    }

    @Override
    protected Field clone()
    {
        try
        {
            return (Field) super.clone();
        }
        catch (CloneNotSupportedException exception)
        {
            return new Field(this);
        }
    }

    @Override
    public String toString()
    {
        return String.format("%s = %5s", this.name_, value_);
    }
}
