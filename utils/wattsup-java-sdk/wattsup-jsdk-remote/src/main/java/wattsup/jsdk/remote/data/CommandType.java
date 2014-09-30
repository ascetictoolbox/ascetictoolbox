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
package wattsup.jsdk.remote.data;

public enum CommandType
{
    /**
     * Starts a new measurement block.
     */
    START,

    /**
     * Get the data available.
     */
    GET,

    /**
     * Writes the data into the response output.
     */
    DUMP,

    /**
     * Writes the data into an output.
     */
    DUMP_TO,

    /**
     * Finalizes a block's measurement.
     */
    END,

    /**
     * Clear the memory.
     */
    CLEAR,

    /**
     * Writes the data into the output and clear the memory.
     */
    DUMP_TO_AND_CLEAR;

    /**
     * Returns as {@link String} the values separated by comma.
     * 
     * @return The values separated by comma.
     */
    public static String asString()
    {
        StringBuilder availableCommands = new StringBuilder("[");

        for (int i = 0; i < CommandType.values().length; i++)
        {
            if (i > 0)
            {
                availableCommands.append(",");
            }
            availableCommands.append(CommandType.values()[i].name());
        }
        availableCommands.append("]");

        return availableCommands.toString();
    }
}
