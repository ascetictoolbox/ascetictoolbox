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
package eu.ascetic.ovf.api;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.xmlbeans.XmlOptions;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeDocument;

import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * @author Django Armstrong (ULeeds)
 *
 */
public class OvfDefinitionTest extends TestCase {
	
	public void testOvfDefinition() {
		OvfDefinition ovfDefinition = OvfDefinition.Factory.newInstance("a-service-id", "a-component-id");
		
		// TODO ...
		ovfDefinition.getVirtualSystemCollectionProductSectionArray()[0].setVersion("Supa-dupa");
	}
	
    protected void writeToFile( XmlBeanEnvelopeDocument ovfDefinition, String fileName )
    {
        try
        {
            // If system property is not set (i.e. test case was started from IDE )
            // we use the current directory to store the file
            String targetDir = System.getProperty( "manifestSampleDir", "." );

            File file = new File( targetDir + File.separator + File.separator + fileName + ".xml" );
            FileWriter fstream = new FileWriter( file );
            BufferedWriter out = new BufferedWriter( fstream );
            out.write( ovfDefinition.xmlText( new XmlOptions().setSavePrettyPrint() ) );
            System.out.println( fileName + " was written to " + file.getAbsolutePath() );
            // Close the output stream
            out.close();
        }
        catch ( Exception e )
        {
            // Catch exception if any
            System.err.println( "Error: " + e.getMessage() );
            fail( e.getMessage() );
        }
    }
}
