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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a test application it is mainly aimed at generating input and output
 * of a set size for data transfer.
 *
 * @author Richard Kavanagh
 */
public class Main {

    /**
     * This takes three arguments the first is the standard outs size in
     * characters and the second is standard errors size in characters. The
     * third is how long the application should run for.
     *
     * @param args the command line arguments. The first is standard outs size
     * the second is standard errors size. The third is how long the app should
     * run for in addition to the time taken to generate the strings. The uses
     * is expected to perform standard out and standard error redirection which
     * shall generate the test files as needed.
     */
    public static void main(String[] args) {

        WorkerThread processorUsingThread = new WorkerThread();

        if (args.length == 0) {
            System.out.println("This is standard out");
            System.err.println("This is standard error");
            return;
        }
        if (args.length >= 1) {
            //arg given in characters
            int stdOutSize = Integer.valueOf(args[0]);
            String result = "";
            for (int i = 0; i < stdOutSize; i++) {
                result += "S";
            }
            System.out.println(result);
        }
        if (args.length >= 2) {
            //arg given in characters
            int stdErrorSize = Integer.valueOf(args[1]);
            String result = "";
            for (int i = 0; i < stdErrorSize; i++) {
                result += "E";
            }
            System.err.println(result);
        }
        if (args.length >= 3) {
            //arg given in seconds
            int computeTime = Integer.valueOf(args[2]);
            try {
                Thread myThread = new Thread(processorUsingThread);
                myThread.setDaemon(true);
                myThread.start();
                Thread.sleep(computeTime * 1000);
                processorUsingThread.stop();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "The test app exited early", ex);
            }
        }

    }

    /**
     * This thread aims to keep the CPU on a busy wait. It ensures the load
     * profile is more realistic for the purpose of the experimentation. i.e.
     * Ganglia should be able to detect the CPU load on the machines because of
     * the running of an application.
     */
    private static class WorkerThread implements Runnable {

        boolean running = true;

        @Override
        public void run() {
            String test = "test";
            long number = 5;
            while (running) {
                if (test.length() > 10000) {
                    test = "test";
                    number = 5;
                } else {
                    test = test + test;
                    number = number + number * number;
                }
            }
        }

        /**
         * This stops the thread from running and consuming cpu time.
         */
        public void stop() {
            running = false;
        }
    }
}
