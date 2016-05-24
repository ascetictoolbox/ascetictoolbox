/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.test.iaas.vmm;

import es.bsc.vmmclient.models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.bsc.vmm.ascetic.mq.ActiveMqAdapter;
import eu.ascetic.test.conf.VMMConf;
import eu.ascetic.test.iaas.vmm.base.VmmTestBase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

/**
 *
 * @author raimon
 */
public class SLAViolationTest extends VmmTestBase implements MessageListener{
    private static final Logger logger = Logger.getLogger("SLAViolationTest");
    
    protected ActiveMqAdapter adapter;
    protected List<Message> messages;
    protected String mqURL;
    Gson gson;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        messages = new ArrayList<>();
        mqURL = VMMConf.activeMqUrl;
        adapter = new ActiveMqAdapter(mqURL);
        gson = new GsonBuilder().create();
        adapter.listenToQueue(VMM_SELF_ADAPTATION_TOPIC_NAME, this);
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        this.waitForIt(10, "You must wait to test a new self-adpatation...");
        adapter.closeQueue(VMM_SELF_ADAPTATION_TOPIC_NAME);
        messages.clear();
    }
    
    /**
     * Tests if a migration has not been done after a SLA violation on hw_platform.
     * 
     * @throws Exception 
     */
    public void testHwPlatformSLAViolationMigrationFail() throws Exception {
        String sourceHostname = "";
        if(environment.equals("dev")){
            sourceHostname = "compute1";
        } else if(environment.equals("test")){
            sourceHostname = "wally152";
        } else if(environment.equals("stable")){
            sourceHostname = "wally157";
        } else{
            throw new Exception("Environment must be defined on testHwInfo()");
        }

        String vmName = "HwPlatformSLAViolationMigrationFailTest01";
        VmRequirements vmDeployRequirements = 
           new VmRequirements( 1, 256, 1, 16, "x86_64", "Intel", "SSD");

        logger.info("Deploying " + vmName + " with requirements:" + vmDeployRequirements.toString() + "...");
        Vm vm = new Vm(vmName, VMMConf.imageId, vmDeployRequirements, null, "dsh01", "", "sla", sourceHostname);
        List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
        VmDeployed vmd = vmm.getVm(deployedVms.get(0));
        vmId = vmd.getId();
        
        logger.info("Migrating to a Xeon cpu... (this migration should fail)");
        sendFakeSLAMessage(vmId, vm.getSlaId(), vm.getOvfId(), "hw_platform", "x86_64/Xeon", "x86_64/Intel;SSD");
        waitForIt(20, "Waiting self-adaptation to finish...");
        
        //Assert that migration has failed
        vmd = vmm.getVm(deployedVms.get(0));
        assertEquals(sourceHostname, vmd.getHostName());
        
        //Assert message has been added
        Message message = messages.get(messages.size()-1);
        assertTrue(message instanceof TextMessage);
        TextMessage tm = (TextMessage) message;
        SelfAdaptationAction action = gson.fromJson(tm.getText(), SelfAdaptationAction.class);
        assertTrue(action.getException().contains("No suitable deployment plan found"));
        assertFalse(action.getSuccess());
    }
    
    /**
     * Tests if a migration is done after a SLA Violation on hw_platform.
     * 
     * @throws Exception 
     */
    public void testHwPlatformSLAViolationMigrationDone() throws Exception {
        String targetHostname = "";
        String sourceHostname = "";
        if(environment.equals("dev")){
            sourceHostname = "compute1";
            targetHostname = "compute2"; 
        } else if(environment.equals("test")){
            sourceHostname = "wally152";
            targetHostname = "wally153"; 
        } else if(environment.equals("stable")){
            targetHostname = "wally158";
            sourceHostname = "wally157";
        } else{
            throw new Exception("Environment must be defined on testHwInfo()");
        }

        String vmName = "HwPlatformSLAViolationMigrationDoneTest01";
        VmRequirements vmDeployRequirements = 
           new VmRequirements( 1, 256, 1, 16, "x86_64", "Intel", "SSD");

        logger.info("Deploying " + vmName + " with requirements:" + vmDeployRequirements.toString() + "...");
        Vm vm = new Vm(vmName, VMMConf.imageId, vmDeployRequirements, null, "dsh01", "", "sla", sourceHostname);
        List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
        VmDeployed vmd = vmm.getVm(deployedVms.get(0));
        vmId = vmd.getId();
        
        logger.info("Migrating to a RAID disk... (this migration should be possible)");
        sendFakeSLAMessage(vmId, vm.getSlaId(), vm.getOvfId(), "hw_platform", "x86_64/Intel;RAID", "x86_64/Intel;SSD");
        waitForIt(120, "Waiting self-adaptation to finish...");
        
        //Assert that migration has been done
        vmd = vmm.getVm(deployedVms.get(0));
        assertEquals(targetHostname, vmd.getHostName());
        
        Message message = messages.get(messages.size()-1);
        assertTrue(message instanceof TextMessage);
        TextMessage tm = (TextMessage) message;
        SelfAdaptationAction action = gson.fromJson(tm.getText(), SelfAdaptationAction.class);
        for(VmPlacement placement : action.getDeploymentPlan()){
            if(placement.getVmId().equals( vmd.getId() )){
                assertEquals(targetHostname, placement.getHostname());
            }
        }
    }
    
    /* ACTIVEMQ LOGIC */
    
    /**
     * Creates a fake SLA message w/ a Violation notification.
     * 
     * @param vmId
     * @param slaId
     * @param ovfId
     * @param slaAgreementTerm
     * @param guaranteedValue
     * @param usageValue
     * @throws Exception 
     */
    private void sendFakeSLAMessage(String vmId, String slaId, String ovfId, String slaAgreementTerm, String guaranteedValue, String usageValue) throws Exception {
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String now = dt.format(new Date()).replace(" ", "T");
        String message = 
            "<ViolationMessage vmId=\"" + vmId + "\" ovfId=\"" + ovfId + "\">\n" +
            "  <time>" + now + "</time>\n" +
            "  <value id=\"usage\">" + usageValue + "</value>\n" +
            "  <alert>\n" +
            "    <type>violation</type>\n" +
            "    <slaUUID>" + slaId + "</slaUUID>\n" +
            "    <slaAgreementTerm>" + slaAgreementTerm + "</slaAgreementTerm>\n" +
            "    <slaGuaranteedState>\n" +
            "      <guaranteedId>" + slaAgreementTerm + "</guaranteedId>\n" +
            "      <operator>EQUAL</operator>\n" +
            "      <guaranteedValue>" + guaranteedValue + "</guaranteedValue>\n" +
            "      <this_-1 reference=\"../..\"/>\n" +
            "    </slaGuaranteedState>\n" +
            "    <outer-class reference=\"../..\"/>\n" +
            "  </alert>\n" +
            "</ViolationMessage>";
        
        adapter.publishMessage(String.format(VIOLATION_QUEUE_NAME, slaId, vmId), message);
	}
    
    @Override
    public void onMessage(Message message) {
        try {
            messages.add(message);
            message.acknowledge();
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
    
    private static final String VIOLATION_QUEUE_NAME = "iaas-slam.monitoring.%s.%s.violationNotified";
    private static final String VMM_SELF_ADAPTATION_TOPIC_NAME = "virtual-machine-manager.self-adaptation";
}