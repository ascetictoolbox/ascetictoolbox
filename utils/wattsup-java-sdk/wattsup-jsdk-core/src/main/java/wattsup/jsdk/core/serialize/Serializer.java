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
package wattsup.jsdk.core.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public interface Serializer
{
    /**
     * Serializes an object to a specified format.
     * 
     * @param out
     *            to write the data.
     * @param value
     *            Object to serialize.
     * @return The amount of data wrote into the output.
     * @throws IOException
     *             if an I/O error occurs.
     */
    int serialize(OutputStream out, Serializable value) throws IOException;

    /**
     * Reads an object from an {@link InputStream}.
     * 
     * @param in
     *            to read serialized data from.
     * @param available
     *            how many bytes are available in DataInput for reading, may be -1 (in streams) or 0 (<code>null</code>).
     * @param <A>
     *            type of the deserialized object.
     * @return deserialized object.
     * @throws IOException
     *             if an I/O error occurs.
     */
    <A extends Serializable> A deserialize(InputStream in, int available) throws IOException;
}
