/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.ascetic;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppManager {

    private static final String applicationId;
    private static final String deploymentId;

    private static final String endpoint;

    private static final String RESOURCES_PATH;
    private static final HashMap<String, VM> detectedVMs;

    private static int TEST_ID = 1;
    private static int TEST_LIMIT = 3;
    private static LinkedList<String> receivedVMs = new LinkedList();

    static {
        applicationId = Configuration.getApplicationId();
        deploymentId = Configuration.getDeploymentId();
        endpoint = Configuration.getApplicationMonitorEndpoint();
        RESOURCES_PATH = "/applications/" + applicationId + "/deployments/" + deploymentId;
        detectedVMs = new HashMap<String, VM>();
        (new Tester()).start();
    }

    public static Collection<VM> getResources() {
        for (String rvm : receivedVMs) {
            String IPv4 = rvm;
            VM vm = detectedVMs.get(IPv4);
            if (vm == null) {
                String component = "component1";
                String id = UUID.randomUUID().toString();
                vm = new VM(IPv4, id, component);
                detectedVMs.put(IPv4, vm);
            }
        }
        return detectedVMs.values();
    }

    public static int getConsumption(String IPv4, String eventId) {
        return new java.util.Random().nextInt(100);
    }

    static class Tester extends Thread {

        public void run() {
            while (TEST_ID <= TEST_LIMIT) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
                receivedVMs.add("127.0.0." + TEST_ID);
                TEST_ID++;
            }
        }
    }
}
