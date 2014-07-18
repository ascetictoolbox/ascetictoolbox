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
package eu.ascetic.utils.ovf.api.utils;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.util.ArrayList;

/**
 * A class to validate an XmlObject and access any errors.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public final class XmlValidator {

    /**
     * Reference to static log4j logger
     */
    protected static final Logger LOGGER = Logger.getLogger(XmlValidator.class);

    /**
     * Validates an XML document in object form.
     * 
     * @param doc
     *            The xmlbean object to be validated
     * @return True if valid
     */
    public static boolean validate(XmlObject doc) {
        if (!doc.validate()) {
            LOGGER.debug(getErrors(doc));
        }
        return doc.validate();
    }

    /**
     * Retrieve the XMLBean objects errors in String form
     * 
     * @param doc
     *            The XMLBean object to be validated
     * @return The error string
     */
    public static String getErrors(XmlObject doc) {
        ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
        XmlOptions voptions = new XmlOptions();
        voptions.setErrorListener(validationErrors);
        boolean valid = doc.validate(voptions);
        String errors = "";
        if (!valid) {
            errors = errors.concat(" Not valid xml.");
            for (XmlError error : validationErrors) {
                errors = errors.concat(System.getProperty("line.separator"));
                errors = errors.concat(error.toString());
            }
        }
        return errors;
    }
}
