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
package wattsup.jsdk.core.event;

import java.util.EventObject;

import wattsup.jsdk.core.listener.WattsUpListener;

public abstract class WattsUpEvent<T> extends EventObject
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 8129051864407658211L;

    /**
     * The enumeration with the available events.
     */
    public static enum EventType
    {
        /**
         * Signal a successfully connection with the meter.
         */
        CONNECT,

        /**
         * Signal about the memory reset.
         */
        RESET,

        /**
         * Signal when there are data available.
         */
        DATA_AVAILABLE,

        /**
         * Signal when the connection with the meter was closed.
         */
        DISCONNECT, 
        
        /**
         * Signal when the meter logging stop.
         */
        STOP_LOGGING;
    }

    /**
     * The value associated with an event.
     */
    private final T value_;

    /**
     * The event type.
     */
    private final EventType type_;

    /**
     * Creates a {@link WattsUpEvent}.
     * 
     * @param source
     *            The source of this event.
     * @param type
     *            This event type.
     * @param value
     *            The values associated with this event.
     */
    public WattsUpEvent(Object source, EventType type, T value)
    {
        super(source);
        this.type_ = type;
        value_ = value;
    }

    /**
     * @return the type_
     */
    public EventType getType()
    {
        return type_;
    }

    /**
     * Returns the value(s) associated with this {@link WattsUpEvent}.
     * 
     * @return The value(s) associated with this {@link WattsUpEvent}.
     */
    public T getValue()
    {
        return value_;
    }

    /**
     * Calls an event processing method, passing this {@link WattsUpEvent} as parameter.
     * 
     * @param listener
     *            The listener to send this {@link WattsUpEvent}.
     */
    public abstract void processListener(WattsUpListener listener);

    /**
     * Returns <code>true</code> if this {@link WattsUpListener} is an instance of a listener class supported by this event.
     * 
     * @param listener
     *            The {@link WattsUpListener} instance to evaluate.
     * @return <code>true</code> if this {@link WattsUpListener} is a type expected by this event.
     */
    public abstract boolean isAppropriateListener(WattsUpListener listener);
}
