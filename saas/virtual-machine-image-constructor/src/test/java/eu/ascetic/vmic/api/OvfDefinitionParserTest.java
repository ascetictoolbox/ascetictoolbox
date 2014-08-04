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
package eu.ascetic.vmic.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.core.OvfDefinitionParser;

/**
 * Class to test {@link OvfDefinitionParser}
 * 
 * @author Django Armstrong (ULeeds)
 *
 */
public class OvfDefinitionParserTest {
    protected final static Logger LOGGER = Logger
            .getLogger(OvfDefinitionParserTest.class);

    /**
     * Test method for
     * {@link eu.ascetic.vmic.api.core.OvfDefinitionParser#replaceVariablesInScript(String, eu.ascetic.utils.ovf.api.References)}
     * .
     */
    @Test
    public void testReplaceVariablesInScript() {
        URL url = getClass().getClassLoader().getResource("gpf-ovf.xml");
        String ovfDefinitionAsString = null;
        try {
            ovfDefinitionAsString = new String(Files.readAllBytes(Paths.get(url
                    .toURI())));
            LOGGER.info(ovfDefinitionAsString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        OvfDefinition ovfDefinition = OvfDefinition.Factory
                .newInstance(ovfDefinitionAsString);

        OvfDefinitionParser.replaceVariablesInScript(ovfDefinition
                .getVirtualSystemCollection().getVirtualSystemAtIndex(0).getProductSectionAtIndex(0)
                .getPropertyByKey("asceticVMICExecution").getValue(),
                ovfDefinition.getReferences());
    }
}
