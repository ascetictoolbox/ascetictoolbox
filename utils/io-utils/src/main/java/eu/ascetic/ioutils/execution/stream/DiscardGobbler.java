/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.utils.execution.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This takes the output from a managed process and discards it.
 *
 * This is based upon the Runtime.exec() tutorial:
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 *
 * It is important to read why gobblers are needed.
 *
 * @see Runtime#exec(java.lang.String)
 *
 * @author Richard Kavanagh
 */
public class DiscardGobbler implements StreamGobbler {

    InputStream is;

    public DiscardGobbler(InputStream is) {
        this.is = is;
    }

    /**
     * This is the main run method of the gobbler.
     */
    @Override
    public void run() {
        try (InputStreamReader isr = new InputStreamReader(is)) {
            BufferedReader br = new BufferedReader(isr);
            while (br.readLine() != null) {
                //no action here just discard the data read
            }
            /*
             * Close the input stream. one call should do as calling the first
             * close stream should propogate, but no harm will occur by explicitly
             * closing all of them.
             */
            is.close();
            br.close();
        } catch (IOException ioe) {
            Logger.getLogger(DiscardGobbler.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }
}
