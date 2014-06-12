package net.atos.ari.seip.CloudManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.atos.ari.seip.CloudManager.manager.CloudComputingManager;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
	
//	String baseurl = "http://192.168.252.47:3001";
//	String baseurl = "http://172.24.100.28:3001";
	String oneauth = "oneadmin:opennebula";
	String onerpc = "http://localhost:2633/RPC2";
	String manifestpath = "/src/test/resources/DemoApp6Manifest.xml";
	int vmToTest;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	//TODO assign value to vmToTest attribute
    	CloudComputingManager ccManager = new CloudComputingManager(oneauth, onerpc);
    	String info = ccManager.getinfoInstance(vmToTest);
    	System.out.println(info);

    }
    
}
