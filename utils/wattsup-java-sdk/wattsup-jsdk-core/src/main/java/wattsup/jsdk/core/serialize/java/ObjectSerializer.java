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
package wattsup.jsdk.core.serialize.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import wattsup.jsdk.core.serialize.Serializer;

public final class ObjectSerializer implements Serializer
{
    @Override
    public int serialize(OutputStream out, Serializable value) throws IOException
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream out2 = new ObjectOutputStream(baos))
        {
            out2.writeObject(value);
            byte[] b = baos.toByteArray();

            out.write(b);
            return b.length;
        }
    }

    /**
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
    @SuppressWarnings("unchecked")
    @Override
    public <A extends Serializable> A deserialize(InputStream in, int available) throws IOException
    {
        try
        {
            return (A) new ObjectInputStream(in).readObject();
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new IOException(cnfe);
        }
    }
}
