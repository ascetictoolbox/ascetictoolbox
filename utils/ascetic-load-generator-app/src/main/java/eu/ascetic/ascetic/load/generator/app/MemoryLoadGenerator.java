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
package eu.ascetic.ascetic.load.generator.app;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is aimed at generating load on the memory. Please note the parameter
 * -Xmx1024M should be set, when running.
 * @author Richard
 */
public class MemoryLoadGenerator {

    private static final ArrayList<byte[]> DATA = new ArrayList<>();
    private static final Runtime RUNTIME = Runtime.getRuntime();

    /**
     * This gets the current runtime allocated memory.
     * @return The amount of memory in use in megabytes
     */
    private static long getMemory() {
        return RUNTIME.totalMemory() / (1024 * 1024);
    }

    /**
     * This generates a memory load.
     * @param args 
     */
    public static void main(String[] args) {
        int maxSize = 800;
        int sleepInterval = 50;
        
        
        if (args.length >= 1) {
            maxSize = Integer.valueOf(args[0]);
        }

        if (args.length >= 2) {
            sleepInterval = Integer.valueOf(args[1]);
        }        
        
        System.out.println("Inital Size : " + getMemory());
        //Add to an array list 1Mb of memory allocation
        byte[] b = new byte[1048576];
        new Random().nextBytes(b);
        while (getMemory() < maxSize) {
            DATA.add(b.clone()); //1 MB
            try {
                Thread.sleep(sleepInterval);
            } catch (InterruptedException ex) {
                Logger.getLogger(MemoryLoadGenerator.class.getName()).log(Level.SEVERE, "", ex);
            }
        }
        System.out.println("Final Size : " + getMemory());
    }

}
