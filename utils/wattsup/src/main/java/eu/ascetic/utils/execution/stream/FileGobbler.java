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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This takes the output from a managed process and places it on disk.
 *
 * This is based upon the Runtime.exec() tutorial:
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 *
 * This is based upon: Listing 4.7 GoodWinRedirect.java
 *
 * It is important to read why gobblers are needed.
 * @see Runtime#exec(java.lang.String)
 * 
 * @author Richard Kavanagh
 */
public class FileGobbler implements StreamGobbler {

    private InputStream is;
    private OutputStream os;
    private boolean toScreen = false;

    FileGobbler(InputStream is, String file, boolean toScreen) {
        this.is = is;
        try {
            this.os = new FileOutputStream(new File(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileGobbler.class.getName()).log(Level.SEVERE, "The file " + file + " was not found", ex);
        }
        this.toScreen = toScreen;
    }

    /**
     * This is the main run method of the gobbler.
     */    
    @Override
    public void run() {
        try {
            PrintWriter pw = null;
            if (os != null) {
                pw = new PrintWriter(os);
            }

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (pw != null) {
                    pw.println(line);
                }
                if (toScreen == true) {
                    System.out.println(line);
                }
            }
            if (pw != null) {
                pw.flush();
            }
        } catch (IOException ioe) {
            Logger.getLogger(FileGobbler.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }
}
