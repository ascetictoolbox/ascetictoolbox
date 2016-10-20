/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.test.iaas.vmm;

import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.Slot;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmRequirements;
import es.bsc.vmmclient.vmm.VmManagerClient;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.SlotSolution;
import eu.ascetic.paas.self.adaptation.manager.utils.SlotAwareDeployer;
import eu.ascetic.test.conf.VMMConf;
import eu.ascetic.test.iaas.vmm.base.VmmTestBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author raimon
 */
public class SlotAwareDeploymentTest extends VmmTestBase{
    private static final Logger logger = Logger.getLogger("SlotAwareDeploymentTest");
    
    public void testSlotAwareDeploymentFake() {
        Map<String, Node> nodesTable = new HashMap<String, Node>();
        nodesTable.put("hostA", new Node("hostA", 8, 8*1024, 80, 6, 6*1024, 60, 0));
        nodesTable.put("hostB", new Node("hostB", 8, 8*1024, 80, 4, 4*1024, 40, 0));
        nodesTable.put("hostC", new Node("hostC", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        
        List<Slot> slots = new ArrayList<>();
        slots.add(new Slot("hostC", 8, 800, 8000));
        slots.add(new Slot("hostA", 2, 200, 2000));
        slots.add(new Slot("hostB", 4, 400, 4000));

        int minCpus = 2;
        int maxCpus = 4;
        int totalCpusToAdd = 6;
        
        SlotAwareDeployer deployer = new SlotAwareDeployer();
        List<SlotSolution> solutions = deployer.getSlotsSortedByConsolidationScore(slots, nodesTable, totalCpusToAdd, minCpus, maxCpus, 1024, 10);
        System.out.println(solutions);
        System.out.println(solutions.get(0));
    }
    
    public void testSlotAwareDeploymentFake26servers() {
        Map<String, Node> nodesTable = new HashMap<String, Node>();
        nodesTable.put("hostD", new Node("hostD", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostF", new Node("hostF", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostG", new Node("hostG", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostH", new Node("hostH", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostI", new Node("hostI", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostA", new Node("hostA", 8, 8*1024, 80, 6, 6*1024, 60, 0));
        nodesTable.put("hostJ", new Node("hostJ", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostK", new Node("hostK", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostL", new Node("hostL", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostB", new Node("hostB", 8, 8*1024, 80, 4, 4*1024, 40, 0));
        nodesTable.put("hostM", new Node("hostM", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostN", new Node("hostN", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostO", new Node("hostO", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostP", new Node("hostP", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostQ", new Node("hostQ", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostR", new Node("hostR", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostS", new Node("hostS", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostT", new Node("hostT", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostU", new Node("hostU", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostC", new Node("hostC", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostV", new Node("hostV", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostW", new Node("hostW", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostX", new Node("hostX", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostY", new Node("hostY", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        nodesTable.put("hostZ", new Node("hostZ", 8, 8*1024, 80, 8, 8*1024, 80, 0));
        
        List<Slot> slots = new ArrayList<>();
        slots.add(new Slot("hostD", 8, 800, 8000));
        slots.add(new Slot("hostF", 8, 800, 8000));
        slots.add(new Slot("hostG", 8, 800, 8000));
        slots.add(new Slot("hostH", 8, 800, 8000));
        slots.add(new Slot("hostI", 8, 800, 8000));
        slots.add(new Slot("hostJ", 8, 800, 8000));
        slots.add(new Slot("hostK", 8, 800, 8000));
        slots.add(new Slot("hostL", 8, 800, 8000));
        slots.add(new Slot("hostM", 8, 800, 8000));
        slots.add(new Slot("hostN", 8, 800, 8000));
        slots.add(new Slot("hostO", 8, 800, 8000));
        slots.add(new Slot("hostA", 2, 200, 2000));
        slots.add(new Slot("hostP", 8, 800, 8000));
        slots.add(new Slot("hostQ", 8, 800, 8000));
        slots.add(new Slot("hostB", 4, 400, 4000));
        slots.add(new Slot("hostR", 8, 800, 8000));
        slots.add(new Slot("hostS", 8, 800, 8000));
        slots.add(new Slot("hostT", 8, 800, 8000));
        slots.add(new Slot("hostU", 8, 800, 8000));
        slots.add(new Slot("hostV", 8, 800, 8000));
        slots.add(new Slot("hostW", 8, 800, 8000));
        slots.add(new Slot("hostC", 8, 800, 8000));
        slots.add(new Slot("hostX", 8, 800, 8000));
        slots.add(new Slot("hostY", 8, 800, 8000));
        slots.add(new Slot("hostZ", 8, 800, 8000));

        int minCpus = 2;
        int maxCpus = 4;
        int totalCpusToAdd = 6;
        
        SlotAwareDeployer deployer = new SlotAwareDeployer();
        List<SlotSolution> solutions = deployer.getSlotsSortedByConsolidationScore(slots, nodesTable, totalCpusToAdd, minCpus, maxCpus, 1024, 10);
        System.out.println(solutions);
        System.out.println(solutions.get(0));
    }
    
    public void testSlotAwareDeploymentMultiProviderFake() {
        boolean bestProvider = true;
        
        Map<String, Node> nodesTable1 = new HashMap<String, Node>();
        nodesTable1.put("hostA", new Node("hostA", 16, 16*1024, 160, 6, 6*1024, 60, 0));
        nodesTable1.put("hostB", new Node("hostB", 12, 12*1024, 120, 0, 0, 0, 0));
        Map<String, Node> nodesTable2 = new HashMap<String, Node>();
        nodesTable2.put("hostC", new Node("hostC", 16, 16*1024, 160, 0, 0, 0, 0));
        nodesTable2.put("hostD", new Node("hostD", 12, 12*1024, 120, 0, 0, 0, 0));
        
        List<Slot> slots1 = new ArrayList<>();
        slots1.add(new Slot("hostA", 10, 100, 10*1024));
        slots1.add(new Slot("hostB", 12, 120, 12*1024));
        List<Slot> slots2 = new ArrayList<>();
        slots2.add(new Slot("hostC", 16, 160, 16*1024));
        slots2.add(new Slot("hostD", 12, 120, 12*1024));
        
        int minCpus = 2;
        int maxCpus = 4;
        int totalCpusToAdd = 10;
        
        SlotAwareDeployer deployerA = new SlotAwareDeployer("providerA");
        List<SlotSolution> solutions1 = deployerA.getSlotsSortedByConsolidationScore(
                slots1, nodesTable1, totalCpusToAdd, minCpus, maxCpus, 512, 10);
        System.out.println(solutions1);

        SlotAwareDeployer deployerB = new SlotAwareDeployer("providerB");
        List<SlotSolution> solutions2 = deployerB.getSlotsSortedByConsolidationScore(
                slots2, nodesTable2, totalCpusToAdd, minCpus, maxCpus, 512, 10);
        System.out.println(solutions2);

        SlotSolution solution = 
            (bestProvider && solutions1.get(0).getConsolidationScore() >= solutions2.get(0).getConsolidationScore()) ?
                solutions1.get(0) : solutions2.get(0);

        System.out.println(solution);
    }
    
    public void testSlotAwareDeployment() throws Exception {
        boolean prepareExperiment = false;
        boolean runExperiment = false;
        boolean bestSlot = true;
        String imageId = "1e8d335f-e797-4d3b-aa28-20154d77006f";
        
        if(prepareExperiment){
            Vm vm01 = new Vm("slotAwareTest01", imageId, new VmRequirements( 6, 6*1024, 10, 0), this.generateScript(6), "slotAwareTest", "", "sla", "bscgrid30");
            vmm.deployVms(Arrays.asList(vm01));
            Vm vm02 = new Vm("slotAwareTest02", imageId, new VmRequirements( 10, 10*1024, 10, 0), this.generateScript(10), "slotAwareTest", "", "sla", "bscgrid29");
            vmm.deployVms(Arrays.asList(vm02));
        }
        
        if(runExperiment){
            Map<String, Node> nodesTable = new HashMap<String, Node>();
            for(Node n : vmm.getNodes()) {
                nodesTable.put(n.getHostname(), n);
            }

            System.out.println( nodesTable.toString() );
            List<Slot> slots = vmm.getSlots();
            System.out.println(slots);
            //List<Slot> slots = new ArrayList<>();
            //slots.add(new Slot("bscgrid30", 2, 200, 2000));
            //slots.add(new Slot("bscgrid29", 4, 400, 4000));
            //slots.add(new Slot("bscgrid28", 12, 1200, 12000));
            //slots.add(new Slot("bscgrid31", 12, 1200, 12000));

            int minCpus = 2;
            int maxCpus = 4;
            int totalCpusToAdd = 16;

            SlotAwareDeployer deployer = new SlotAwareDeployer();
            List<SlotSolution> solutions = deployer.getSlotsSortedByConsolidationScore(slots, nodesTable, totalCpusToAdd, minCpus, maxCpus, 512, 10);
            System.out.println(solutions);
            
            SlotSolution chosenSolution = (bestSlot) ? solutions.get(0) : solutions.get(solutions.size()-1);
            List<Slot> chosenSlots = chosenSolution.getSlots();
            for(Slot slot : chosenSlots) {
                System.out.println("Deploying a VM following this requirements: " + slot.toString());
                VmRequirements slotRequeriments = new VmRequirements( (int)slot.getFreeCpus(), (int)slot.getFreeMemoryMb(), (int)slot.getFreeDiskGb(), 0);
                Vm vm = new Vm("slotAwareInstance", imageId, slotRequeriments, this.generateScript(slotRequeriments.getCpus()), "slotAwareTest", "", "sla", slot.getHostname());
                vmm.deployVms(Arrays.asList(vm));
            }
        }
    }
    
    public void testSlotAwareDeploymentMultiProvider() throws Exception {
        boolean prepareExperiment = false;
        boolean runExperiment = false;
        boolean bestProvider = true;
        VmManagerClient vmm2 = new VmManagerClient(VMMConf.vmManagerURLSecondProvider);
        String imageId = "1e8d335f-e797-4d3b-aa28-20154d77006f";
        
        if(prepareExperiment){
            //bscgrid30 on providerA
            Vm vm01 = new Vm("slotAwareTest01", imageId, new VmRequirements(6, 6*1024, 10, 0), this.generateScript(6), "slotAwareTest", "", "sla", "bscgrid30");
            vmm.deployVms(Arrays.asList(vm01));
            
            //bscgrid29 on providerB
            Vm vm02 = new Vm("slotAwareTest02", imageId, new VmRequirements(7, 7*1024, 10, 0), this.generateScript(7), "slotAwareTest", "", "sla", "bscgrid29");
            vmm2.deployVms(Arrays.asList(vm02));
        }
        
        if(runExperiment){
            Map<String, Node> nodesTable1 = getNodesTable(vmm.getNodes());
            List<Slot> slots1 = vmm.getSlots();
            
            Map<String, Node> nodesTable2 =getNodesTable(vmm2.getNodes());
            List<Slot> slots2 = vmm2.getSlots();

            int minCpus = 2;
            int maxCpus = 4;
            int totalCpusToAdd = 10;

            SlotAwareDeployer deployerA = new SlotAwareDeployer("providerA");
            List<SlotSolution> solutions1 = deployerA.getSlotsSortedByConsolidationScore(
                    slots1, nodesTable1, totalCpusToAdd, minCpus, maxCpus, 512, 10);
            System.out.println(solutions1);
            
            SlotAwareDeployer deployerB = new SlotAwareDeployer("providerB");
            List<SlotSolution> solutions2 = deployerB.getSlotsSortedByConsolidationScore(
                    slots2, nodesTable2, totalCpusToAdd, minCpus, maxCpus, 512, 10);
            System.out.println(solutions2);
            
            SlotSolution chosenSolution = 
                (bestProvider && solutions1.get(0).getConsolidationScore() >= solutions2.get(0).getConsolidationScore()) ?
                    solutions1.get(0) : solutions2.get(0);
            VmManagerClient providerVMM = chosenSolution.getProvider().equals("providerA") ? vmm : vmm2;
            
            System.out.println(chosenSolution);
            List<Slot> chosenSlots = chosenSolution.getSlots();
            for(Slot slot : chosenSlots) {
                System.out.println("Deploying a VM following this requirements: " + slot.toString());
                VmRequirements slotRequeriments = new VmRequirements( (int)slot.getFreeCpus(), (int)slot.getFreeMemoryMb(), (int)slot.getFreeDiskGb(), 0);
                Vm vm = new Vm("slotAwareInstance", imageId, slotRequeriments, this.generateScript(slotRequeriments.getCpus()), "slotAwareTest", "", "sla", slot.getHostname());
                providerVMM.deployVms(Arrays.asList(vm));
            }
        }
    }
    
    private static final String END_OF_LINE = System.getProperty("line.separator");

    private String generateScript(int numCpus) {
        return "#cloud-config" + END_OF_LINE
                + "password: bsc" + END_OF_LINE
                + "chpasswd: { expire: False }" + END_OF_LINE
                + "ssh_pwauth: True" + END_OF_LINE
                + "runcmd:" + END_OF_LINE
                + " - stress -c " + numCpus + END_OF_LINE
                + " - sleep 5" + END_OF_LINE
                + END_OF_LINE;
    }
    
    private Map<String, Node> getNodesTable(List<Node> nodes) {
        Map<String, Node> nodesTable = new HashMap<String, Node>();
        for(Node n : nodes) {
            nodesTable.put(n.getHostname(), n);
        }
        
        return nodesTable;
    }
}
