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

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanDiskSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualDiskDescType;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Provides access to elements within the DiskSection of an OVF document. The
 * DiskSection describes meta-information about virtual disks in the OVF
 * package. Virtual disks and their metadata are described outside the virtual
 * hardware to facilitate sharing between virtual machines within an OVF
 * document.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class DiskSection extends AbstractElement<XmlBeanDiskSectionType> {

    /**
     * A static reference to the {@link DiskSectionFactory} class for generating
     * new instances of this object.
     */
    public static DiskSectionFactory Factory = new DiskSectionFactory();

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public DiskSection(XmlBeanDiskSectionType base) {
        super(base);
    }

    /**
     * Gets an array of all the Disk held in this object.
     * 
     * @return An array of Disk
     */
    public Disk[] getDiskArray() {

        List<Disk> diskArray = new ArrayList<Disk>();
        for (XmlBeanVirtualDiskDescType diskSectionType : delegate
                .getDiskArray()) {
            diskArray.add(new Disk(diskSectionType));
        }
        return diskArray.toArray(new Disk[diskArray.size()]);
    }

    /**
     * Sets the Disk array held in this object.
     * 
     * @param virtualDiskDescArray
     *            The array of Disk to set.
     */
    public void setDiskArray(Disk[] virtualDiskDescArray) {
        Vector<XmlBeanVirtualDiskDescType> diskArray = new Vector<XmlBeanVirtualDiskDescType>();
        for (int i = 0; i < virtualDiskDescArray.length; i++) {
            diskArray.add(virtualDiskDescArray[i].getXmlObject());
        }
        delegate.setDiskArray((XmlBeanVirtualDiskDescType[]) diskArray
                .toArray());
    }

    /**
     * Gets a Disk at held within the Disk array at index i
     * 
     * @param i
     *            The index value
     * @return The Disk at index i
     */
    public Disk getDiskAtIndex(int i) {
        return new Disk(delegate.getDiskArray(i));
    }

    /**
     * Adds a Disk to the end of the Disk array
     * 
     * @param disk
     *            The Disk to add to the end of the array.
     */
    public void addDisk(Disk disk) {
        XmlBeanVirtualDiskDescType xmlBeanVirtualDiskDescType = delegate
                .addNewDisk();
        xmlBeanVirtualDiskDescType.set(disk.getXmlObject());
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
}
