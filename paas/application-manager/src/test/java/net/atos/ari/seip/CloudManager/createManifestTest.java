package net.atos.ari.seip.CloudManager;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import eu.optimis.manifest.api.sp.Manifest;
import eu.optimis.manifest.api.sp.VirtualMachineComponent;
//import eu.optimis.types.xmlbeans.servicemanifest.XmlBeanServiceManifestDocument;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */

public class createManifestTest 
    extends TestCase
{
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public createManifestTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( createManifestTest.class );
    }

//    /**
//     * Rigourous Test :-)
//     */
//    public void testcreateManifest()
// 
//    {
//    	
//		//InputStream in = ManifestConstructionTest.class.getResourceAsStream( "/bursting.manifest.properties" );
//		InputStream in = Manifest.class.getResourceAsStream( "/coolemall.manifest.properties" );
//
//        Properties properties = new Properties();
//
//        try
//        {
//        	properties.load( in );
//            in.close();
//        }
//        catch (Exception ex)
//        {
//        	System.out.println ("Failure loading properties to create the manifest ");
//        	ex.printStackTrace();
//        }
//        
//
//        // now we can create a new manifest instance by using the properties file
//
//        Manifest manifest =
//            Manifest.Factory.newInstance( "testingCreation", "benchmarkTS", properties );
//        
//        manifest.getTRECSection().getTrustSectionArray()[0].setMinimumTrustLevel(1);
//        manifest.getTRECSection().getTrustSectionArray()[0].setSocialNetworkingTrustLevel(1);
//        
//        manifest.getTRECSection().getRiskSectionArray()[0].addNewAvailability("P1D", 98);
//        manifest.getTRECSection().getRiskSectionArray()[0].addNewAvailability("P1M", 99.5);
//        
//        manifest.getElasticitySection().addNewRule("benchmarkTS", "Memory");
//        manifest.getElasticitySection().getRuleArray()[0].setQuota(2048);
//        manifest.getElasticitySection().addNewRule("benchmarkTS", "CPUSpeed");
//        manifest.getElasticitySection().getRuleArray()[1].setQuota(2000);
//        
//        manifest.getDataProtectionSection().addNewEligibleCountry("ES");
//        manifest.getDataProtectionSection().addNewEligibleCountry("UK");
//        manifest.getDataProtectionSection().addNewEligibleCountry("DE");
//        
//        //Add Virtual Machine Component
//        VirtualMachineComponent myComponent =
//                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "benchmarkTS2" );
//        myComponent.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 512 );
//        myComponent.getOVFDefinition().getReferences().getImageFile().setHref( "HA" );
//        myComponent.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
//        
//        manifest.getElasticitySection().addNewRule("benchmarkTS2", "Memory");
//        manifest.getElasticitySection().getRuleArray()[0].setQuota(2048);
//        manifest.getElasticitySection().addNewRule("benchmarkTS2", "CPUSpeed");
//        manifest.getElasticitySection().getRuleArray()[1].setQuota(2000);
//        
//        
//        XmlBeanServiceManifestDocument exportedXmlbeansObject = manifest.toXmlBeanObject();
//        
//        try
//        {
//        	exportedXmlbeansObject.save(new File ("c:\\tests\\test2.xml"));
//        	System.out.println("Manifest created");
//        }
//        catch (Exception ex)
//        {
//        	ex.printStackTrace();
//        }   
//    
//        	
//    }
    

			
}
