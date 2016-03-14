/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.test.iaas.vmm.base;

import es.bsc.vmmclient.models.VmDeployed;
import es.bsc.vmmclient.vmm.VmManagerClient;
import eu.ascetic.test.conf.VMMConf;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author raimon
 */
public class VmmTestBase extends TestCase {
    private static final Logger logger = Logger.getLogger("VmmTestBase");
    
    private int iterations;
    private static final int MAX_ITERATIONS = 100;
    public VmManagerClient vmm;
    public String vmId = null;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmm = new VmManagerClient(VMMConf.vmManagerURL);
        iterations = 0;
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(vmId != null){
            //Destroy
            logger.info("Destroying VM " + vmId);
            vmm.destroyVm(vmId);

            VmDeployed vmDestroyed = vmm.getVm(vmId);
            assertNull(vmDestroyed);
            
            vmId = null;
        }
    }
    
    public boolean loopIsAlive() throws Exception{
        if(iterations++ > MAX_ITERATIONS){
            throw new Exception("Timeout reached! This loop cannot do more than " + 
                MAX_ITERATIONS + " iterations.");
            //return false;
        }
        
        return true;
    }
}
