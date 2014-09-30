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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public final class TextUtil
{
    /**
     * Private constructor to avoid instance of this class.
     */
    private TextUtil()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a {@link String} containing the contents lines of the {@code file}.
     * 
     * @param file
     *            The file to be read.
     * @return A String containing the contents of the lines, including line-termination characters, or <code>null</code> if the end of the stream has
     *         been reached.
     * @throws IOException
     *             If an I/O error occurs.
     * @throws NullPointerException
     *             If {@code file} is <code>null</code>.
     */
    public static String readLines(File file) throws IOException
    {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(Objects.requireNonNull(file))))
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
