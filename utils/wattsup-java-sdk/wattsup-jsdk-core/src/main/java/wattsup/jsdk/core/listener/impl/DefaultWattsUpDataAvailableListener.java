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
package wattsup.jsdk.core.listener.impl;

import java.util.Objects;

import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.data.storage.Memory;
import wattsup.jsdk.core.data.storage.impl.RamMemory;
import wattsup.jsdk.core.event.WattsUpDataAvailableEvent;
import wattsup.jsdk.core.listener.WattsUpDataAvailableListener;

public class DefaultWattsUpDataAvailableListener implements WattsUpDataAvailableListener
{
    /**
     * Memory instance to store the data.
     */
    private final Memory<WattsUpPacket> memory_;

    /**
     * @param memory
     *            Memory instance to store the measurements. Might not be <code>null</code>.
     */
    public DefaultWattsUpDataAvailableListener(Memory<WattsUpPacket> memory)
    {
        this.memory_ = Objects.requireNonNull(memory);
    }

    /**
     * Creates a new {@link DefaultWattsUpDataAvailableListener} instance storing all data into the RAM memory.
     */
    public DefaultWattsUpDataAvailableListener()
    {
        this(new RamMemory<WattsUpPacket>(Integer.MAX_VALUE));
    }

    @Override
    public void processDataAvailable(WattsUpDataAvailableEvent event)
    {
        for (WattsUpPacket value : event.getValue())
        {
            this.memory_.put(value.getId(), value);
        }
    }
}
