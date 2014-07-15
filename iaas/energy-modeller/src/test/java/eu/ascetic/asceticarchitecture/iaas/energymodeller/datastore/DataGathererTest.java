/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore;

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
        DataGatherer instance = new DataGatherer(adaptor, connector);
        Thread thread = new Thread(instance);
        thread.start();
        try {
            Thread.sleep(30000); //gather 30 seconds worth of data.
        } catch (InterruptedException ex) {
            fail("Something interuppted the thread while waiting for some data to be gathered");
        }
    }
    
}
