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
package wattsup.jsdk.client.jcommander.converter;

import wattsup.jsdk.remote.data.CommandType;

import com.beust.jcommander.IStringConverter;

public class CommandTypeConverter implements IStringConverter<CommandType>
{
    @Override
    public CommandType convert(String value)
    {
        CommandType command;
        try
        {
            command = CommandType.valueOf(value.trim().toUpperCase());
        }
        catch (java.lang.IllegalArgumentException exception)
        {
            command = null;
        }
        return command;
    }
}
