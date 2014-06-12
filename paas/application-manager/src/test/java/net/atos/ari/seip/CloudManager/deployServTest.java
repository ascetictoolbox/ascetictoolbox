package net.atos.ari.seip.CloudManager;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.atos.ari.seip.CloudManager.REST.service.ServiceService;

/**
 * Unit test for simple App.
 */

public class deployServTest 
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
	String oneauth = "oneadmin:opennebula";
	
	// CoolEmAll endpoint
	//String onerpc = "http://recs1.coolemall.eu:2633/RPC2";
	// CoolEmAll endpoint with tunnel
    private String onerpc = "http://localhost:2633/RPC2";
	// Local endpoint
	//	String onerpc = "http://192.168.252.19:2633/RPC2";

	

   // private String manifestpath = "/src/test/resources/DemoApp6Manifest.xml";
  //  private String manifestpath = "D:\\test2.xml";

   //private String manifestpath = "D:\\3tierScalable.xml";
    private String manifestpath = "D:\\jmetermanifsetCOOLEMALL.xml";


    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public deployServTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( deployServTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testdeploy()
 
    {
    File manifestFile = new File(manifestpath);
    
    new ServiceService(oneauth, onerpc).DeployService(new ServiceService(oneauth, onerpc).parseSPServiceManifest(manifestFile));
    
        	
    }
    

			
}
