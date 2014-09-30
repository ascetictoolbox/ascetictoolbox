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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import wattsup.jsdk.core.convert.MeasurementToWattsUpPacketConverter;
import wattsup.jsdk.core.convert.WattsUpPacketToMeasurementConverter;
import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.core.data.Measurement;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.data.storage.Memory;
import wattsup.jsdk.core.data.storage.database.MeasurementDao;
import wattsup.jsdk.core.serialize.Serializer;

public final class DatabaseMemory implements Memory<WattsUpPacket>
{
    /**
     * 
     */
    private final static WattsUpPacketToMeasurementConverter WATTSUP_TO_MEASUREMENT_CONVERTER = new WattsUpPacketToMeasurementConverter();

    private final static MeasurementToWattsUpPacketConverter MEASUREMENT_TO_WATTSUP_PACKET_CONVERTER = new MeasurementToWattsUpPacketConverter();

    /**
     * The object to persist the data into the database.
     */
    private final MeasurementDao measurementDAO_;

    /**
     * Creates a {@link DatabaseMemory} configuring the database instance to use.
     * 
     * @param measurementDao
     *            {@link MeasurementDao}'s instance. Might not be <code>null</code>.
     */
    public DatabaseMemory(MeasurementDao measurementDao)
    {
        this.measurementDAO_ = measurementDao;
    }

    /**
     * The time of the first measurement.
     */
    private volatile Long startTime_;

    @Override
    public void clear()
    {
        this.measurementDAO_.deleteInInterval(this.startTime_, System.currentTimeMillis());
    }

    @Override
    public void dump(OutputStream out, Serializer serializer) throws IOException
    {
        serializer.serialize(out, (Serializable) values());
    }

    @Override
    public synchronized void put(ID id, WattsUpPacket data)
    {
        if (this.startTime_ == null)
        {
            this.startTime_ = data.getTime();
        }

        this.measurementDAO_.insert(convert(data));
    }

    @Override
    public int size()
    {
        return this.measurementDAO_.countInInterval(this.startTime_, System.currentTimeMillis());
    }

    @Override
    public Collection<WattsUpPacket> values()
    {
        List<WattsUpPacket> values = new ArrayList<>();

        for (Measurement measurement : this.measurementDAO_.findInInterval(this.startTime_, System.currentTimeMillis()))
        {
            values.add(convert(measurement));
        }

        return Collections.unmodifiableList(values);
    }

    /**
     * Converts a {@link WattsUpPacket} object into a {@link Measurement} object.
     * 
     * @param data
     *            The {@link WattsUpPacket} to be converted to {@link Measurement}.
     * @return A {@link Measurement} object with the state of the {@link WattsUpPacket}.
     */
    private Measurement convert(WattsUpPacket data)
    {
        return WATTSUP_TO_MEASUREMENT_CONVERTER.convert(data);
    }

    /**
     * Converts a {@link Measurement} object into a {@link WattsUpPacket} object.
     * 
     * @param measurement
     *            The {@link Measurement} to be converted to {@link WattsUpPacket}.
     * @return A {@link WattsUpPacket} object with the state of the {@link Measurement}.
     */
    private WattsUpPacket convert(Measurement measurement)
    {
        return MEASUREMENT_TO_WATTSUP_PACKET_CONVERTER.convert(measurement);
    }

}
