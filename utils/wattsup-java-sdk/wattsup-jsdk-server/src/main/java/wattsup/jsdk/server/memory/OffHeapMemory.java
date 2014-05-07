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
package wattsup.jsdk.server.memory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.core.data.storage.Memory;
import wattsup.jsdk.core.data.storage.impl.RamMemory;
import wattsup.jsdk.core.serialize.Serializer;

public class OffHeapMemory<T> implements Memory<T>
{
    /**
     * Global memory region name.
     */
    private static final String GLOBAL_MEMORY_REGION = "GLOBAL_MEMORY";

    /**
     * 
     */
    private final DB db_;

    /**
     * 
     */
    private final Map<String, Memory<T>> regions_ = new ConcurrentHashMap<String, Memory<T>>();

    /**
     * Creates a new Off-heap memory.
     */
    public OffHeapMemory()
    {
        db_ = DBMaker.newDirectMemoryDB().transactionDisable().asyncFlushDelay(100).compressionEnable().make();
    }

    @Override
    public synchronized void clear()
    {
        for (String region : this.regions_.keySet())
        {
            this.regions_.get(region).clear();
            this.db_.delete(region);
        }

        regions_.clear();
    }

    @Override
    public void dump(OutputStream out, Serializer serializer) throws IOException
    {
        for (Memory<T> memory : this.regions_.values())
        {
            memory.dump(out, serializer);
        }
    }

    @Override
    public void put(ID id, T data)
    {
        if (id != null && data != null)
        {
            this.getRegion(GLOBAL_MEMORY_REGION).put(id, data);
        }
    }

    @Override
    public synchronized int size()
    {
        int size = 0;
        for (Memory<T> memory : this.regions_.values())
        {
            size += memory.size();
        }

        return size;
    }

    @Override
    public synchronized Collection<T> values()
    {
        List<T> values = new LinkedList<>();

        for (Memory<T> memory : this.regions_.values())
        {
            values.addAll(memory.values());
        }

        return values;
    }

    /**
     * Returns a {@link Memory} region with the given name. It creates one if it does not exist.
     * 
     * @param name
     *            The region name. Might not be <code>null</code>.
     * @return Memory space allocated with the given name.
     */
    public synchronized Memory<T> getRegion(String name)
    {
        if (name == null)
        {
            throw new NullPointerException("Region name is null!");
        }

        Memory<T> memory = this.regions_.get(name);

        if (memory == null)
        {
            memory = new RamMemory<T>(this.db_.<ID, T> getTreeMap(name));
            this.regions_.put(name, memory);
        }

        return memory;
    }

    public synchronized void freeRegion(String name)
    {
        this.regions_.remove(name);
        this.db_.delete(name);
    }

}
