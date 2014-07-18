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
package eu.ascetic.utils.ovf.api;

import java.math.BigInteger;
import java.util.List;
import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanRASDType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualHardwareSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;
import eu.ascetic.utils.ovf.api.enums.*;

/**
 * Provides access to the VirtualHardwareSection. Each {@link VirtualSystem}
 * element may contain one or more VirtualHardwareSection elements and describes
 * the virtual hardware required by the virtual system. The virtual hardware
 * required by a virtual machine is specified in VirtualHardwareSection
 * elements. This specification supports abstract or incomplete hardware
 * descriptions in which only the major devices are described. The hypervisor is
 * allowed to create additional virtual hardware controllers and devices, as
 * long as the required devices listed in the descriptor are realized.<br>
 * <br>
 * A typical VirtualHardwareSection contains a {@link System} child element to
 * describe the virtual hardware family (a.k.a virtual system type) and a
 * sequence of {@link Item} elements to describe the virtual hardware
 * characteristics.<br>
 * <br>
 * TODO: Refactor ASCETiC specific helper methods to another package/class so as
 * to not pollute the API.<br>
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class VirtualHardwareSection extends
        AbstractElement<XmlBeanVirtualHardwareSectionType> {

    /**
     * A static reference to the {@link VirtualHardwareSectionFactory} class for
     * generating new instances of this object.
     */
    public static VirtualHardwareSectionFactory Factory = new VirtualHardwareSectionFactory();

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public VirtualHardwareSection(XmlBeanVirtualHardwareSectionType base) {
        super(base);
    }

    /**
     * Gets the info element, a human readable description of the meaning of
     * this section.
     * 
     * @return The content of the info element
     */
    public String getInfo() {
        return delegate.getInfo().getStringValue();
    }

    /**
     * Sets the info element, a human readable description of the meaning of
     * this section.
     * 
     * @param info
     *            The content to set within the info element
     */
    public void setInfo(String info) {
        delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
    }

    /**
     * Gets the {@link System} element used to describe the virtual system type
     * (a.k.a virtual hardware family). See
     * {@link VirtualHardwareSection#getVirtualHardwareFamily()} for a helper
     * method that simplifies getting the virtual system type.
     * 
     * @return The System element
     */
    public System getSystem() {
        return new System(delegate.getSystem());
    }

    /**
     * Sets the {@link System} element used to describe the virtual system type
     * (a.k.a virtual hardware family). See
     * {@link VirtualHardwareSection#setVirtualHardwareFamily(String)} for a
     * helper method that simplifies setting the virtual system type.
     * 
     * @param system
     *            The System element to set
     */
    public void getSystem(System system) {
        delegate.setSystem(system.getXmlObject());
    }

    /**
     * Gets the {@link Item} array. Item elements describe virtual hardware
     * characteristics.
     * 
     * @return The Item array
     */
    public Item[] getItemArray() {
        List<Item> vector = new Vector<Item>();
        for (XmlBeanRASDType type : delegate.getItemArray()) {
            vector.add(new Item(type));
        }
        return vector.toArray(new Item[vector.size()]);
    }

    /**
     * Sets the {@link Item} array. Item elements describe virtual hardware
     * characteristics.
     * 
     * @param itemArray
     *            The Item array to set
     */
    public void setItemArray(Item[] itemArray) {
        List<XmlBeanRASDType> newItemArray = new Vector<XmlBeanRASDType>();
        for (int i = 0; i < itemArray.length; i++) {
            newItemArray.add(itemArray[i].getXmlObject());
        }
        delegate.setItemArray((XmlBeanRASDType[]) newItemArray.toArray());
    }

    /**
     * Gets the {@link Item} at index i from the Item array. Item elements
     * describe virtual hardware characteristics.
     * 
     * @param i
     *            The index of the item
     * @return The Item
     */
    public Item getItemAtIndex(int i) {
        return new Item(delegate.getItemArray(i));
    }

    /**
     * Add an {@link Item} to the end of the Item array. Item elements describe
     * virtual hardware characteristics.
     * 
     * @param item
     *            The item to add
     */
    public void addItem(Item item) {
        XmlBeanRASDType xmlBeanRASDType = delegate.addNewItem();
        xmlBeanRASDType.set(item.getXmlObject());
    }

    // Start of ASCETiC specific helper functions

    /**
     * Gets the virtual hardware family from the {@link System} element.
     * 
     * @return The virtual hardware family (a.k.a virtual system type)
     */
    public String getVirtualHardwareFamily() {
        return getSystem().getVirtualSystemType();
    }

    /**
     * Sets the virtual hardware family from the {@link System} element.
     * 
     * @param virtualHardwareFamily
     *            The virtual hardware family (a.k.a virtual system type) to set
     */
    public void setVirtualHardwareFamily(String virtualHardwareFamily) {
        getSystem().setVirtualSystemType(virtualHardwareFamily);
    }

    /**
     * Gets the number of virtual CPUs. Returns zero if no {@link Item} no CPU
     * number element can be found.
     * 
     * @return The number of virtual CPUs
     */
    public int getNumberOfVirtualCPUs() {
        Item[] itemArray = getItemArray();
        for (int i = 0; i < itemArray.length; i++) {
            if (itemArray[i].getResourceType().equals(ResourceType.PROCESSOR)
                    && itemArray[i].isSetResourceSubType() == false) {
                return itemArray[i].getVirtualQuantity().intValue();
            }
        }
        return 0;
    }

    /**
     * Sets the number of virtual CPUs. Returns false if no {@link Item} no CPU
     * number element can be found.
     * 
     * @param numberOfVirtualCPUs
     *            The number of virtual CPUs to set
     * @return Indicates if setting was successful.
     */
    public boolean setNumberOfVirtualCPUs(int numberOfVirtualCPUs) {
        Item[] itemArray = getItemArray();
        for (int i = 0; i < itemArray.length; i++) {
            if (itemArray[i].getResourceType().equals(ResourceType.PROCESSOR)
                    && itemArray[i].isSetResourceSubType() == false) {
                itemArray[i].setVirtualQuantity(new BigInteger(new Integer(
                        numberOfVirtualCPUs).toString()));
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the memory size (see {@link Item#getAllocationUnits()}). Returns
     * zero if no {@link Item} element for memory can be found.
     * 
     * @return The number of virtual CPUs
     */
    public int getMemorySize() {
        Item[] itemArray = getItemArray();
        for (int i = 0; i < itemArray.length; i++) {
            if (itemArray[i].getResourceType().equals(ResourceType.MEMORY)) {
                return itemArray[i].getVirtualQuantity().intValue();
            }
        }
        return 0;
    }

    /**
     * Sets the memory size (see {@link Item#getAllocationUnits()}). Returns
     * false if no {@link Item} element for memory can be found.
     * 
     * @param memorySize
     *            The memory size to set
     * @return Indicates if setting was successful.
     */
    public boolean setMemorySize(int memorySize) {
        if (memorySize < 0) {
            throw new IllegalArgumentException("Memory size must be > -1");
        }

        Item[] itemArray = getItemArray();
        for (int i = 0; i < itemArray.length; i++) {
            if (itemArray[i].getResourceType().equals(ResourceType.MEMORY)) {
                itemArray[i].setVirtualQuantity(new BigInteger(new Integer(
                        memorySize).toString()));
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the CPU speed (see {@link Item#getAllocationUnits()}). Returns zero
     * if no {@link Item} element for CPU speed can be found with a resource sub
     * type of "cpuspeed".
     * 
     * @return The CPU speed in MHz
     */
    public int getCPUSpeed() {
        Item[] itemArray = getItemArray();
        for (int i = 0; i < itemArray.length; i++) {
            if (itemArray[i].getResourceType().equals(ResourceType.PROCESSOR)
                    && itemArray[i].getResourceSubType().equals("cpuspeed")) {
                return itemArray[i].getReservation().intValue();
            }
        }
        return 0;
    }

    /**
     * Sets the CPU speed (see {@link Item#getAllocationUnits()}). Returns false
     * if no {@link Item} element for CPU speed can be found with a resource sub
     * type of "cpuspeed".
     * 
     * @param cpuSpeed
     *            The CPU speed to set
     * @return Indicates if setting was successful
     */
    public boolean setCPUSpeed(int cpuSpeed) {
        if (!(cpuSpeed > -1)) {
            throw new IllegalArgumentException("CPU speed must be > -1");
        }

        Item[] itemArray = getItemArray();
        for (int i = 0; i < itemArray.length; i++) {
            if (itemArray[i].getResourceType().equals(ResourceType.PROCESSOR)
                    && itemArray[i].getResourceSubType().equals("cpuspeed")) {
                itemArray[i].setReservation(new BigInteger(
                        new Integer(cpuSpeed).toString()));
                return true;
            }
        }
        return false;
    }

}
