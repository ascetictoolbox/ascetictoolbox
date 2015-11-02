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
package wattsup.jsdk.core.data.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.core.serialize.Serializer;

public interface Memory<T>
{
    /**
     * Clear this memory removing all entries.
     */
    void clear();

    /**
     * Dumps the memory into the output without clean it.
     * 
     * @param out
     *            An {@link OutputStream} instance to write the data. Might not be <code>null</code>.
     * @param serializer
     *            A {@link Serializer} instance to convert from the stored type (T) to a serializable format. Might not be <code>null</code>.
     * @throws IOException
     *             If an I/O error occurs.
     */
    void dump(OutputStream out, Serializer serializer) throws IOException;

    /**
     * Store the data into memory.
     * 
     * @param id
     *            The ID to identify the data.
     * @param data
     *            The data to store. Null values are not stored.
     */
    void put(ID id, T data);

    /**
     * Returns the memory size.
     * 
     * @return The memory size.
     */
    int size();

    /**
     * Returns a dump of the memory with all values stored on it.
     * 
     * @return A read-only view of the entries in this memory.
     */
    Collection<T> values();

}
