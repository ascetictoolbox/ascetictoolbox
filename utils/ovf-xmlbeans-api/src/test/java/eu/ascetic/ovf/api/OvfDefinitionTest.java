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
                "threeTierWebApp", "/DFS/ascetic/vm-images/threeTierWebApp");

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

        // TODO: add this to the template
        String publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQD/TKK8H1TmlbdAVFVRJ3CZo6Lu8ZRzJy/jtTpQ83Yfjh8rFifOU1t39e9QZnbVrZ9ez5NA63WJh/Fwf2qEiaVBez80FaNR3xVVPl5xbZx1D+sfPJaoL4Y6JJ90Zey+ZO7Feb4bHpfGFm72e72mNg8nS0dbUJrJsMCdmF7CFsKlSQ== ascetic-public-key";
        ovfDefinition.getVirtualSystemCollection().getProductSectionAtIndex(0)
                .setPublicSshKey(publicKey);
        assertEquals(publicKey, ovfDefinition.getVirtualSystemCollection()
                .getProductSectionAtIndex(0).getPublicSshKey());
        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIICXgIBAAKBgQD/TKK8H1TmlbdAVFVRJ3CZo6Lu8ZRzJy/jtTpQ83Yfjh8rFifO\n"
                + "U1t39e9QZnbVrZ9ez5NA63WJh/Fwf2qEiaVBez80FaNR3xVVPl5xbZx1D+sfPJao\n"
                + "L4Y6JJ90Zey+ZO7Feb4bHpfGFm72e72mNg8nS0dbUJrJsMCdmF7CFsKlSQIDAQAB\n"
                + "AoGBAJq4sS9dtbCBL6v28DXctysFtusk0ZjwON/Bp3QD+KSrF0yfgsRSVG7hR4Xs\n"
                + "czyQmrN1DYMcsAEHuFU7gyyL1vAgiDPKU5PXpK4cZq5rW1luDip0m6kU/KRiufg8\n"
                + "a9zEecq0mzKCcR5zHSkWTfSzASzrqdDRr0KjlyG9ZnOBLDzhAkEA/8hbLh/dDvpA\n"
                + "pOoUs9AYs6Wvdbb4N8ONqrTOsDDv0UvCDbrJ9JDBuIF5+73jV4Siqero0bV37zSG\n"
                + "LWtSfmE4dQJBAP+ELKPdwX4uTWcPGVuX3TIbluvlSLUq/aQrrPO5qM3jFhkmqBpD\n"
                + "pmddxwncPYhhFlfqwmwgSwWCaix+TbUg/wUCQFUjJWZm6LexiI7b82Qeofo57fsq\n"
                + "mdhF2QO3Bw0SXOC3bLIROGOVQ0XcovOuMtvQpCwWqsQSuQb/3qGDlYPHbHkCQQCT\n"
                + "cPi1Ygv6PMurUXoncT1RYbw3yOmoqPMNnapCRXrTu1sQDk9oQGswMFvfI7haDvPu\n"
                + "rWedLxE7T6Lmo8dBYpXlAkEA+zuoj0DxKs9j32hV7XlnEUMJEy989KQzs56Q0CnP\n"
                + "aXS3ggScjG9Ww/gHmqHAsptac4hyhPyWNdNZB5XoqtYT+Q==\n        "
                + "-----END RSA PRIVATE KEY-----";
        ovfDefinition.getVirtualSystemCollection().getProductSectionAtIndex(0)
                .setPrivateSshKey(privateKey);
        assertEquals(privateKey, ovfDefinition.getVirtualSystemCollection()
                .getProductSectionAtIndex(0).getPrivateSshKey());

        // TODO: add these to the template
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

        int endpointIndexFromAdd = ovfDefinition
                .getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .addEndPointProperties("cpu-probe",
                        "uri://some-end-point/application-monitor", "probe",
                        "cpu", "1sec");

        int endpointIndex = ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getEndPointIndexById("cpu-probe");

        assertEquals(endpointIndexFromAdd, endpointIndex);

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getEndPointId(endpointIndex));

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getEndPointUri(endpointIndex));

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getEndPointType(endpointIndex));

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getEndPointSubtype(endpointIndex));

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getEndPointInterval(endpointIndex));

        assertEquals(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getEndPointNumber(), 2);

        ovfDefinition.getVirtualSystemCollection().getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .removeEndPointProperties(endpointIndex);

        ovfDefinition
                .getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .addEndPointProperties("cpu-probe",
                        "uri://some-end-point/application-monitor", "probe",
                        "cpu", "1sec");

        int softwareDependencyIndexFromAdd = ovfDefinition
                .getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .addSoftwareDependencyProperties("cpu-probe", "zip",
                        "/some-end-point/probe-repository/cpu-probe.zip",
                        "/some-end-point/probe-repository/cpu-probe.sh");

        int softwareDependencyIndex = ovfDefinition
                .getVirtualSystemCollection().getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .getSoftwareDependencyIndexById("cpu-probe");

        assertEquals(softwareDependencyIndexFromAdd, softwareDependencyIndex);

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getSoftwareDependencyId(softwareDependencyIndex));

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getSoftwareDependencyType(softwareDependencyIndex));

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getSoftwareDependencyPackageUri(softwareDependencyIndex));
        
        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getSoftwareDependencyInstallScriptUri(softwareDependencyIndex));

        assertNotNull(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getEndPointInterval(softwareDependencyIndex));

        assertEquals(ovfDefinition.getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1).getProductSectionAtIndex(0)
                .getSoftwareDependencyNumber(), 2);

        ovfDefinition.getVirtualSystemCollection().getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .removeSoftwareDependencyProperties(softwareDependencyIndex);

        ovfDefinition
                .getVirtualSystemCollection()
                .getVirtualSystemAtIndex(1)
                .getProductSectionAtIndex(0)
                .addSoftwareDependencyProperties("cpu-probe", "zip",
                        "/some-end-point/probe-repository/cpu-probe.zip",
                        "/some-end-point/probe-repository/cpu-probe.sh");

        System.out.println(ovfDefinition.toString());

        writeToFile(ovfDefinition.getXmlObject(), "3tier-webapp.ovf");

        OvfDefinition ovfDefinition2 = OvfDefinition.Factory
                .newInstance(ovfDefinition.toString());
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
        productSection
                .setInfo("Product configuration for then entire VirtualSystemCollection.");
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
        virtualHardwareSection
                .setInfo("Description of virtual hardware requirements.");
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
        if (!virtualHardwareSection.setCPUSpeed(2000)) {
            Item itemCpuSpeed = Item.Factory.newInstance();
            itemCpuSpeed.setDescription("CPU Speed");
            itemCpuSpeed.setElementName("2000 MHz CPU speed reservation");
            itemCpuSpeed.setInstanceId("1");
            itemCpuSpeed.setResourceType(ResourceType.PROCESSOR);
            itemCpuSpeed.setResourceSubType("cpuspeed");
            itemCpuSpeed.setAllocationUnits("hertz * 2^20");
            itemCpuSpeed.setReservation(new BigInteger("2000"));
            virtualHardwareSection.addItem(itemCpuSpeed);
        }

        // Memory
        Item itemMemory = Item.Factory.newInstance();
        itemMemory.setDescription("Memory Size");
        itemMemory.setElementName("512 MB of memory");
        itemMemory.setInstanceId("2");
        itemMemory.setResourceType(ResourceType.MEMORY);
        itemMemory.setAllocationUnits("byte * 2^20");
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

        OvfDefinition ovfDefinition2 = OvfDefinition.Factory
                .newInstance(ovfDefinition.toString());
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
