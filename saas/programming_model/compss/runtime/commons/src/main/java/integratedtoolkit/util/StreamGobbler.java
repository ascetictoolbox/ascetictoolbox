/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class StreamGobbler extends Thread {

    InputStream is;
    PrintStream out;

    public StreamGobbler(InputStream is, PrintStream out) {
        this.is = is;
        this.out = out;
    }

    public void run() {
        try {
            int nRead;
            byte[] buffer = new byte[4096];
            while ((nRead = is.read(buffer, 0, buffer.length)) != -1) {
                byte[] readData = new byte[nRead];
                System.arraycopy(buffer, 0, readData, 0, nRead);
                out.print(new String(readData));
            }
        } catch (IOException ioe) {
            System.err.println("Exception during reading/writing in output Stream");
            ioe.printStackTrace();
        } finally {
            out.flush();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
