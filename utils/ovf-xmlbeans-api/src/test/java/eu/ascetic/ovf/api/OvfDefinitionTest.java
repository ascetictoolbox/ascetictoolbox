/**
 *  Copyright 2014 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.ovf.api;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Vector;

import org.apache.xmlbeans.XmlOptions;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;

import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.Network;
import eu.ascetic.utils.ovf.api.NetworkSection;
import eu.ascetic.utils.ovf.api.OperatingSystem;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.References;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.enums.DiskFormatType;
import eu.ascetic.utils.ovf.api.enums.OperatingSystemType;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;
import eu.ascetic.utils.ovf.api.enums.ResourceType;

/**
 * Workflow unit tests testing core functionality of the OVF API.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class OvfDefinitionTest extends TestCase {

    /**
     * Tests the creation of an OVF document using a Velocity template.
     */
    public void testOvfDefinitionViaTemplate() {
        OvfDefinition ovfDefinition = OvfDefinition.Factory.newInstance(
                "threeTierWebApp", "/DFS/ascetic/vm-images/3tierweb");

        // Global product details

        // Stores the Application's ID
        String applicationId = ovfDefinition.getVirtualSystemCollection()
                .getId();
        assertNotNull(applicationId);
        ;
        ovfDefinition.getVirtualSystemCollection().getProductSectionAtIndex(0)
                .setDeploymentId("101");
        String deploymentId = ovfDefinition.getVirtualSystemCollection()
                .getProductSectionAtIndex(0).getDeploymentId();
        assertNotNull(deploymentId);

        // @formatter:off
        ovfDefinition.getVirtualSystemCollection().getProductSectionAtIndex(0)
                    .setSecurityKeys("\n        " +
            "-----BEGIN PUBLIC KEY-----\n        " +
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0\n        " +
            "FPqri0cb2JZfXJ/DgYSF6vUpwmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/\n        " +
            "3j+skZ6UtW+5u09lHNsj6tQ51s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQAB\n        " +
            "-----END PUBLIC KEY-----\n        " +
            "-----BEGIN RSA PRIVATE KEY-----\n        " +
            "MIICXAIBAAKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0FPqri0cb2JZfXJ/DgYSF6vUp\n        " +
            "wmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/3j+skZ6UtW+5u09lHNsj6tQ5\n        " +
            "1s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQABAoGAFijko56+qGyN8M0RVyaRAXz++xTqHBLh\n        " +
            "3tx4VgMtrQ+WEgCjhoTwo23KMBAuJGSYnRmoBZM3lMfTKevIkAidPExvYCdm5dYq3XToLkkLv5L2\n        " +
            "pIIVOFMDG+KESnAFV7l2c+cnzRMW0+b6f8mR1CJzZuxVLL6Q02fvLi55/mbSYxECQQDeAw6fiIQX\n        " +
            "GukBI4eMZZt4nscy2o12KyYner3VpoeE+Np2q+Z3pvAMd/aNzQ/W9WaI+NRfcxUJrmfPwIGm63il\n        " +
            "AkEAxCL5HQb2bQr4ByorcMWm/hEP2MZzROV73yF41hPsRC9m66KrheO9HPTJuo3/9s5p+sqGxOlF\n        " +
            "L0NDt4SkosjgGwJAFklyR1uZ/wPJjj611cdBcztlPdqoxssQGnh85BzCj/u3WqBpE2vjvyyvyI5k\n        " +
            "X6zk7S0ljKtt2jny2+00VsBerQJBAJGC1Mg5Oydo5NwD6BiROrPxGo2bpTbu/fhrT8ebHkTz2epl\n        " +
            "U9VQQSQzY1oZMVX8i1m5WUTLPz2yLJIBQVdXqhMCQBGoiuSoSjafUhV7i1cEGpb88h5NBYZzWXGZ\n        " +
            "37sJ5QsW+sJyoNde3xH8vdXhzU7eT82D6X/scw9RZz+/6rCJ4p0=\n        " +
            "-----END RSA PRIVATE KEY-----");
        // @formatter:on

        ovfDefinition
                .getVirtualSystemCollection()
                .getProductSectionAtIndex(0)
                .addNewProperty("asceticWorkloadVmId",
                        ProductPropertyType.STRING, "jmeter");
        ovfDefinition
                .getVirtualSystemCollection()
                .getProductSectionAtIndex(0)
                .addNewProperty("asceticWorkloadType",
                        ProductPropertyType.STRING, "user-count");
        ovfDefinition
                .getVirtualSystemCollection()
                .getProductSectionAtIndex(0)
                .addNewProperty("asceticWorkloadRange",
                        ProductPropertyType.STRING, "10-200");
        ovfDefinition
                .getVirtualSystemCollection()
                .getProductSectionAtIndex(0)
                .addNewProperty("asceticWorkloadIncrement",
                        ProductPropertyType.STRING, "10");
        ovfDefinition
                .getVirtualSystemCollection()
                .getProductSectionAtIndex(0)
                .addNewProperty("asceticWorkloadInterval",
                        ProductPropertyType.STRING, "1min");

        // Virtual Machine product details

        // Stores the Virtual Machine's ID
        String virtualMachineId = ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getId();
        assertNotNull(virtualMachineId);
        ovfDefinition
                .getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .addNewProperty("asceticProbeUri-1",
                        ProductPropertyType.STRING,
                        "uri://some-end-point/application-monitor");
        String probeUri = ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getPropertyByKey("asceticProbeUri-1").getValue();
        assertNotNull(probeUri);
        ovfDefinition
                .getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .addNewProperty("asceticProbeType-1",
                        ProductPropertyType.STRING, "cpu");
        ovfDefinition
                .getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .addNewProperty("asceticProbeInterval-1",
                        ProductPropertyType.STRING, "1sec");

        System.out.println(ovfDefinition.toString());

        writeToFile(ovfDefinition.getXmlObject(), "3tier-webapp.ovf");
        
        OvfDefinition ovfDefinition2 = OvfDefinition.Factory.newInstance(ovfDefinition.toString());
        assertFalse(ovfDefinition2.hasErrors());
    }

    /**
     * Tests the creation of an OVF document using Factory methods.
     */
    public void testOvfDefinitionViaFactory() {
        OvfDefinition ovfDefinition = OvfDefinition.Factory.newInstance();

        // File references
        References references = References.Factory.newInstance();
        File file = File.Factory.newInstance("file", "/some/location");
        file.setSize(new BigInteger("1000000000"));
        references.addFile(file);
        ovfDefinition.setReferences(references);

        // Disk section
        DiskSection diskSection = DiskSection.Factory.newInstance();
        diskSection.setInfo("List of the virtual disks used.");
        Disk disk = Disk.Factory.newInstance();
        disk.setDiskId("disk");
        disk.setFileRef("file");
        disk.setFormat(DiskFormatType.QCOW2);
        disk.setCapacityAllocationUnits("byte * 2^20");
        disk.setCapacity("900");
        diskSection.addDisk(disk);
        ovfDefinition.setDiskSection(diskSection);

        // Network section
        NetworkSection networkSection = NetworkSection.Factory.newInstance();
        networkSection.setInfo("List of the virtual networks used.");
        Network network = Network.Factory.newInstance();
        network.setName("network");
        network.setDescription("A virtual machine network");
        networkSection.addNetwork(network);
        ovfDefinition.setNetworkSection(networkSection);

        // Virtual System Collection
        VirtualSystemCollection virtualSystemCollection = VirtualSystemCollection.Factory
                .newInstance();
        virtualSystemCollection.setId("factory-test");
        virtualSystemCollection.setInfo("Factory test description.");
        // Product Section
        ProductSection productSection = ProductSection.Factory.newInstance();
        productSection.setInfo("Product configuration for then entire VirtualSystemCollection.");
        productSection.setProduct("product");
        productSection.setVersion("1.0");
        productSection.addNewProperty("key", ProductPropertyType.STRING,
                "value");
        virtualSystemCollection.addProductSection(productSection);

        // Virtual System
        VirtualSystem virtualSystem = VirtualSystem.Factory.newInstance();
        virtualSystem.setId("id");
        virtualSystem.setInfo("Factory Test Virtual System.");
        OperatingSystem operatingSystem = OperatingSystem.Factory.newInstance();
        operatingSystem.setInfo("Description of Operating System.");
        operatingSystem
                .setId(OperatingSystemType.MICROSOFT_WINDOWS_SERVER_2008);
        operatingSystem.setVersion("R2");
        virtualSystem.setOperatingSystem(operatingSystem);

        // Product Section
        ProductSection productSection2 = ProductSection.Factory.newInstance();
        productSection2.setInfo("Product configuration for the VirtualSystem.");
        productSection2.setProduct("product2");
        productSection2.setVersion("2.0");
        productSection2.setLowerBound(1);
        productSection2.setUpperBound(5);
        Vector<ProductSection> productSections = new Vector<ProductSection>();
        productSections.add(productSection2);
        virtualSystem.setProductSectionArray(productSections
                .toArray(new ProductSection[productSections.size()]));

        // Virtual Hardware Section
        VirtualHardwareSection virtualHardwareSection = VirtualHardwareSection.Factory
                .newInstance();
        virtualHardwareSection.setInfo("Description of virtual hardware requirements.");
        eu.ascetic.utils.ovf.api.System system = eu.ascetic.utils.ovf.api.System.Factory
                .newInstance();
        // System
        system.setElementName("Virtual System Type");
        system.setInstanceID("0");
        system.setVirtualSystemType("kvm");
        virtualHardwareSection.setSystem(system);

        // CPU Number
        Item itemCpuNumber = Item.Factory.newInstance();
        itemCpuNumber.setDescription("Number of virtual CPUs");
        itemCpuNumber.setElementName("1 virtual CPU");
        itemCpuNumber.setInstanceId("1");
        itemCpuNumber.setResourceType(ResourceType.PROCESSOR);
        itemCpuNumber.setVirtualQuantity(new BigInteger("1"));
        virtualHardwareSection.addItem(itemCpuNumber);

        // CPU Speed
        Item itemCpuSpeed = Item.Factory.newInstance();
        itemCpuSpeed.setDescription("CPU Speed");
        itemCpuSpeed.setElementName("2000 MHz CPU speed reservation");
        itemCpuSpeed.setInstanceId("1");
        itemCpuSpeed.setResourceType(ResourceType.PROCESSOR);
        itemCpuSpeed.setResourceSubType("cpuspeed");
        itemCpuSpeed.setAllocationUnits("hertz * 2^20");
        itemCpuSpeed.setReservation(new BigInteger("2000"));
        virtualHardwareSection.addItem(itemCpuSpeed);

        // Memory
        Item itemMemory = Item.Factory.newInstance();
        itemMemory.setDescription("Memory Size");
        itemMemory.setElementName("512 MB of memory");
        itemMemory.setInstanceId("2");
        itemMemory.setResourceType(ResourceType.MEMORY);
        itemCpuSpeed.setAllocationUnits("byte * 2^20");
        itemMemory.setVirtualQuantity(new BigInteger("512"));
        virtualHardwareSection.addItem(itemMemory);

        // Network
        Item itemNetwork = Item.Factory.newInstance();
        itemNetwork.setDescription("Virtual Network");
        itemNetwork.addConnection("network");
        itemNetwork.setElementName("Ethernet adapter on network");
        itemNetwork.setInstanceId("3");
        itemNetwork.setResourceType(ResourceType.ETHERNET_ADAPTER);
        itemNetwork.setAutomaticAllocation(true);
        virtualHardwareSection.addItem(itemNetwork);

        // Disk
        Item itemDisk = Item.Factory.newInstance();
        itemDisk.setDescription("VM Disk");
        itemDisk.setElementName("VM Disk Drive 1");
        itemDisk.setInstanceId("4");
        itemDisk.setResourceType(ResourceType.DISK_DRIVE);
        itemDisk.addHostResource("ovf:/disk/disk");
        virtualHardwareSection.addItem(itemDisk);

        virtualSystem.setVirtualHardwareSection(virtualHardwareSection);

        Vector<VirtualSystem> virtualSystems = new Vector<VirtualSystem>();
        virtualSystems.add(virtualSystem);
        virtualSystemCollection.setVirtualSystemArray(virtualSystems
                .toArray(new VirtualSystem[virtualSystems.size()]));

        ovfDefinition.setVirtualSystemCollection(virtualSystemCollection);

        System.out.println(ovfDefinition.toString());

        writeToFile(ovfDefinition.getXmlObject(), "factory-test.ovf");
        
        OvfDefinition ovfDefinition2 = OvfDefinition.Factory.newInstance(ovfDefinition.toString());
        assertFalse(ovfDefinition2.hasErrors());
    }

    /**
     * Writes out an OVF Document to a file name consolidating name spaces and
     * their prefixes.
     * 
     * @param ovfDefinition
     *            The OVF Definition
     * @param fileName
     *            The file name to write out to
     */
    protected void writeToFile(XmlBeanEnvelopeDocument ovfDefinition,
            String fileName) {
        try {
            // If system property is not set (i.e. test case was started from
            // IDE )
            // we use the current directory to store the file
            String targetDir = System.getProperty("ovfSampleDir", "target");

            java.io.File file = new java.io.File(targetDir
                    + java.io.File.separator + java.io.File.separator
                    + fileName + ".xml");
            FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);

            // XML options to make the file more readable
            XmlOptions options = new XmlOptions();
            options.setSavePrettyPrint();
            // Setup the prefixes
            HashMap<String, String> suggestedPrefixes = new HashMap<String, String>();
            suggestedPrefixes
                    .put("http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData",
                            "vssd");
            suggestedPrefixes
                    .put("http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData",
                            "rasd");
            suggestedPrefixes.put("http://schemas.dmtf.org/ovf/envelope/1",
                    "ovf");
            options.setSaveSuggestedPrefixes(suggestedPrefixes);
            // Make sure name spaces are aggressively resolved
            options.setSaveAggressiveNamespaces();

            out.write(ovfDefinition.xmlText(options));
            System.out.println(fileName + ".xml was written to "
                    + file.getAbsolutePath());
            // Close the output stream
            out.close();
        } catch (Exception e) {
            // Catch exception if any
            System.err.println("Error: " + e.getMessage());
            fail(e.getMessage());
        }
    }
}
