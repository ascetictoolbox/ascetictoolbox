package net.atos.ari.seip.CloudManager;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import eu.optimis.manifest.api.sp.Manifest;
import eu.optimis.manifest.api.sp.VirtualMachineComponent;
import eu.optimis.types.xmlbeans.servicemanifest.XmlBeanServiceManifestDocument;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */

public class ThreeTierScalableCOOLEMALL 
    extends TestCase
{
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ThreeTierScalableCOOLEMALL( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ThreeTierScalableCOOLEMALL.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testthreetierManifest()
 
    {
    	
		//InputStream in = ManifestConstructionTest.class.getResourceAsStream( "/bursting.manifest.properties" );
		InputStream in = Manifest.class.getResourceAsStream( "/coolemall.manifest.properties" );

        Properties properties = new Properties();

        try
        {
        	properties.load( in );
            in.close();
        }
        catch (Exception ex)
        {
        	System.out.println ("Failure loading properties to create the manifest ");
        	ex.printStackTrace();
        }
        

        // now we can create a new manifest instance by using the properties file
        UUID serviceID = UUID.randomUUID();
        
        Manifest manifest =
            Manifest.Factory.newInstance( serviceID.toString(), "LB", properties );
        
        manifest.getTRECSection().getTrustSectionArray()[0].setMinimumTrustLevel(1);
        manifest.getTRECSection().getTrustSectionArray()[0].setSocialNetworkingTrustLevel(1);
        
        manifest.getTRECSection().getRiskSectionArray()[0].addNewAvailability("P1D", 98);
        manifest.getTRECSection().getRiskSectionArray()[0].addNewAvailability("P1M", 99.5);
        
        
        manifest.getDataProtectionSection().addNewEligibleCountry("ES");
        manifest.getDataProtectionSection().addNewEligibleCountry("UK");
        manifest.getDataProtectionSection().addNewEligibleCountry("DE");
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "MYSQL" );
        myComponent.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent.getOVFDefinition().getReferences().getImageFile().setHref( "MYSQL" );
        myComponent.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
   
      //Add Virtual Machine Component
        VirtualMachineComponent myComponent2 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS1" );
        myComponent2.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent2.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent2.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent2.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );

        manifest.getElasticitySection().addNewRule("JBOSS1", "ConcurrentConnections");
        manifest.getElasticitySection().getRuleArray()[0].setQuota(50);
        manifest.getElasticitySection().addNewRule("JBOSS1", "CPUSpeed");
        manifest.getElasticitySection().getRuleArray()[1].setQuota(2000);
        
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent3 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS2" );
        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent3.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent3.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent3.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent4 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS3" );
        myComponent4.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent4.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent4.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent4.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent5 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS4" );
        myComponent5.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent5.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent5.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent5.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent6 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS5" );
        myComponent6.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent6.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent6.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent6.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent7 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS6" );
        myComponent7.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent7.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent7.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent7.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent8 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS7" );
        myComponent8.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent8.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent8.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent8.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent9 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS8" );
        myComponent9.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent9.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent9.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent9.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent10 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS9" );
        myComponent10.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent10.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent10.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent10.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );

        //Add Virtual Machine Component
        VirtualMachineComponent myComponent11 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS10" );
        myComponent11.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent11.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent11.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent11.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        //Add Virtual Machine Component
        VirtualMachineComponent myComponent12 =
                manifest.getVirtualMachineDescriptionSection().addNewVirtualMachineComponent( "JBOSS11" );
        myComponent12.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setMemorySize( 1024 );
        myComponent12.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().setNumberOfVirtualCPUs(1);
        myComponent12.getOVFDefinition().getReferences().getImageFile().setHref( "JBOSS-context" );
        myComponent12.getOVFDefinition().getDiskSection().getImageDisk().setCapacity( "7380016" );
        
        
        XmlBeanServiceManifestDocument exportedXmlbeansObject = manifest.toXmlBeanObject();
        
        try
        {
        	exportedXmlbeansObject.save(new File ("d:\\3tierScalable.xml"));
        	System.out.println("Manifest created");
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }   
    
        	
    }
    

			
}
