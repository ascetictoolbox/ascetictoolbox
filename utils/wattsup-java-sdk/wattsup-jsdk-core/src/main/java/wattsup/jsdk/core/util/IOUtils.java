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
package wattsup.jsdk.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class IOUtils
{
    private IOUtils()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates and returns a {@link InputStream} using the given data as content.
     * 
     * @param data
     *            The {@link InputStream} data source.
     * @return A new {@link InputStream} with the data.
     */
    public static InputStream createInputStream(byte[] data)
    {
        return new ByteArrayInputStream(data);
    }

    public static byte[] readfully(InputStream in) throws IOException
    {
        byte[] result = new byte[0];

        if (in != null)
        {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
            {
                int bytesRead;
                while ((bytesRead = in.read()) > 0)
                {
                    baos.write(bytesRead);
                }
                result = baos.toByteArray();
            }
        }

        return result;
    }

    /**
     * Compresses and returns a given input.
     * 
     * @param b
     *            The data to compress.
     * @return The input compressed. An empty array when the input is <code>null</code>.
     * @throws IOException
     *             If an I/O error occurs.
     */
    public static byte[] compress(byte[] b) throws IOException
    {
        byte[] result = new byte[0];

        if (b != null)
        {
            Deflater compressor = new Deflater();
            compressor.setLevel(Deflater.BEST_COMPRESSION);
            compressor.setInput(b);
            compressor.finish();

            try (ByteArrayOutputStream out = new ByteArrayOutputStream(b.length))
            {
                byte[] buffer = new byte[1024];
                while (!compressor.finished())
                {
                    int bytesCompressed = compressor.deflate(buffer);
                    out.write(buffer, 0, bytesCompressed);
                }
                result = out.toByteArray();
            }

            compressor.end();
        }

        return result;
    }

    /**
     * Uncompresses the given data. A return value of length 0 indicates a <code>null</code> input.
     * 
     * @param data
     *            Compressed data.
     * @return
     * @throws IOException
     *             if an I/O error occurs.
     * @throws DataFormatException
     *             if the compressed data format is invalid.
     */
    public static byte[] uncompress(byte[] data) throws IOException, DataFormatException
    {
        byte[] uncompresses = new byte[0];

        if (data != null)
        {
            Inflater decompressor = new Inflater();
            decompressor.setInput(data);

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream())
            {
                byte[] buffer = new byte[1024];

                while (!decompressor.finished())
                {
                    int count = decompressor.inflate(buffer);
                    bos.write(buffer, 0, count);
                }
                uncompresses = bos.toByteArray();
            }
            decompressor.end();
        }
        return uncompresses;
    }
}
