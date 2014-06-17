package net.atos.ari.seip.CloudManager;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import eu.optimis.manifest.api.sp.Manifest;
import eu.optimis.manifest.api.sp.VirtualMachineComponent;
//import eu.optimis.types.xmlbeans.servicemanifest.XmlBeanServiceManifestDocument;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */

public class jmeterCOOLEMALLManifestTest 
    extends TestCase
{
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public jmeterCOOLEMALLManifestTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( jmeterCOOLEMALLManifestTest.class );
    }

//    /**
//     * Rigourous Test :-)
//     */
//    public void testthreetierManifest()
// 
//    {
//    	
//		//InputStream in = ManifestConstructionTest.class.getResourceAsStream( "/bursting.manifest.properties" );
//		InputStream in = Manifest.class.getResourceAsStream( "/coolemall.jmeter.properties" );
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
//        UUID serviceID = UUID.randomUUID();
//        
//        Manifest manifest =
//            Manifest.Factory.newInstance( serviceID.toString(), "stressUsers", properties );
//        
//        manifest.getTRECSection().getTrustSectionArray()[0].setMinimumTrustLevel(1);
//        manifest.getTRECSection().getTrustSectionArray()[0].setSocialNetworkingTrustLevel(1);
//        
//        manifest.getTRECSection().getRiskSectionArray()[0].addNewAvailability("P1D", 98);
//        manifest.getTRECSection().getRiskSectionArray()[0].addNewAvailability("P1M", 99.5);
//        
//        
//        manifest.getDataProtectionSection().addNewEligibleCountry("ES");
//        manifest.getDataProtectionSection().addNewEligibleCountry("UK");
//        manifest.getDataProtectionSection().addNewEligibleCountry("DE");
//        
//        //Add Virtual Machine Component
//        VirtualMachineComponent myComponent =
//                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "stressUsers0" );
//        myComponent.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
//        myComponent.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
//        myComponent.getOVFDefinition().getReferences().getImageFile().setHref( "SL63v2" );
//        myComponent.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
//        
//        manifest.getElasticitySection().addNewRule("stressUsers", "ConcurrentConnections");
//        manifest.getElasticitySection().getRuleArray()[0].setQuota(50);
//        manifest.getElasticitySection().addNewRule("stressUsers", "CPUSpeed");
//        manifest.getElasticitySection().getRuleArray()[1].setQuota(2000);
//        
//      //Add Virtual Machine Component
//        VirtualMachineComponent myComponent2 =
//                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "stressUsers1" );
//        myComponent2.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
//        myComponent2.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
//        myComponent2.getOVFDefinition().getReferences().getImageFile().setHref( "SL63v2" );
//        myComponent2.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
//
//        
//        //Add Virtual Machine Component
//        VirtualMachineComponent myComponent3 =
//                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "stressUsers2" );
//        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
//        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
//        myComponent3.getOVFDefinition().getReferences().getImageFile().setHref( "SL63v2" );
//        myComponent3.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
//        
//        //Add Virtual Machine Component
//         myComponent3 =
//                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "stressUsers3" );
//        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
//        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
//        myComponent3.getOVFDefinition().getReferences().getImageFile().setHref( "SL63v2" );
//        myComponent3.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
//        
//        //Add Virtual Machine Component
//         myComponent3 =
//                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "stressUsers4" );
//        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
//        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
//        myComponent3.getOVFDefinition().getReferences().getImageFile().setHref( "SL63v2" );
//        myComponent3.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
//        //Add Virtual Machine Component
//         myComponent3 =
//                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "stressUsers5" );
//        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
//        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
//        myComponent3.getOVFDefinition().getReferences().getImageFile().setHref( "SL63v2" );
//        myComponent3.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
//        //Add Virtual Machine Component
//
//
//        
//        XmlBeanServiceManifestDocument exportedXmlbeansObject = manifest.toXmlBeanObject();
//        
//        try
//        {
//        	exportedXmlbeansObject.save(new File ("d:\\jmetermanifsetCOOLEMALL.xml"));
//        	System.out.println("Manifest created");
//        }
//        catch (Exception ex)
//        {
//        	ex.printStackTrace();
//        }   
//    
//        	
//    }
//    

			
}
