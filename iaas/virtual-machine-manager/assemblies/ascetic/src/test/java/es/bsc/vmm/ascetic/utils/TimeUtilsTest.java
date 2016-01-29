/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmm.ascetic.utils;

import es.bsc.demiurge.core.utils.TimeUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the TimeUtils class.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class TimeUtilsTest {

    @Test
    public void getDifferenceInSecondsTest() {
        Calendar oldCalendar = Calendar.getInstance();
        oldCalendar.set(2015, Calendar.JANUARY, 1, 1, 1, 1);
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(2015, Calendar.JANUARY, 1, 1, 1, 11);
        Assert.assertEquals(10, TimeUtils.getDifferenceInSeconds(oldCalendar, newCalendar));
    }
    
}
