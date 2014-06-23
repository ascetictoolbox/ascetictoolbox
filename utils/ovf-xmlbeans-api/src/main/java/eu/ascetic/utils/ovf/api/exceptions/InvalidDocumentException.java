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
package eu.ascetic.utils.ovf.api.exceptions;

import org.apache.xmlbeans.XmlObject;

import eu.ascetic.utils.ovf.api.utils.XmlValidator;

/**
 * @author Django Armstrong (ULeeds)
 * 
 */
public class InvalidDocumentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7045796285018934681L;

	public InvalidDocumentException(String message, XmlObject document) {
		super(message.concat(XmlValidator.getErrors(document)));
	}

}
