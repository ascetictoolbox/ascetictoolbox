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
package wattsup.jsdk.core.exception;

public class WattsUpException extends RuntimeException
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 7373596993831870223L;

    /**
     * Default constructor.
     */
    public WattsUpException()
    {
        super();
    }

    /**
     * Creates a {@link WattsUpException} with the {@code message}.
     * @param message The message with information about the exception.
     */
    public WattsUpException(String message)
    {
        super(message);
    }

    /**
     * Creates a {@link WattsUpException} with the {@code cause}.
     * @param cause The root of this exception.
     */
    public WattsUpException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a {@link WattsUpException} with the {@code message} and {@code cause}.
     * @param message The message with information about the exception.
     * @param cause The root of this exception.
     */
    public WattsUpException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
