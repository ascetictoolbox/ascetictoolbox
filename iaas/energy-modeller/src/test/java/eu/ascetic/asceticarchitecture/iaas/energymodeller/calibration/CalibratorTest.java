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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.Configuration;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class CalibratorTest {
    
    public CalibratorTest() {
        try {
            /**
             * This will save the configuration file to disk. In case the
             * defaults need setting. It will prevent this test code failing in
             * the event of clean and build or running from a fresh
             * installation.
             */
            org.apache.commons.configuration.PropertiesConfiguration config = new PropertiesConfiguration("ascetic-zabbix-api.properties");
            config.getString("zabbix.server.url", "http://10.4.0.15/zabbix/api_jsonrpc.php");
            config.getString("zabbix.user", "admin");
            config.getString("zabbix.password", "73046447cce977b10167");
            config.save();
        } catch (ConfigurationException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.INFO, "Error loading the configuration of the IaaS energy modeller", ex);
        }        
    }
    
    private final Host CHOSEN_HOST = new Host(10084, "asok10");

    /**
     * Test of calibrateHostEnergyData method, of class Calibrator.
     */
    @Test
    public void testCalibrateHostEnergyData() {
        System.out.println("calibrateHostEnergyData");
        Host host = new Host(10084, "asok10");
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        Calibrator instance = new Calibrator(adaptor);
        Host expResult = CHOSEN_HOST;
        Host result = instance.calibrateHostEnergyData(host);
        assertEquals(expResult, result);
        assert(result.getIdlePowerConsumption() > 0.0);
        System.out.println("Idle Power: " + result.getIdlePowerConsumption());
        assert(result.isCalibrated());
        System.out.println("Calibration Data Count: " + result.getCalibrationData().size());
    }

    /**
     * Test of setGenerator method, of class Calibrator.
     */
    @Test
    public void testSetGenerator() {
        System.out.println("setGenerator");
        DefaultLoadGenerator generator = null;
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        Calibrator instance = new Calibrator(adaptor);
        instance.setGenerator(generator);
    }
    
}
