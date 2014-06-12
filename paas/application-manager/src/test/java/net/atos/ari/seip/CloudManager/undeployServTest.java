package net.atos.ari.seip.CloudManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.atos.ari.seip.CloudManager.REST.service.ServiceService;

/**
 * Unit test for simple App.
 */

public class undeployServTest 
    extends TestCase
{

	// Secret   
	String oneauth = "oneadmin:opennebula";	
	// Local endpoint with tunnel
    private String onerpc = "http://localhost:2633/RPC2";
    // String serviceID = "eee63f63-7642-4e6b-89eb-c0c415857c5e";
    String serviceID = "fd7342e8-9fa3-480f-958d-33f84dde3d7a";



    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public undeployServTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( undeployServTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testUndeploy()
 
    {
    

    
/*    InstanceInfoDAO dao = new InstanceInfoDAO();
    ServiceInfoDAO sdao = new ServiceInfoDAO();
    int idserviceInfo = 0;
    String idserviceInfoS;
    
    try {
		idserviceInfo = sdao.getService("eee63f63-7642-4e6b-89eb-c0c415857c5d").getIdserviceInfo();
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    
    try {
    	idserviceInfoS = Integer.toString(idserviceInfo);
		List<String> list = dao.getInstancesByServiceID(idserviceInfoS);
		for (String vm : list)
		{
			System.out.println(vm);
			dao.deleteInstance(vm);
			//dao.deleteInstance("512");
		}	
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    try {
    	
		sdao.deleteService("eee63f63-7642-4e6b-89eb-c0c415857c5d");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
    String result = new ServiceService(oneauth, onerpc).UndeployService(serviceID);
    System.out.println("Result:" + result);
 	
    }
    
}
