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
package wattsup.jsdk.core.serialize.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import wattsup.jsdk.core.data.Field;
import wattsup.jsdk.core.data.Sequence;
import wattsup.jsdk.core.data.WattsUpConfig.Delimiter;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.serialize.Serializer;

public class CsvWattsUpPacketSerializer implements Serializer
{
    /**
     * A flag to indicate if a header number must be included in output. The header is the name of the fields/columns.
     */
    private boolean includeHeader_;

    /**
     * Flag to indicate if the header has already been inserted.
     */
    private volatile boolean headerIncluded_;

    /**
     * A flag to indicate if the row's number must be included in the file.
     */
    private boolean includeRownum_;

    /**
     * A sequence instance to generate the number of each row.
     */
    private volatile Sequence sequence_;

    /**
     * The date pattern to format the the {@link WattsUpPacket} time.
     */
    private String dateFormatPattern_ = "yyyy-MM-dd HH:mm:ss";

    /**
     * Default constructor. Uses the fluent methods to configure the instance.
     */
    public CsvWattsUpPacketSerializer()
    {
        super();
    }

    /**
     * Configure this {@link wattsup.jsdk.core.serialize.Serializer} to include the name of each column.
     * 
     * @return The same instance configured to include the name of the columns.
     */
    public CsvWattsUpPacketSerializer includeHeader()
    {
        this.includeHeader_ = true;
        return this;
    }

    /**
     * Configure this {@link wattsup.jsdk.core.serialize.Serializer} to include the number of each line.
     * 
     * @return The same instance configured to include the number of each line.
     */
    public CsvWattsUpPacketSerializer includeRownum()
    {
        this.includeRownum_ = true;
        this.sequence_ = new Sequence();
        return this;
    }

    /**
     * Configure the date format to use in the {@link WattsUpPacket} time. This method ignore both <em>null</em> and empty {@link String}.
     * 
     * @param pattern
     *            Date format to use.
     * @return The same instance configured to use the given date format.
     */
    public CsvWattsUpPacketSerializer configureDateFormat(String pattern)
    {
        if (pattern != null && !pattern.trim().isEmpty())
        {
            this.dateFormatPattern_ = pattern;
        }

        return this;
    }

    /**
     * Inserts the header into the output file. The header is the name of each field including only letters [a-z,A-Z].
     * 
     * @param data
     *            The reference to the {@link WattsUpPacket} that has the fields for the CSV's file.
     * @param output
     *            A {@link StringBuffer} to append a line with the reader.
     */
    private void header(final WattsUpPacket data, final StringBuilder output)
    {
        if (includeRownum_)
        {
            output.append("rownum").append(Delimiter.COMMA.getSymbol());
        }

        output.append("date");

        for (Field f : data.getFields())
        {
            output.append(Delimiter.COMMA.getSymbol());
            output.append(f.getName() != null ? f.getName().replaceAll("\\W", "") : "null");
        }
        output.append("\n");
    }

    @Override
    public int serialize(final OutputStream out, final Serializable value) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormatPattern_);
        
        final WattsUpPacket packet = (WattsUpPacket) value;

        if (includeHeader_ && !headerIncluded_)
        {
            header(packet, sb);
            headerIncluded_ = true;
        }

        if (includeRownum_)
        {
            sb.append(sequence_.nextValue()).append(Delimiter.COMMA.getSymbol());
        }

        sb.append(dateFormat.format(new Date(packet.getTime())));

        for (Field f : packet.getFields())
        {
            try
            {
                sb.append(Delimiter.COMMA.getSymbol());
                sb.append(Double.valueOf(f.getValue()) / 10);
            }
            catch (NumberFormatException nfe)
            {
                sb.append(-1);
            }
        }

        sb.append("\n");
        
        byte[] b = sb.toString().getBytes();
        out.write(b);
        
        return b.length;
    }

    @Override
    public <A extends Serializable> A deserialize(InputStream in, int available) throws IOException
    {
        return null;
    }
}
