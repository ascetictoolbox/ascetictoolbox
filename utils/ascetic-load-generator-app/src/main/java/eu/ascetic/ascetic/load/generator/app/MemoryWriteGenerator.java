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

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is aimed at generating load on the memory. Please note the
 * parameter -Xmx1024M should be set, when running.
 *
 * @author Richard
 */
public class MemoryWriteGenerator {

    private static final Runtime RUNTIME = Runtime.getRuntime();

    /**
     * This gets the current runtime allocated memory.
     *
     * @return The amount of memory in use in megabytes
     */
    private static long getMemory() {
        return RUNTIME.totalMemory() / (1024 * 1024);
    }

    /**
     * This generates a memory load.
     *
     * @param args The first arg is the size of the array to write to, the
     * second is the interval timer to wait for, between writes to an array of
     * bytes (in milliseconds). The third is the duration for which the
     * experiment should go on for (in seconds).
     */
    public static void main(String[] args) {
        int size = 500;
        int sleepInterval = -1;
        int duration = 120;

        if (args.length >= 1) {
            size = Integer.valueOf(args[0]);
        }

        if (args.length >= 2) {
            sleepInterval = Integer.valueOf(args[1]);
        }

        if (args.length >= 3) {
            duration = Integer.valueOf(args[2]);
        }

        System.out.println("Inital Size : " + getMemory());
        /**
         * In this test 50Mb of data is written to an array. The default size is
         * 500Mb. This 50Mb of data is randomly generated once, as to avoid
         * over-taxing the CPU. The position it is written to is randomly
         * generated each time.
         */
        int mainBlockSize = 1048576 * size;
        int dataWriteBlockSize = 1048576 * 50;
        int maxGeneratedNumber = mainBlockSize - dataWriteBlockSize;
        byte[] main = new byte[mainBlockSize];
        byte[] dataBlock = new byte[1];
        new Random().nextBytes(dataBlock);
        long endTime = new GregorianCalendar().getTimeInMillis();
        endTime = endTime + TimeUnit.SECONDS.toMillis(duration);
        int writeStartPosition;
        Random rnd = new Random();

        while (new GregorianCalendar().getTimeInMillis() < endTime) {
            writeStartPosition = rnd.nextInt(maxGeneratedNumber);
            Arrays.fill(main, writeStartPosition, writeStartPosition + dataWriteBlockSize, dataBlock[0]);
            try {
                if (sleepInterval > 0) {
                    Thread.sleep(sleepInterval);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MemoryWriteGenerator.class.getName()).log(Level.SEVERE, "", ex);
            }
        }
        System.out.println("Final Size : " + getMemory());
    }

}
