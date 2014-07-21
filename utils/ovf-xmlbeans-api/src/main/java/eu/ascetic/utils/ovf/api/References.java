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

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanFileType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanReferencesType;

import eu.ascetic.utils.ovf.api.AbstractElement;

/**
 * Provides access to the References element of an OVF document. The References
 * element stores references to files (see {@link File}) and allows a tool to
 * easily determine the integrity of an OVF package without having to parse or
 * interpret the entire structure of the descriptor.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class References extends AbstractElement<XmlBeanReferencesType> {

    /**
     * A static reference to the {@link ReferencesFactory} class for generating
     * new instances of this object.
     */
    public static ReferencesFactory Factory = new ReferencesFactory();

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public References(XmlBeanReferencesType base) {
        super(base);
    }

    /**
     * Gets the {@link File} references array held in this object.
     * 
     * @return The file references array
     */
    public File[] getFileArray() {
        Vector<File> vector = new Vector<File>();
        for (XmlBeanFileType type : delegate.getFileArray()) {
            vector.add(new File(type));
        }
        return vector.toArray(new File[vector.size()]);
    }

    /**
     * Sets the {@link File} references array held in this object.
     * 
     * @param fileArray
     *            The file references array to set
     */
    public void setFileArray(File[] fileArray) {
        Vector<XmlBeanFileType> vector = new Vector<XmlBeanFileType>();

        for (int i = 0; i < fileArray.length; i++) {
            vector.add(fileArray[i].getXmlObject());
        }

        delegate.setFileArray(vector.toArray(new XmlBeanFileType[vector.size()]));
    }

    /**
     * Gets the {@link File} reference at index i.
     * 
     * @param i
     *            The index of the file reference
     * @return The file reference
     */
    public File getFileAtIndex(int i) {
        return new File(delegate.getFileArray(i));
    }

    /**
     * Adds a new {@link File} references to the end of the array held in this
     * object.
     * 
     * @param file
     *            The file reference to add
     */
    public void addFile(File file) {
        XmlBeanFileType newfile = delegate.addNewFile();
        newfile.set(file.getXmlObject());
    }
}
