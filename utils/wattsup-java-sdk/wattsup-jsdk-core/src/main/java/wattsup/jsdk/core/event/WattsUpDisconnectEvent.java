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

import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.listener.WattsUpListener;

public class WattsUpDisconnectEvent extends WattsUpEvent<Long>
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = -26658202708485717L;

    /**
     * 
     * @param source
     *            The source of this event.
     * @param timeInMilliseconds
     *            The time in milliseconds when the meter was disconnected.
     */
    public WattsUpDisconnectEvent(Object source, long timeInMilliseconds)
    {
        super(source, wattsup.jsdk.core.event.WattsUpEvent.EventType.DISCONNECT, timeInMilliseconds);
    }

    @Override
    public void processListener(WattsUpListener listener)
    {
        ((WattsUpDisconnectListener) listener).onDisconnect(this);
    }

    @Override
    public boolean isAppropriateListener(WattsUpListener listener)
    {
        return listener instanceof WattsUpDisconnectListener;
    }
}
