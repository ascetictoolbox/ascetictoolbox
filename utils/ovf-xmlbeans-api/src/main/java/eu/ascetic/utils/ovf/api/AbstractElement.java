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

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * Abstract base class of XML types used in the OVF XML Beans API. Each XML type
 * implementation must define the type of the underlying XMLBean object. This
 * type definition is used for the internal delegation object.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 * @param <T>
 *            Type of the underlying XMLBean object
 */
public abstract class AbstractElement<T extends XmlObject> {

	/**
	 * Internal delegate used as data storage
	 */
	// CHECKSTYLE:OFF
	public T delegate;
	// CHECKSTYLE:ON

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            The base type used for internal delegation and as a data
	 *            storage object
	 */
	public AbstractElement(T base) {
		delegate = base;
	}

	/**
	 * Returns the internal XML representation of the API object.
	 * 
	 * @return Internal representation as a XMLBean
	 */
	@SuppressWarnings("unchecked")
	public T getXmlObject() {
		return (T) delegate.copy();
	}

	/**
	 * Returns the internal XML representation of the API object as a String.
	 * 
	 * @return Internal representation as a String.
	 */
	@Override
	public String toString() {
		XmlOptions options = new XmlOptions();
		options.setSavePrettyPrint();
		options.setSaveOuter();
		return delegate.xmlText(options);
	}

	/**
	 * Returns validation errors found in an XML document. The validation is
	 * done by the XMLBeans validate method.
	 * 
	 * @return A list of errors
	 */
	public List<XmlError> getErrors() {
		List<XmlError> validationErrors = new ArrayList<XmlError>();
		XmlOptions voptions = new XmlOptions();
		voptions.setErrorListener(validationErrors);
		delegate.validate(voptions);
		return validationErrors;
	}

	/**
	 * Returns true if the XML object is not valid.
	 * 
	 * @return True if erroneous
	 */
	public boolean hasErrors() {
		return !delegate.validate();
	}

}
