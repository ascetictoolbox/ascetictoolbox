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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.core.data.storage.Memory;
import wattsup.jsdk.core.serialize.Serializer;

public final class RamMemory<T> implements Memory<T>
{
    /**
     * A thread-safe {@link Map}'s instance to store the measurements.
     */
    private final Map<ID, T> entriesMap_;

    /**
     * Creates a new {@link RamMemory} empty WattsUpMemory with the specified capacity.
     * 
     * @param initialCapacity
     *            The initial capacity.
     */
    public RamMemory(int initialCapacity)
    {
        this(new ConcurrentHashMap<ID, T>(initialCapacity));
    }

    /**
     * 
     * @param repository
     *            {@link Map} to store the data.
     */
    public RamMemory(Map<ID, T> repository)
    {
        this.entriesMap_ = Objects.requireNonNull(repository);
    }

    @Override
    public void clear()
    {
        this.entriesMap_.clear();
    }

    @Override
    public void dump(OutputStream out, Serializer serializer) throws IOException
    {
        for (T value : this.entriesMap_.values())
        {
            serializer.serialize(out, (Serializable) value);
        }
    }

    @Override
    public void put(ID id, T data)
    {
        if (id != null && data != null)
        {
            entriesMap_.put(id, data);
        }
    }

    @Override
    public int size()
    {
        return this.entriesMap_.size();
    }

    @Override
    public Collection<T> values()
    {
        return Collections.unmodifiableCollection(this.entriesMap_.values());
    }
}
