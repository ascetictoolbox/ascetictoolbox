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
package wattsup.jsdk.core.listener;

import java.util.EventListener;

/**
 * A generic base interface for event listeners for various types of {@link wattsup.jsdk.core.event.WattsUpEvent}. All listener interfaces for
 * specific WattsUpEvent event types must extend this interface.
 * 
 * Implementations of this interface must have a zero-args public constructor.
 */
public interface WattsUpListener extends EventListener
{
}
