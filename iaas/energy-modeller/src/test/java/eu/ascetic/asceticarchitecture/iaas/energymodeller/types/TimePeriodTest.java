/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class TimePeriodTest {

    GregorianCalendar time1 = new GregorianCalendar();
    GregorianCalendar time2 = new GregorianCalendar();
    long timeAgoSeconds = 60 * 60 * 10;

    public TimePeriodTest() {
        /*
         * Sets the first time 10 hours in the past.
         * 1000 sets the time to seconds, 
         * then 60 to mins, 
         * then 60 to an hour 
         * then 10 gets 10 hours ago
         */
        time1.setTimeInMillis(time2.getTimeInMillis() - (1000 * timeAgoSeconds));
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getDuration method, of class TimePeriod.
     */
    @Test
    public void testGetDuration() {
        System.out.println("getDuration");
        TimePeriod instance = new TimePeriod(time1, time2);
        long expResult = 36000L;
        long result = instance.getDuration();
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToMinutes method, of class TimePeriod.
     */
    @Test
    public void testConvertToMinutes() {
        System.out.println("convertToMinutes");
        TimePeriod duration = new TimePeriod(time1, time2);
        long expResult = 600L;
        long result = TimePeriod.convertToMinutes(duration);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToHours method, of class TimePeriod.
     */
    @Test
    public void testConvertToHours() {
        System.out.println("convertToHours");
        TimePeriod duration = new TimePeriod(time1, time2);
        long expResult = 10L;
        long result = TimePeriod.convertToHours(duration);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToDays method, of class TimePeriod.
     */
    @Test
    public void testConvertToDays() {
        System.out.println("convertToDays");
        TimePeriod duration = new TimePeriod(time1, time2);
        long expResult = 0L;
        long result = TimePeriod.convertToDays(duration);
        assertEquals(expResult, result);
    }

    /**
     * Test of getStartTime method, of class TimePeriod.
     */
    @Test
    public void testGetStartTime() {
        System.out.println("getStartTime");
        TimePeriod instance = new TimePeriod(time1, time2);
        Calendar expResult = time1;
        Calendar result = instance.getStartTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of getEndTime method, of class TimePeriod.
     */
    @Test
    public void testGetEndTime() {
        System.out.println("getEndTime");
        TimePeriod instance = new TimePeriod(time1, time2);
        Calendar expResult = time2;
        Calendar result = instance.getEndTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class TimePeriod.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = new TimePeriod(time1, time2);
        TimePeriod instance = new TimePeriod(time1, time2);
        boolean expResult = true;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new TimePeriod(time2, new GregorianCalendar());
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);        
    }

    /**
     * Test of hashCode method, of class TimePeriod.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        TimePeriod instance = new TimePeriod(time1, time2);
        int result = instance.hashCode();
        assertTrue(result != -1);
    }

    /**
     * Test of compareTo method, of class TimePeriod.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        //equals
        Object o = new TimePeriod(time1, time2);
        TimePeriod instance = new TimePeriod(time1, time2);
        int expResult = 0;
        int result = instance.compareTo(o);
        assertEquals(expResult, result);
        //before
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(time2.getTimeInMillis() - (1000 * (timeAgoSeconds / 2)));
        o = new TimePeriod(cal, time2);
        expResult = -1;
        result = instance.compareTo(o);
        assertEquals(expResult, result);        
        //after
        cal = new GregorianCalendar();
        cal.setTimeInMillis(time2.getTimeInMillis() - (1000 * timeAgoSeconds * 2));
        o = new TimePeriod(cal, time2);
        expResult = 1;
        result = instance.compareTo(o);
        assertEquals(expResult, result);  
    }
}