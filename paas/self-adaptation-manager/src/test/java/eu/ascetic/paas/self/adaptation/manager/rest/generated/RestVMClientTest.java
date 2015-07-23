/**
 * Copyright 2015 University of Leeds
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
package eu.ascetic.paas.self.adaptation.manager.rest.generated;

import com.sun.jersey.api.client.ClientResponse;
import eu.ascetic.paas.applicationmanager.model.VM;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Richard Kavanagh
 */
public class RestVMClientTest {

    public RestVMClientTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of setResourcePath method, of class RestVMClient.
     */
    @Test
    public void testSetResourcePath() {
        System.out.println("setResourcePath");
        String application_name = "";
        String deployment_id = "";
        RestVMClient instance = null;
        instance.setResourcePath(application_name, deployment_id);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEnergySample method, of class RestVMClient.
     */
    @Test
    public void testGetEnergySample() {
        System.out.println("getEnergySample");
        RestVMClient instance = new RestVMClient("", "");
        Object expResult = null;
//        Object result = instance.getEnergySample(null);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVM method, of class RestVMClient.
     */
    @Test
    public void testGetVM() {
        System.out.println("getVM");
        RestVMClient instance = new RestVMClient("threeTierWebApp", "100");
        String result = instance.getVM(String.class, "1686");
        System.out.println(result);
    }

    /**
     * Test of postVM method, of class RestVMClient.
     */
    @Test
    public void testPostVM() {
        System.out.println("postVM");
        Object requestEntity = null;
        RestVMClient instance = new RestVMClient("", "");
        ClientResponse expResult = null;
        ClientResponse result = instance.postVM(requestEntity);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEnergyConsumption method, of class RestVMClient.
     */
    @Test
    public void testGetEnergyConsumption() {
        System.out.println("getEnergyConsumption");
        RestVMClient instance = new RestVMClient("", "");
        Object expResult = null;
//        Object result = instance.getEnergyConsumption(null);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteVM method, of class RestVMClient.
     */
    @Test
    public void testDeleteVM() {
        System.out.println("deleteVM");
        String vm_id = "";
        RestVMClient instance = null;
        ClientResponse expResult = null;
        ClientResponse result = instance.deleteVM(vm_id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEnergyEstimation method, of class RestVMClient.
     */
    @Test
    public void testGetEnergyEstimation() {
        System.out.println("getEnergyEstimation");
        RestVMClient instance = new RestVMClient("", "");
        Object expResult = null;
//        Object result = instance.getEnergyEstimation(null);
//        assertEquals(ect result = instance.getEnergyEstimation(null);
//        assertEqexpResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVMs method, of class RestVMClient.
     */
    @Test
    public void testGetVMs() {

        System.out.println("getVMs");
        RestVMClient instance = new RestVMClient("threeTierWebApp", "478");
        String result = instance.getVMs(String.class);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(VM.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            VM vmFromRest = (VM) jaxbUnmarshaller.unmarshal(new StringReader(result));

        assertEquals(33, vmFromRest.getId());
        assertEquals("0.0.0.0", vmFromRest.getIp());
        assertEquals("ACTIVE", vmFromRest.getStatus());
        } catch (JAXBException ex) {
            Logger.getLogger(RestVMClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Test of close method, of class RestVMClient.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        RestVMClient instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
