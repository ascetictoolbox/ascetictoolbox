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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.Calibrator;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Richard
 */
public class DataGathererTest {
    
    public DataGathererTest() {
    }

//    /**
//     * Test of stop method, of class DataGatherer.
//     */
//    @Test
//    public void testStop() {
//        System.out.println("stop");
//        DataGatherer instance = null;
//        instance.stop();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of run method, of class DataGatherer.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        Thread thread = new Thread(instance);
        thread.start();
        try {
            Thread.sleep(30000); //gather 30 seconds worth of data.
        } catch (InterruptedException ex) {
            fail("Something interuppted the thread while waiting for some data to be gathered");
        }
    }
    
}
