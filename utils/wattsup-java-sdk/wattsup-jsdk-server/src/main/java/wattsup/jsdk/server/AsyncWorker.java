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
package wattsup.jsdk.server;

import java.util.HashMap;
import java.util.Map;

import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.data.storage.Memory;
import wattsup.jsdk.core.listener.impl.DefaultWattsUpDataAvailableListener;
import wattsup.jsdk.core.meter.WattsUp;

public final class AsyncWorker extends AbstractWorker
{
    /**
     * Reference for the WattsUp server storage.
     */
    private final Memory<WattsUpPacket> memory_;

    /**
     * @param id
     *            This worker id.
     * @param wattsUp
     *            The reference to the {@link WattsUp} to read the measurements.
     * @param storage
     *            Reference for the WattsUp storage.
     */
    public AsyncWorker(ID id, WattsUp wattsUp, Memory<WattsUpPacket> memory)
    {
        super(id, wattsUp, new DefaultWattsUpDataAvailableListener(memory));
        this.memory_ = memory;
    }

    @Override
    public Map<ID, WattsUpPacket> getData()
    {
        Map<ID, WattsUpPacket> data = new HashMap<>();

        for (WattsUpPacket packet : this.memory_.values())
        {
            data.put(packet.getId(), packet);
        }

        return data;
    }
}
