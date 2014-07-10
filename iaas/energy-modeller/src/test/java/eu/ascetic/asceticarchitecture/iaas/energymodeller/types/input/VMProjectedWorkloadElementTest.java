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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.input;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
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
public class VMProjectedWorkloadElementTest {

    GregorianCalendar time1 = new GregorianCalendar();
    GregorianCalendar time2 = new GregorianCalendar();
    long timeAgoSeconds = 60 * 60 * 10;

    public VMProjectedWorkloadElementTest() {
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
     * Test of getStartTime method, of class VMProjectedWorkloadElement.
     */
    @Test
    public void testGetStartTime() {
        System.out.println("getStartTime");
        VMProjectedWorkloadElement instance = new VMProjectedWorkloadElement();
        instance.setDuration(new TimePeriod(time1, time2));
        Calendar expResult = time1;
        Calendar result = instance.getStartTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of getEndTime method, of class VMProjectedWorkloadElement.
     */
    @Test
    public void testGetEndTime() {
        System.out.println("getEndTime");
        VMProjectedWorkloadElement instance = new VMProjectedWorkloadElement();
        instance.setDuration(new TimePeriod(time1, time2));
        Calendar expResult = time2;
        Calendar result = instance.getEndTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of isLongTermDeployment method, of class VMProjectedWorkloadElement.
     */
    @Test
    public void testIsLongTermDeployment() {
        System.out.println("isLongTermDeployment");
        VMProjectedWorkloadElement instance = new VMProjectedWorkloadElement();
        boolean expResult = true;
        boolean result = instance.isLongTermDeployment();
        assertEquals(expResult, result);
        instance.setDuration(new TimePeriod(time1, time2));
        expResult = false;
        result = instance.isLongTermDeployment();
        assertEquals(expResult, result);
    }

    /**
     * Test of compareTo method, of class VMProjectedWorkloadElement.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        Object o = new VMProjectedWorkloadElement();
        VMProjectedWorkloadElement instance = new VMProjectedWorkloadElement();
        instance.setDuration(new TimePeriod(time1, time2));
        int expResult = -1;
        int result = instance.compareTo(o);
        assertEquals(expResult, result);
        expResult = 1;
        result = ((VMProjectedWorkloadElement) o).compareTo(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDuration method, of class VMProjectedWorkloadElement.
     */
    @Test
    public void testGetDuration() {
        System.out.println("getDuration");
        VMProjectedWorkloadElement instance = new VMProjectedWorkloadElement();
        TimePeriod expResult = new TimePeriod(time1, time2);
        instance.setDuration(new TimePeriod(time1, time2));
        TimePeriod result = instance.getDuration();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDuration method, of class VMProjectedWorkloadElement.
     */
    @Test
    public void testSetDuration() {
        System.out.println("setDuration");
        TimePeriod duration = null;
        VMProjectedWorkloadElement instance = new VMProjectedWorkloadElement();
        assertTrue(instance.getDuration() == null);
        instance.setDuration(duration);
        instance.setDuration(new TimePeriod(time1, time2));
        assertFalse(instance.getDuration() == null);
    }
}