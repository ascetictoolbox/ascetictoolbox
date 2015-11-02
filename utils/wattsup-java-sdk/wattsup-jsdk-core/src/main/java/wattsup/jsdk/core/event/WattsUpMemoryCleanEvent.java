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

import wattsup.jsdk.core.listener.WattsUpListener;
import wattsup.jsdk.core.listener.WattsUpMemoryCleanListener;

public class WattsUpMemoryCleanEvent extends WattsUpEvent<Long>
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 4800686113742422472L;

    /**
     * 
     * @param source
     *            The source of this event.
     * @param timeInMillis
     *            Time in milliseconds when the device was reseted.
     */
    public WattsUpMemoryCleanEvent(Object source, long timeInMillis)
    {
        super(source, wattsup.jsdk.core.event.WattsUpEvent.EventType.RESET, timeInMillis);
    }

    @Override
    public void processListener(WattsUpListener listener)
    {
        ((WattsUpMemoryCleanListener) listener).processWattsUpReset(this);
    }

    @Override
    public boolean isAppropriateListener(WattsUpListener listener)
    {
        return listener instanceof WattsUpMemoryCleanListener;
    }
}
