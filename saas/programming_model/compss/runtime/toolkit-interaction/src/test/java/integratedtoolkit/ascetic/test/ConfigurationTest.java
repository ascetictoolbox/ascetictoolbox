package integratedtoolkit.ascetic.test;

import integratedtoolkit.ITConstants;
import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.ascetic.Configuration;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.components.ResourceUser;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.MethodImplementation;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.updates.ResourceUpdate;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ResourceManager;
import java.util.LinkedList;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigurationTest {

    public ConfigurationTest() {

    }

    @BeforeClass
    public static void setUpClass() {
    /*    System.setProperty("realValues", "false");
        System.setProperty("discoveryPeriod", "500");
        System.setProperty(ITConstants.IT_TRACING, "0");
        System.setProperty(ITConstants.COMM_ADAPTOR, "integratedtoolkit.nio.master.NIOAdaptor");

        System.setProperty(ITConstants.IT_CONTEXT, "/home/flordan/ascetic/testbedY3/master/mnt/context");
        System.setProperty(ITConstants.IT_PROJ_FILE, "/home/flordan/ascetic/testbedY3/master/ascetic_service/project.xml");
        System.setProperty(ITConstants.IT_RES_FILE, "/home/flordan/ascetic/testbedY3/master/ascetic_service/resources.xml");
        System.setProperty(ITConstants.IT_PROJ_SCHEMA, "/home/flordan/ascetic/testbedY3/master/ascetic_service/project_schema.xsd");
        System.setProperty(ITConstants.IT_RES_SCHEMA, "/home/flordan/ascetic/testbedY3/master/ascetic_service/resources_schema.xsd");

        ConsoleAppender console = new ConsoleAppender();
        Logger.getRootLogger().setLevel(Level.INFO);
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
        Comm.init();
        CoreManager.resizeStructures(3);

        //Register Core runEPlusJobOptimized
        String[] signatures = new String[]{
            "runEPlusJobOptimized(FILE_T,FILE_T,FILE_T,STRING_T,STRING_T,STRING_T,STRING_T,INT_T,STRING_T,FILE_T,FILE_T)jeplus.worker.JEPlusImplOptimized"
        };
        Integer coreId = CoreManager.getCoreId(signatures);
        if (coreId == CoreManager.getCoreCount()) {
            CoreManager.increaseCoreCount();
        }
        Implementation[] implementations = new Implementation[signatures.length];
        implementations[0] = new MethodImplementation("jeplus.worker.JEPlusImplOptimized", coreId, 0, new MethodResourceDescription());
        CoreManager.registerImplementations(coreId, implementations, signatures);

        //Register Core runEPlusJobNormal
        signatures = new String[]{
            "runEPlusJobNormal(FILE_T,FILE_T,FILE_T,STRING_T,STRING_T,STRING_T,STRING_T,INT_T,STRING_T,FILE_T,FILE_T)jeplus.worker.JEPlusImpl",};
        coreId = CoreManager.getCoreId(signatures);
        if (coreId == CoreManager.getCoreCount()) {
            CoreManager.increaseCoreCount();
        }
        implementations = new Implementation[signatures.length];
        implementations[0] = new MethodImplementation("jeplus.worker.JEPlusImpl", coreId, 0, new MethodResourceDescription());
        CoreManager.registerImplementations(coreId, implementations, signatures);

        //Register Core runEPlusJob
        signatures = new String[]{
            "runEPlusJob(FILE_T,FILE_T,FILE_T,STRING_T,STRING_T,STRING_T,STRING_T,INT_T,INT_T,STRING_T,FILE_T,FILE_T)jeplus.worker.JEPlusImplOptimized",
            "runEPlusJob(FILE_T,FILE_T,FILE_T,STRING_T,STRING_T,STRING_T,STRING_T,INT_T,INT_T,STRING_T,FILE_T,FILE_T)jeplus.worker.JEPlusImpl"
        };
        coreId = CoreManager.getCoreId(signatures);
        if (coreId == CoreManager.getCoreCount()) {
            CoreManager.increaseCoreCount();
        }
        implementations = new Implementation[signatures.length];
        implementations[0] = new MethodImplementation("jeplus.worker.JEPlusImplOptimized", coreId, 0, new MethodResourceDescription());
        implementations[1] = new MethodImplementation("jeplus.worker.JEPlusImpl", coreId, 1, new MethodResourceDescription());
        CoreManager.registerImplementations(coreId, implementations, signatures);


        try {
            ResourceManager.load(new Status());
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
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

    //@Test
    public void ovfParsing() throws InterruptedException {
        if (!Configuration.getApplicationId().equals("JEPlus")) {
            fail("Application Id not properly read");
        }
        if (!Configuration.getDeploymentId().equals("893")) {
            fail("Deployment Id not properly read");
        }
        if (!Configuration.getApplicationManagerEndpoint().equals("http://192.168.3.222/application-manager")) {
            fail("AppManager endpoint not properly read");
        }
        if (!Configuration.getApplicationMonitorEndpoint().equals("http://192.168.3.222:9000")) {
            fail("AppMonitor endpoint not properly read");
        }
        System.out.println(Ascetic.getEconomicalBoundary() + " -- " + Ascetic.getEnergyBoundary());
        if (Ascetic.getEconomicalBoundary() != 2.0) {
            fail("Economic boundary not properly read");
        }
        if (Ascetic.getEnergyBoundary() != 300.0) {
            fail("Energy boundary not properly read");
        }
    }

   // @Test
    public void ConfParsingTest() throws InterruptedException {
        Ascetic.getEconomicalBoundary();
        if (workersDetected.size() > 0) {
            fail("Was suposed to have no workers");
        }
        synchronized (workersDetected) {
            workersDetected.wait();
        }
        if (workersDetected.size() != 1) {
            fail("Was suposed to have one worker. Obtained:" + workersDetected);
            System.out.println(workersDetected.get(0).getName());
            if (workersDetected.get(0).getName().equals("COMPSsWorker01")) {
                fail("COMPSsWorker01 should have been detected");
            }

        }
        synchronized (workersDetected) {
            workersDetected.wait();
        }
        if (workersDetected.size() != 2) {
            fail("Was suposed to have two workers. Obtained:" + workersDetected);
            if (workersDetected.get(0).getName().equals("COMPSsWorker01")) {
                fail("COMPSsWorker01 should have been detected");
            }
            if (workersDetected.get(1).getName().equals("COMPSsWorker02")) {
                fail("COMPSsWorker02 should have been detected");
            }
        }
    }

    static LinkedList<Worker> workersDetected = new LinkedList<Worker>();

    private static void addWorker(Worker<?> r) {
        workersDetected.add(r);
        synchronized (workersDetected) {
            workersDetected.notify();
        }
    }

    public static class Status implements ResourceUser {

        @Override
        public void updatedResource(Worker<?> r, ResourceUpdate ru) {
            ConfigurationTest.addWorker(r);
        }
    }
}
