package net.atos.ari.seip.CloudManager;

import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.atos.ari.seip.CloudManager.manager.CloudComputingManager;
//import com.sun.jersey.api.client.Client;

/**
 * Unit test for simple App.
 */

public class connectionTest 
    extends TestCase
{
	
	// Secret
	String oneauth = "oneadmin:opennebula";
	// CoolEmAll endpoint with tunnel
	String onerpc = "http://localhost:2633/RPC2";	

	String manifestpath = "/src/test/resources/DemoApp6Manifest.xml";
	int vmToTest = 456;
	int templateToTest =3;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public connectionTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( connectionTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testConnection()
 
    {
//        	OneDriver od = new OneDriver(oneuser+":"+onepwd, onerpc);
//        	od.listUsers();
    		System.out.println("Connecting ...");
    		System.out.println("...");
    		System.out.println("...");
    		
    		CloudComputingManager ccManager = new CloudComputingManager(oneauth, onerpc);
    		ccManager.testUserConnection();
    		
        	String vmTemplate =
        	          "NAME     = vm_from_java    CPU = 0.1    MEMORY = 64\n"
        	        + "DISK     = [\n"
        	        + "\tsource   = \"/var/lib/one/datastores/101/test.im	g\",\n"
        	        + "\ttarget   = \"hda\",\n"
        	        + "\treadonly = \"no\" ]\n"
        	        + "FEATURES = [ acpi=\"no\" ]";
     
    String image = "HAproxy";
    String name = "Testingcl";
    String cpu = "1";
    String vcpu = "2";
    String mem = "2048";
    String networkID = "cloud";
        	
    String Template = " NAME   = " +
    		name +
    		"\n" +
    		"CPU    = " +
    		cpu +
    		"\n" +
    		"VCPU   = " +
    		vcpu +
    		"\n" +
    		"MEMORY = " +
    		mem +
    		"\n" +
    		"OS = [ \n" +
    		"\tARCH = \"x86_64\",\n" +
    		"\tMACHINE = \"rhel6.3.0\",\n" +
    		"\tBOOT = \"hd\"  ]\n" +
    		"DISK = [ \n" +
    		"\tIMAGE = \"" +
    		image +
    		"\",\n" +
    		"\tIMAGE_UNAME = \"oneadmin\",\n" +
    		"BUS = \"virtio\",\n" +
    		"DRIVER = \"qcow2\" ]\n" +
    		"NIC = [ \n" +
    		"\tNETWORK = \"" +
    		networkID +
    		"\",\n" +
    		"NETWORK_UNAME = \"oneadmin\",\n" +
    		"MODEL = \"virtio\" ]\n" +
    		"RAW = [\n" +
    		"\ttype = \"kvm\",\n" +
    		"\tdata = \"<devices>\n" +
    		"\t<serial type='pty'>\n" +
    		"\t<source path='/dev/pts/1'/>\n" +
    		"\t<target port='0'/>\n" +
    		"\t<alias name='serial0'/>\n" +
    		"\t</serial>\n" +
    		"\t<console type='pty' tty='/dev/pts/1'>\n" +
    		"\t<source path='/dev/pts/1'/>\n" +
    		"\t<target type='serial' port='0'/>\n" +
    		"\t<alias name='serial0'/>\n" +
    		"\t</console>\n" +
    		"\t</devices>\"\n" +
    		"]";
    System.out.println("TEMPLATE" + Template);
    
    //Create instance
    String newID = ccManager.createInstance(Template);

    try {
    	TimeUnit.MINUTES.sleep(1);
    } catch (InterruptedException e) {
    	e.printStackTrace();
    }

    //Get status
    String status = ccManager.getStatusInstance(Integer.parseInt(newID));
    System.out.println(status);

    try {
    	TimeUnit.MINUTES.sleep(2);
    } catch (InterruptedException e) {
    	e.printStackTrace();
    }

    //delete instance
    ccManager.deleteInstance(Integer.parseInt(newID));

    try {
    	TimeUnit.MINUTES.sleep(2);
    } catch (InterruptedException e) {
    	// TODO Auto-generated catch block
    	e.printStackTrace();
    }

    //get status
    System.out.println(ccManager.getStatusInstance(Integer.parseInt(newID)));


    System.out.println("=================================");
    System.out.println("=================================");

    System.out.println("=================================");

    System.out.println("=================================");

    System.out.println("=================================");

    System.out.println("=================================");

    ccManager.testVirtualMachinePool();    

    //get Info from instance
    String instanceInfo = ccManager.getinfoInstance(vmToTest);
    System.out.println("instance info: " + instanceInfo);

    //get Info from template
    String templateInfo = ccManager.getInfoTemplate(templateToTest);
    System.out.println("template info: " + templateInfo);     	
    

    }
    
//    public void testManifestValues() throws IOException{
//    	String strManifest = readFileAsString(System.getProperty("user.dir")+manifestpath);
//    	Manifest mani = Manifest.Factory.newInstance(strManifest);
//    	System.out.println(mani.getVirtualMachineDescriptionSection().getServiceId());
//    	mani.getVirtualMachineDescriptionSection().getAffinityRules();
//    }
//    
//    private String readFileAsString(String filePath)
//			throws java.io.IOException {
//		byte[] buffer = new byte[(int) new File(filePath).length()];
//		BufferedInputStream f = null;
//		try {
//			f = new BufferedInputStream(new FileInputStream(filePath));
//			f.read(buffer);
//		} finally {
//			if (f != null)
//				try {
//					f.close();
//				} catch (IOException ignored) {
//				}
//		}
//		return new String(buffer);
//	}
			
}
