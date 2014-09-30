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

import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.data.storage.impl.OutputStreamMemory;
import wattsup.jsdk.core.listener.impl.DefaultWattsUpDataAvailableListener;
import wattsup.jsdk.core.meter.WattsUp;
import wattsup.jsdk.core.serialize.csv.CsvWattsUpPacketSerializer;

public final class SyncWorker extends AbstractWorker
{
    /**
     * @param id
     *            This worker id.
     * @param wattsUp
     *            The reference to the {@link WattsUp} to read the measurements.
     * @param out
     *            Output to write the measurements.
     */
    public SyncWorker(ID id, WattsUp wattsUp, OutputStream out)
    {
        super(id, wattsUp, new DefaultWattsUpDataAvailableListener(new OutputStreamMemory<WattsUpPacket>(new CsvWattsUpPacketSerializer(), out)));
    }

    @Override
    public Map<ID, WattsUpPacket> getData()
    {
        return Collections.emptyMap();
    }
}
