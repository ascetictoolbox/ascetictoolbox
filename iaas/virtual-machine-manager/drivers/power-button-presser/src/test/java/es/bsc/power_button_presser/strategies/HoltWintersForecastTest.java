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

package es.bsc.power_button_presser.strategies;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class HoltWintersForecastTest {
    
    private final HoltWintersForecast holtWintersForecast = new HoltWintersForecast(10.0, 8.0, 12.0, 6.0, 14.0);
    
    @Test
    public void testGetters() {
        assertEquals(10.0, holtWintersForecast.getForecast());
        assertEquals(8.0, holtWintersForecast.getLow80());
        assertEquals(12.0, holtWintersForecast.getHigh80());
        assertEquals(6.0, holtWintersForecast.getLow95());
        assertEquals(14.0, holtWintersForecast.getHigh95());
    }
    
}
