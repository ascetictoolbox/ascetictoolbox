package net.atos.ari.seip.CloudManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.atos.ari.seip.CloudManager.manager.CloudComputingManager;

/**
 * Unit test for simple App.
 */

public class createvmTest 
    extends TestCase
{
	//VM details
    private String image = "HAproxy";
    private String name = "Testactions2";
    private String cpu = "1";
    private String vcpu = "2";
    private String mem = "2048";
    private String networkID = "PSNC";
    
	// Secret
    private String oneuser = "oneadmin";
    private String onepwd = "opennebula";
//    private String oneauth = "oneadmin:52f288011f6fe0515429dc5717744d36";
	String oneauth = "oneadmin:opennebula";
	
	// CoolEmAll endpoint
	//String onerpc = "http://recs1.coolemall.eu:2633/RPC2";
	// CoolEmAll endpoint with tunnel
    private String onerpc = "http://localhost:2633/RPC2";
	// Local endpoint
	//	String onerpc = "http://192.168.252.19:2633/RPC2";	

    private String manifestpath = "/src/test/resources/DemoApp6Manifest.xml";
	

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public createvmTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( createvmTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testCreationVM()
 
    {
    		System.out.println("Connecting ...");
    		System.out.println("...");
    		System.out.println("...");
    		
    		CloudComputingManager ccManager = new CloudComputingManager(oneauth, onerpc);
    		ccManager.testUserConnection();    
        	
        	String template = " NAME   = " +
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
        	System.out.println("TEMPLATE" + template);

        	ccManager.createInstance(template);        	
    }
    
}
