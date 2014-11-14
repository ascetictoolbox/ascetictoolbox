/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class OVFConverterFactoryTest {

    public OVFConverterFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of getVMs method, of class OVFConverterFactory.
     */
    @Test
    public void testGetVMs() {
        System.out.println("getVMs");
        OvfDefinition deploymentOVF = null;
        Collection<VM> expResult = new ArrayList<>();
        //Start of null case test
        Collection<VM> result = EnergyModeller.getVMs(deploymentOVF);
        assertEquals(expResult, result);
        //End of null case test.
        deploymentOVF = getSampleOVF();
        expResult.add(new VM(1, 512, 0.87890625));
        result = EnergyModeller.getVMs(deploymentOVF);
        assertEquals(expResult, result);
    }

    private OvfDefinition getSampleOVF() {
        OvfDefinition sample = OvfDefinition.Factory.newInstance();

        // File references
        References references = References.Factory.newInstance();
        File file = File.Factory.newInstance("file", "/some/location");
        file.setSize(new BigInteger("1000000000"));
        references.addFile(file);
        sample.setReferences(references);

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
        sample.setDiskSection(diskSection);

        // Network section
        NetworkSection networkSection = NetworkSection.Factory.newInstance();
        networkSection.setInfo("List of the virtual networks used.");
        Network network = Network.Factory.newInstance();
        network.setName("network");
        network.setDescription("A virtual machine network");
        networkSection.addNetwork(network);
        sample.setNetworkSection(networkSection);

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
        ArrayList<ProductSection> productSections = new ArrayList<>();
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
        //itemDisk.findHostRosourceId(itemDisk.getHostResourceAtIndex(0));
        virtualHardwareSection.addItem(itemDisk);

        virtualSystem.setVirtualHardwareSection(virtualHardwareSection);

        ArrayList<VirtualSystem> virtualSystems = new ArrayList<>();
        virtualSystems.add(virtualSystem);
        virtualSystemCollection.setVirtualSystemArray(virtualSystems
                .toArray(new VirtualSystem[virtualSystems.size()]));

        sample.setVirtualSystemCollection(virtualSystemCollection);
        return sample;
    }

}
