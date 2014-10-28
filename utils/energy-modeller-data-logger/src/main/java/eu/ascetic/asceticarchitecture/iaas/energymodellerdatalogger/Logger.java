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
package eu.ascetic.asceticarchitecture.iaas.energymodellerdatalogger;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * This application logs out the raw data that is received by the energy
 * modeller from the Zabbix API.
 */
public class Logger {

    private static boolean running = true;

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("Please provide as the first argument the name of "
                    + "the host or virtual machine to monitor, such as asok09.");
            System.exit(0);
        }
        String hostname = args[0];
        System.out.println("This application will run continually until the word "
                + "'quit' is written.)");
        System.out.println("It is currently logging data out for: " + hostname);
        System.out.println("This is being output to the file: Dataset_" + hostname + ".txt");
        MeasurementLogger logger = new MeasurementLogger(new File("Dataset_" + hostname + ".txt"), false);
        new Thread(logger).start();
        QuitWatcher quitWatcher = new QuitWatcher();
        new Thread(quitWatcher).start();
        ZabbixDirectDbDataSourceAdaptor adaptor = new ZabbixDirectDbDataSourceAdaptor();
        Host host = adaptor.getHostByName(hostname);
        while (running) {
            logger.printToFile(adaptor.getHostData(host));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        logger.stop();
    }

    /**
     * This looks for input from the console so that the application can be
     * told when to quit.
     */
    private static class QuitWatcher implements Runnable {

        @Override
        public void run() {
            while (running) {
                Scanner scanner = new Scanner(System.in);
                String cmd = scanner.hasNext() ? scanner.next() : null;
                if (cmd != null && cmd.equals("quit")) {
                    running = false;
                }
            }
        }

    }
}
