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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanFileType;

import eu.ascetic.utils.ovf.api.AbstractElement;

/**
 * Provides access to the File elements of an OVF document. The file references
 * contained by the {@link References} element allows a tool to easily determine
 * the integrity of an OVF package without having to parse or interpret the
 * entire structure of the descriptor.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class File extends AbstractElement<XmlBeanFileType> {

    /**
     * A static reference to the {@link FileFactory} class for generating new
     * instances of this object.
     */
    public static FileFactory Factory = new FileFactory();

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public File(XmlBeanFileType base) {
        super(base);
    }

    /**
     * Gets the unique ID of this File element.
     * 
     * @return The File element ID
     */
    public String getId() {
        return delegate.getId();
    }

    /**
     * Sets the unique ID of this File element.
     * 
     * @param id
     *            The File element ID to set
     */
    public void setId(String id) {
        delegate.setId(id);
    }

    /**
     * Gets the URL of the File.
     * 
     * @return The URL
     */
    public String getHref() {
        return delegate.getHref();
    }

    /**
     * Sets the URL of the File. Relative-path references and the URL schemes
     * "file", "http" and "https" are supported (see RFC1738 and RFC3986)
     * 
     * @param href
     *            The URL to set
     */
    public void setHref(String href) {
        delegate.setHref(href);
    }

    /**
     * Gets the size of the File (or compressed File) in bytes
     * 
     * @return The size of the File in bytes
     */
    public BigInteger getSize() {
        return delegate.getSize();
    }

    /**
     * Sets the size of the File (or compressed File) in bytes
     * 
     * @param size
     *            The size of the File in bytes to set
     */
    public void setSize(BigInteger size) {
        delegate.setSize(size);
    }

    /**
     * Provides details of compression format used. When a File element is
     * compressed using gzip the value will be set to "gzip".
     * 
     * TODO: return enum here for compression param?
     * 
     * @return The compression format
     */
    public String getCompression() {
        return delegate.getCompression();
    }

    /**
     * Sets the compression format used. When a File element is compressed using
     * gzip the value should be set to "gzip".
     * 
     * TODO: use enum here for compression param?
     * 
     * @param compression
     *            The compression format to set
     */
    public void setCompression(String compression) {
        delegate.setCompression(compression);
    }

    /**
     * Sets the chunk size. Files referenced within the OVF maybe split into
     * chunks to accommodate file size restrictions on certain file systems. The
     * value of the chunk size is the size of each chunk, except the last chunk,
     * which may be smaller.
     * 
     * @return The chunk size in bytes
     */
    public long getChunkSize() {
        return delegate.getChunkSize();
    }

    /**
     * Gets the chunk size. Files referenced within the OVF maybe split into
     * chunks to accommodate file size restrictions on certain file systems. The
     * value of the chunk size is the size of each chunk, except the last chunk,
     * which may be smaller.
     * 
     * @param chunkSize
     *            The chunk size in bytes to set
     */
    public void setChunkSize(long chunkSize) {
        delegate.setChunkSize(chunkSize);
    }

}
