/* 
 * Copyright (c) 2012, Fraunhofer-Gesellschaft
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the disclaimer at the end.
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 * 
 * (2) Neither the name of Fraunhofer nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package eu.ascetic.utils.ovf.api.utils;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.util.ArrayList;

/**
 * This class validates an XmlObject and prints errors to System.out
 */
public final class XmlValidator
{
	protected static final Logger LOGGER = Logger
			.getLogger(XmlValidator.class);

    public static boolean validate( XmlObject doc )
    {
        if ( !doc.validate() )
        {
        	LOGGER.debug( getErrors( doc ) );
        }
        return doc.validate();
    }

    /**
     * retrieve the xmlbean objects errors in a String
     *
     * @param doc the xmlbean object to be validated
     * @return the error string
     */
    public static String getErrors( XmlObject doc )
    {
    	// FIXME
        ArrayList<XmlError> validationErrors = new ArrayList();
        XmlOptions voptions = new XmlOptions();
        voptions.setErrorListener( validationErrors );
        boolean valid = doc.validate( voptions );
        String errors = "";
        if ( !valid )
        {
            errors = errors.concat( " Not valid xml." );
            for ( XmlError error : validationErrors )
            {
                errors = errors.concat( System.getProperty( "line.separator" ) );
                errors = errors.concat( error.toString() );
            }
        }
        return errors;
    }
}
