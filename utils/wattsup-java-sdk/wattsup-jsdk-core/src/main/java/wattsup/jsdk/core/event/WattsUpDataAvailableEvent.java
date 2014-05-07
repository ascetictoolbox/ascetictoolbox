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

import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.listener.WattsUpDataAvailableListener;
import wattsup.jsdk.core.listener.WattsUpListener;

public class WattsUpDataAvailableEvent extends WattsUpEvent<WattsUpPacket[]>
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 7266611207810888376L;

    /**
     * @param source The reference to the source of this event.
     * @param values The data available.
     */
    public WattsUpDataAvailableEvent(Object source, WattsUpPacket[] values)
    {
        super(source, WattsUpEvent.EventType.DATA_AVAILABLE, values);
    }

    @Override
    public void processListener(WattsUpListener listener)
    {
        ((WattsUpDataAvailableListener) listener).processDataAvailable(this);
    }

    @Override
    public boolean isAppropriateListener(WattsUpListener listener)
    {
        return listener instanceof WattsUpDataAvailableListener;
    }
}
