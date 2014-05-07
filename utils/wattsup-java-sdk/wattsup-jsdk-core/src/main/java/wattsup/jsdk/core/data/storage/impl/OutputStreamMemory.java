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
package wattsup.jsdk.core.data.storage.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.core.data.storage.Memory;
import wattsup.jsdk.core.serialize.Serializer;

public class OutputStreamMemory<T> implements Memory<T>
{
    /**
     * 
     */
    private static final Logger LOG = Logger.getLogger(OutputStreamMemory.class.getName());

    /**
     * 
     */
    private final OutputStream out_;

    /**
     * 
     */
    private final Serializer serializer_;

    /**
     * The number of bytes written onto the {@link OutputStream}.
     */
    private volatile int bytesWritten;

    /**
     * 
     * @param serializer
     *            The serializer to serialize the values.
     * @param out
     *            {@link OutputStream} to write the data.
     */
    public OutputStreamMemory(Serializer serializer, OutputStream out)
    {
        this.out_ = out;
        this.serializer_ = serializer;
    }

    @Override
    public void clear()
    {
    }

    @Override
    public void dump(OutputStream out, Serializer serializer) throws IOException
    {
        //see https://code.google.com/p/io-tools/
    }

    @Override
    public synchronized void put(ID id, T data)
    {
        try
        {
            bytesWritten += this.serializer_.serialize(out_, (Serializable) data);
        }
        catch (IOException exception)
        {
            LOG.log(Level.SEVERE, exception.getMessage(), exception);
        }
    }

    @Override
    public synchronized int size()
    {
        return bytesWritten;
    }

    @Override
    public Collection<T> values()
    {
        return Collections.emptyList();
    }
}
