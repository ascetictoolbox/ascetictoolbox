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
package wattsup.jsdk.client.jcommander.validator;

import wattsup.jsdk.remote.data.CommandType;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class RemoteCommandNameValidator implements IValueValidator<CommandType>
{
    /**
     * The valid commands.
     */
    public static final String AVAILABLE_COMMANDS = CommandType.asString().toLowerCase();

    @Override
    public void validate(String name, CommandType value) throws ParameterException
    {
        if (value == null)
        {
            throw new ParameterException(String.format("Invalid command! The available commands are: %s", AVAILABLE_COMMANDS));
        }
    }
}
