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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.ascetic.vmic.api.core.SystemCallException;
import eu.ascetic.vmic.api.core.SystemCallRemote;
import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;

/**
 * Class to test {@link SystemCallRemote}
 * 
 * @author Django Armstrong (ULeeds)
 *
 */
public class SystemCallRemoteTest {
    protected final static Logger LOGGER = Logger
            .getLogger(SystemCallRemoteTest.class);

    /**
     * Test method for
     * {@link eu.ascetic.vmic.api.core.SystemCallRemote#runCommand(java.lang.String, java.util.List)}
     * .
     */
    @Test
    public void testRunCommand() {
        GlobalConfiguration globalConfiguration = new GlobalConfiguration();
        SystemCallRemote systemCallRemote = new SystemCallRemote(
                "C:\\Users\\django\\cygwin\\home\\django\\",
                globalConfiguration);

        String commandName = "ls";
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("-alh");
        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for
     * {@link eu.ascetic.vmic.api.core.SystemCallRemote#scriptToSingleLineCommand(java.lang.String)}
     * .
     */
    @Test
    public void testScriptToSingleLineCommand() {
        File file = new File(
                "C:\\Users\\django\\cygwin\\etc\\rc.d\\init.d\\inetd");
        try {
            LOGGER.info("### Multi-line script file used as input to test: ");
            LOGGER.info(new String(Files.readAllBytes(Paths.get(file
                    .getAbsolutePath())), Charset.defaultCharset()));
            LOGGER.info("### Script as single line:");
            LOGGER.info(SystemCallRemote.scriptToSingleLineCommand(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
