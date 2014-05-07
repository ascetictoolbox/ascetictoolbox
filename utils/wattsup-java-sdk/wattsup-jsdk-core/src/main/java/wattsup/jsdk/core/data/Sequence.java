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
package wattsup.jsdk.core.data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 */
public final class Sequence
{
    /**
     */
    private AtomicLong source_ = new AtomicLong(0);

    /**
     * Returns the current value incremented by one.
     * 
     * @return The next sequence value.
     */
    public Long nextValue()
    {
        return this.source_.incrementAndGet();
    }

    /**
     * Returns the current value.
     * 
     * @return The current value.
     */
    public Long currentValue()
    {
        return this.source_.get();
    }
}
