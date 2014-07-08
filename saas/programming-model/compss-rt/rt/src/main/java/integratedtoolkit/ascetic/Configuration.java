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

import integratedtoolkit.types.ResourceDescription;
import java.util.HashMap;

public class Configuration {

    private final static String applicationId;
    private final static String deploymentId;
    private final static String applicationManagerEndpoint;
    private final static String applicationMonitorEndpoint;
    private final static HashMap<String, ResourceDescription> componentDescription;

    static {
        applicationId = "applicationID";
        deploymentId = "deploymentID";
        applicationManagerEndpoint = "http://192.168.1.1:8080";
        applicationMonitorEndpoint = "http://localhost:9000";
        componentDescription = new HashMap<String, ResourceDescription>();
    }

    public static String getApplicationId() {
        return applicationId;
    }

    public static String getDeploymentId() {
        return deploymentId;
    }

    public static String getApplicationMonitorEndpoint() {
        return applicationMonitorEndpoint;
    }

    public static String getApplicationManagerEndpoint() {
        return applicationManagerEndpoint;
    }

    static HashMap<String, ResourceDescription> getComponentDescriptions() {
        return componentDescription;
    }
}
