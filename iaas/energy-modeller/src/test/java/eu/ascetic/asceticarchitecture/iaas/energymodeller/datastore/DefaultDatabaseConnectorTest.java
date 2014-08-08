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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore;

import static org.junit.Assert.assertNotNull;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;

/**
 *
 * @author Richard
 */
public class DefaultDatabaseConnectorTest {

    private final List<Host> hostList = new ArrayList<>();
    private final Host CHOSEN_HOST = new Host(10084, "asok10");

    public DefaultDatabaseConnectorTest() {
        hostList.add(new Host(10105, "asok09"));
        hostList.add(new Host(10084, "asok10"));
        hostList.add(new Host(10107, "asok11"));
        hostList.add(new Host(10106, "asok12"));
    }

    /**
     * Test of getHosts method, of class DefaultDatabaseConnector.
     */
    @Test
    public void testGetHosts() {
        System.out.println("getHosts");
        DefaultDatabaseConnector instance = new DefaultDatabaseConnector();
        Collection<Host> result = instance.getHosts();
        assert( result != null);
        assert (!result.isEmpty());
    }

    /**
     * Test of getHostCalibrationData method, of class DefaultDatabaseConnector.
     */
    @Test
    public void testGetHostCalibrationData_Collection() {
        System.out.println("getHostCalibrationData");
        Collection<Host> hosts = hostList;
        DefaultDatabaseConnector instance = new DefaultDatabaseConnector();
        Collection<Host> result = instance.getHostCalibrationData(hosts);
        assert (!result.isEmpty());
    }

    /**
     * Test of getHostCalibrationData method, of class DefaultDatabaseConnector.
     */
    @Test
    public void testGetHostCalibrationData_Host() {
        System.out.println("getHostCalibrationData");
        Host host = CHOSEN_HOST;
        DefaultDatabaseConnector instance = new DefaultDatabaseConnector();
        Host result = instance.getHostCalibrationData(host);
        assertNotNull(result);
    }

    /**
     * Test of setHosts method, of class DefaultDatabaseConnector.
     */
    @Test
    public void testSetHosts() {
        System.out.println("setHosts");
        Collection<Host> hosts = hostList;
        DefaultDatabaseConnector instance = new DefaultDatabaseConnector();
        instance.setHosts(hosts);
    }

    /**
     * Test of setHostCalibrationData method, of class DefaultDatabaseConnector.
     */
    @Test
    public void testSetHostCalibrationData() {
        System.out.println("setHostCalibrationData");
        Host host = CHOSEN_HOST;
        DefaultDatabaseConnector instance = new DefaultDatabaseConnector();
        //This will run the method but otherwise do very little. A better test case would be good.
        instance.setHostCalibrationData(host); 
        assert(host.getCalibrationData().isEmpty());
    }

    /**
     * Test of writeHostHistoricData method, of class DefaultDatabaseConnector.
     */
    @Test
    public void testWriteHostHistoricData() {
        System.out.println("writeHostHistoricData");
        Host host = CHOSEN_HOST;
        long time = new GregorianCalendar().getTimeInMillis() / 1000;
        double power = 300;
        double energy = 1500;
        DefaultDatabaseConnector instance = new DefaultDatabaseConnector();
        instance.writeHostHistoricData(host, time, power, energy);
    }

    /**
     * Test of getHostHistoryData method, of class DefaultDatabaseConnector.
     */
    @Test
    public void testGetHostHistoryData() {
        System.out.println("getHostHistoryData");
        Host host = CHOSEN_HOST;
        TimePeriod timePeriod = null;
        DefaultDatabaseConnector instance = new DefaultDatabaseConnector();
        Collection<HostEnergyRecord> result = instance.getHostHistoryData(host, timePeriod);
        assert(!result.isEmpty());
        System.out.println("Host Records Found: " + result.size());

    }

}
