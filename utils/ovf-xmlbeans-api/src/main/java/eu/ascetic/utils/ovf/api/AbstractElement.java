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
 * @param <T>
 *            type of the underlying XMLBean object
 */
public abstract class AbstractElement<T extends XmlObject> {

	/**
	 * internal delegate
	 */
	// CHECKSTYLE:OFF - base type declaration, exception to the general rule by
	// purpose
	public T delegate;

	// CHECKSTYLE:ON

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            the base type is used as internal delegation and data store
	 *            object
	 */
	public AbstractElement(T base) {
		delegate = base;
	}

	/**
	 * Returns the internal XML representation of the API object.
	 * 
	 * @return internal representation as a XMLBean
	 */
	@SuppressWarnings("unchecked")
	public T getXmlObject() {
		return (T) delegate.copy();
	}

	/**
	 * Returns the internal XML representation of the API object as a String.
	 * 
	 * @return internal representation as a String.
	 */
	@Override
	public String toString() {
		XmlOptions options = new XmlOptions();
		options.setSavePrettyPrint();
		options.setSaveOuter();
		return delegate.xmlText(options);
	}

	/**
	 * returns validation errors found in the xml document. The validation is
	 * done by the xmlbeans validate method.
	 * 
	 * @return a list of errors
	 */
	public List<XmlError> getErrors() {
		List<XmlError> validationErrors = new ArrayList<XmlError>();
		XmlOptions voptions = new XmlOptions();
		voptions.setErrorListener(validationErrors);
		delegate.validate(voptions);
		return validationErrors;
	}

	/**
	 * returns true if the xml object is not valid.
	 * 
	 * @return true | false
	 */
	public boolean hasErrors() {
		return !delegate.validate();
	}

}
