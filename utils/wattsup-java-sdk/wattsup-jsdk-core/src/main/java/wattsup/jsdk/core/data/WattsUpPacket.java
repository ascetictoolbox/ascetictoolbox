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
package wattsup.jsdk.core.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import wattsup.jsdk.core.data.WattsUpConfig.Delimiter;

public final class WattsUpPacket implements Serializable, Comparable<WattsUpPacket>
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = -252864256406279128L;

    /**
     * The number of field defined by the power meter.
     */
    private static final int NUM_FIELDS = 18;

    /**
     * The delimiter of a record (line). This cannot be changed.
     */
    private static final Delimiter RECORD_DELIMITER = Delimiter.SEMICOLON;

    /**
     * The labels defined by the power meter.
     */
    private static final String[] LABELS = new String[NUM_FIELDS];

    /**
     * This packet id.
     */
    private final ID id_;

    /**
     * The command for the packet.
     */
    private String command_;

    /**
     * The sub-command for the packet.
     */
    private String subCommand_;

    /**
     * Meter's measurements.
     */
    private String data_;

    /**
     * The number of fields returned by the power meter. It can be less than the number of fields.
     */
    private int count_;

    /**
     * The time in milliseconds when the measurement was realized.
     */
    private long time_;

    /**
     * The fields available in this packet.
     */
    private final Field[] fields_ = new Field[NUM_FIELDS];

    static
    {
        LABELS[0] = "watts";
        LABELS[1] = "volts";
        LABELS[2] = "amps";
        LABELS[3] = "wattskwh";
        LABELS[4] = "cost";
        LABELS[5] = "mo. kWh";
        LABELS[6] = "mo. cost";
        LABELS[7] = "max watts";
        LABELS[8] = "max volts";
        LABELS[9] = "max amps";
        LABELS[10] = "min watts";
        LABELS[11] = "min volts";
        LABELS[12] = "min amps";
        LABELS[13] = "power factor";
        LABELS[14] = "duty cycle";
        LABELS[15] = "power cycle";
    }

    /**
     * Private constructor to avoid instance of this class outside of the method {@link #parser(String, Delimiter, long)}.
     * 
     * @param record
     *            The data as returned by the meter.
     * @param time
     *            The time that the data were read.
     */
    private WattsUpPacket(String record, long time)
    {
        this.data_ = record;
        this.time_ = time;
        this.id_ = ID.fromLong(time);
    }

    /**
     * Parses the meter's output and creates an instance of {@link WattsUpPacket} for each record read.
     * 
     * @param data
     *            Meter's output. Might not be <code>null</code>.
     * @param delimiter
     *            Meter's data delimiter. Might not be <code>null</code>.
     * @param packetTime
     *            Time in milliseconds when (time) {@code data} was read.
     * @return A not <code>null</code> array with each meter's measurements converted to a {@link WattsUpPacket}.
     */
    public static WattsUpPacket[] parser(final String data, final Delimiter delimiter, long packetTime)
    {
        WattsUpPacket[] packets = new WattsUpPacket[0];
        if (data != null && data.length() > 0)
        {
            String[] lines = data.split(RECORD_DELIMITER.getSymbol());
            packets = parser(lines, delimiter, packetTime);
        }
        return packets;
    }

    /**
     * 
     * @param records
     *            Meter's measurements.
     * @param delimiter
     *            Data delimiter
     * @param packetTime
     *            The time that the {@code data} was read.
     * @return A non-null array with the meter's measures.
     */
    private static WattsUpPacket[] parser(final String[] records, final Delimiter delimiter, long packetTime)
    {
        List<WattsUpPacket> packets = new LinkedList<>();

        for (int i = 0; i < records.length; i++)
        {
            final String record = records[i].trim();

            if (!record.startsWith("#d") && !record.endsWith(RECORD_DELIMITER.getSymbol()))
            {
                continue;
            }

            final String[] fields = record.split(delimiter.getSymbol());

            if (fields.length >= 3)
            {
                int j = 0;
                WattsUpPacket packet = new WattsUpPacket(record, packetTime);

                packet.command_ = fields[j++].trim();

                if (packet.command_.length() >= 1)
                {
                    packet.command_ = packet.command_.substring(1).trim();
                }

                packet.subCommand_ = fields[j++].trim();
                packet.count_ = Integer.valueOf(fields[j++].trim());

                for (int k = 0; k < packet.count_ && j < fields.length; k++)
                {
                    packet.fields_[k] = Field.valueOf(LABELS[k], fields[j++].trim());
                }

                packets.add(packet);
            }
        }

        return packets.toArray(new WattsUpPacket[packets.size()]);
    }

    /**
     * @return the id
     */
    public ID getId()
    {
        return id_;
    }

    /**
     * @return the command
     */
    public String getCommand()
    {
        return command_;
    }

    /**
     * @return the subCommand
     */
    public String getSubCommand()
    {
        return subCommand_;
    }

    /**
     * @return the data
     */
    public String getData()
    {
        return data_;
    }

    /**
     * @return the fields
     */
    public Field[] getFields()
    {
        return fields_.clone();
    }
    
    /**
     * Returns a {@link Field} with the given name.
     * @param name Field name.
     * @return {@link Field} with the given name.
     */
    public Field get(String name)
    {
        //TODO: This method is O(n); change the data structure to a Map to be O(1). 
        Field f = null;
        
        for (Field ff :fields_)
        {
            if (ff.getName().equals(name))
            {
                f = ff.clone();
                break;
            }
        }
        
        return f;
    }

    /**
     * @return the time
     */
    public long getTime()
    {
        return time_;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (Field f : fields_)
        {
            sb.append(" ").append(f.getValue());
        }
        return sb.toString();
    }

    @Override
    public int compareTo(WattsUpPacket other)
    {
        return Long.valueOf(this.getTime()).compareTo(other.getTime());
    }

    /**
     * <p>
     * Returns a {@link Map} with the meter's data. Each entry is a field, where the key is the field's name and the value is the field's value. In
     * addition to the meter's data we have the time when the measurement was realized.
     * </p>
     * 
     * @return A read-only {@link Map} with the meter's data.
     * @see #toMap(boolean)
     */
    public Map<String, Object> toMap()
    {
        return this.toMap(false);
    }

    /**
     * <p>
     * Returns a {@link Map} with the meter's data. Each entry is a field, where the key is the field's name and the value is the field's value. In
     * addition to the meter's data we have the time when the measurement was realized. The field's name may contain space if
     * {@code removeSpaceInFieldsName} is <code>false</code>.
     * </p>
     * 
     * @param removeSpaceInFieldsName
     *            When <code>true</code> removes spaces from the field's name.
     * @return A read-only {@link Map} with the meter's data.
     */
    public Map<String, Object> toMap(boolean removeSpaceInFieldsName)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("time", this.getTime());

        for (wattsup.jsdk.core.data.Field f : this.getFields())
        {
            if (f.getName() != null)
            {
                map.put(removeSpaceInFieldsName ? f.getName().replaceAll("\\W", "").trim() : f.getName().trim(), Double.valueOf(f.getValue()) / 10);
            }            
        }

        return Collections.unmodifiableMap(map);
    }
}
