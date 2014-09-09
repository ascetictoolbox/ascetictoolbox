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
package integratedtoolkit.util;

import integratedtoolkit.types.CloudProvider;
import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.Resource;
import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.types.ResourceCreationRequest;
import integratedtoolkit.types.ResourceDestructionRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * The CloudManager class is an utility to manage all the cloud interactions and
 * hide the details of each provider.
 */
public class CloudManager {

    private static boolean useCloud;

    /**
     * Relation between a Cloud provider name and its representation
     */
    private static HashMap<String, CloudProvider> providers;
    /**
     * Relation between a resource name and the representation of the Cloud
     * provider that support it
     */
    private static HashMap<String, CloudProvider> VM2Provider;
    //Logs
    private static final Logger logger = Logger.getLogger(Loggers.RESOURCES);

    private static final LinkedList<ResourceCreationRequest> pendingRequests = new LinkedList<ResourceCreationRequest>();

    /**
     * Initializes the internal data structures
     *
     */
    public static void initialize() {
        useCloud = false;
        providers = new HashMap<String, CloudProvider>();
        VM2Provider = new HashMap<String, CloudProvider>();
    }

    /**
     * Configures the runtime to use the Cloud to adapt the resource pool
     *
     * @param useCloud true if enabled
     */
    public static void setUseCloud(boolean useCloud) {
        CloudManager.useCloud = useCloud;
    }

    /**
     * Check if Cloud is used to dynamically adapt the resource pool
     *
     * @return true if it is used
     */
    public static boolean isUseCloud() {
        return useCloud;
    }

    /**
     * Adds a new Provider to the management
     *
     * @param name Identifier of that cloud provider
     * @param connectorPath Package and class name of the connector required to
     * interact with the provider
     * @param limitOfVMs Max amount of VMs that can be running at the same time
     * for that Cloud provider
     * @param connectorProperties Properties to configure the connector
     * @throws Exception Loading the connector by reflection
     */
    public static void newCloudProvider(String name, Integer limitOfVMs, String connectorPath, HashMap<String, String> connectorProperties)
            throws Exception {
        CloudProvider cp = new CloudProvider(connectorPath, limitOfVMs, connectorProperties, name);
        providers.put(name, cp);
    }

    /**
     * Adds an image description to a Cloud Provider
     *
     * @param providerName Identifier of the Cloud provider
     * @param cid Description of the features offered by that image
     * @throws Exception the cloud provider does not exist
     */
    public static void addImageToProvider(String providerName, CloudImageDescription cid)
            throws Exception {
        CloudProvider cp = providers.get(providerName);
        if (cp == null) {
            throw new Exception("Inexistent Cloud Provider " + providerName);
        }
        cp.addCloudImage(cid);
    }

    /**
     * Adds an instance type description to a Cloud Provider
     *
     * @param providerName Identifier of the Cloud provider
     * @param rd Description of the features offered by that instance type
     * @throws Exception the cloud provider does not exist
     */
    public static void addInstanceTypeToProvider(String providerName, ResourceDescription rd)
            throws Exception {
        CloudProvider cp = providers.get(providerName);
        if (cp == null) {
            throw new Exception("Inexistent Cloud Provider " + providerName);
        }
        cp.addInstanceType(rd);
    }

    public static LinkedList<ResourceCreationRequest> getPendingRequests() {
        return pendingRequests;
    }

    /**
     * Asks for the described resources to a Cloud provider. The CloudManager
     * checks the best resource that each provider can offer. Then it picks one
     * of them and it constructs a resourceRequest describing the resource and
     * which cores can be executed on it. This ResourceRequest will be used to
     * ask for that resource creation to the Cloud Provider and returned if the
     * application is accepted.
     *
     * @param constraints description of the resource expected to receive
     * @param contained {@literal true} if we want the request to ask for a
     * resource contained in the description; else, the result contains the
     * passed in description.
     * @return Description of the ResourceRequest sent to the CloudProvider.
     * {@literal Null} if any of the Cloud Providers can offer a resource like
     * the requested one.
     */
    public static ResourceCreationRequest askForResources(ResourceDescription constraints, boolean contained) {
        return askForResources(1, constraints, contained);
    }

    /**
     * The CloudManager ask for resources that can execute certain amount of
     * cores at the same time. It checks the best resource that each provider
     * can offer to execute that amount of cores and picks one of them. It
     * constructs a resourceRequest describing the resource and which cores can
     * be executed on it. This ResourceRequest will be used to ask for that
     * resource creation to the Cloud Provider and returned if the application
     * is accepted.
     *
     * @param amount amount of slots
     * @param constraints festures of the resource
     * @param contained {@literal true} if we want the request to ask for a
     * resource contained in the description; else, the result contains the
     * passed in description.
     * @return
     */
    public static ResourceCreationRequest askForResources(Integer amount, ResourceDescription constraints, boolean contained) {
        CloudProvider bestProvider = null;
        ResourceDescription bestConstraints = null;
        Float bestValue = Float.MAX_VALUE;

        for (CloudProvider cp : providers.values()) {
            ResourceDescription rc = cp.getBestIncrease(amount, constraints, contained);
            if (rc != null && rc.getValue() < bestValue) {
                bestProvider = cp;
                bestConstraints = rc;
                bestValue = rc.getValue();
            }
        }
        if (bestConstraints == null) {
            return null;
        }

        int[] simultaneousCounts = bestProvider.getSimultaneousCounts(bestConstraints.getType());
        if (simultaneousCounts == null) {
            for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
                ResourceDescription[] descriptions = CoreManager.getResourceConstraints(coreId);
                for (ResourceDescription description : descriptions) {
                    if (description != null) {
                        Integer into = bestConstraints.into(description);
                        if (into != null) {
                            simultaneousCounts[coreId] = Math.max(simultaneousCounts[coreId], into);
                        }
                    }
                }
            }
        }

        ResourceCreationRequest rcr = new ResourceCreationRequest(bestConstraints, simultaneousCounts, bestProvider.getName());

        try {
            if (bestProvider.turnON(rcr)) {
                pendingRequests.add(rcr);
                return rcr;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Given a set of resources, it checks every possible modification of the
     * resource and returns the one that better fits with the destuction
     * recommendations.
     *
     * The decision-making algorithm tries to minimize the number of affected CE
     * that weren't recommended to be modified, minimize the number of slots
     * that weren't requested to be destroyed and maximize the number of slots
     * that can be removed and they were requested for.
     *
     * @param resourceSet set of resources
     * @param destroyRecommendations number of slots to be removed for each CE
     * @return an object array defining the best solution. 0-> (Resource)
     * selected Resource. 1-> (int[]) record of the #CE with removed slots and
     * that they shouldn't be modified, #slots that will be destroyed aand they
     * weren't recomended, #slots that will be removed and they were asked to
     * be. 2->(int[]) #slots to be removed by each CE. 3->(ResourceDescription)
     * description of the resource to be destroyed.
     *
     *
     */
    public static Object[] getBestDestruction(Set<Resource> resourceSet, float[] destroyRecommendations) {
        CloudProvider cp;
        float[] bestRecord = new float[3];
        bestRecord[0] = Float.MAX_VALUE;
        bestRecord[1] = Float.MAX_VALUE;
        bestRecord[2] = Float.MIN_VALUE;
        Resource bestResource = null;
        CloudProvider bestCP = null;
        String bestType = null;
        ResourceDescription bestRD = null;

        for (Resource res : resourceSet) {
            cp = VM2Provider.get(res.getName());
            if (cp == null) { // it's not a cloud machine
                continue;
            }
            HashMap<String, Object[]> typeToPoints = cp.getPossibleReductions(res, destroyRecommendations);

            for (java.util.Map.Entry<String, Object[]> destruction : typeToPoints.entrySet()) {
                String typeName = destruction.getKey();
                Object[] description = destruction.getValue();
                float[] values = (float[]) description[0];
                ResourceDescription rd = (ResourceDescription) description[1];
                if (bestRecord[0] == values[0]) {
                    if (bestRecord[1] == values[1]) {
                        if (bestRecord[2] < values[2]) {
                            bestRecord = values;
                            bestResource = res;
                            bestType = typeName;
                            bestCP = cp;
                            bestRD = rd;
                        }
                    } else {
                        if (bestRecord[1] > values[1]) {
                            bestRecord = values;
                            bestResource = res;
                            bestType = typeName;
                            bestCP = cp;
                            bestRD = rd;
                        }
                    }

                } else {
                    if (bestRecord[0] > values[0]) {
                        bestRecord = values;
                        bestResource = res;
                        bestType = typeName;
                        bestCP = cp;
                        bestRD = rd;
                    }
                }
            }
        }
        if (bestResource != null) {
            Object[] ret = new Object[4];
            ret[0] = bestResource;
            ret[1] = bestRecord;
            ret[2] = bestCP.getSimultaneousCounts(bestType);
            ret[3] = bestRD;
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Cloudmanager terminates the described resources (only part of it)
     *
     * @param rdr Description of the resource
     */
    public static void terminate(ResourceDestructionRequest rdr) {
        CloudProvider cp = VM2Provider.get(rdr.getRequested().getName());
        if (cp != null) {
            cp.terminate(rdr);
        }
    }

    /**
     * Notifies to the Cloud Manager the desruction of the resource associated
     * to a resource destruction request
     *
     * @param request description of the resource destruction request
     */
    public static void notifyShutdown(ResourceDestructionRequest request) {
        CloudProvider cp = VM2Provider.get(request.getRequested().getName());
        if (cp != null) {
            cp.terminatedInstance();
        }
        if (request.isTerminate()) {
            VM2Provider.remove(request.getRequested().getName());
        }
    }

    /**
     * CloudManager terminates all the resources obtained from any provider
     *
     * @throws ConnectorException
     */
    public static void terminateALL() throws ConnectorException {
        for (java.util.Map.Entry<String, CloudProvider> vm : providers.entrySet()) {
            CloudProvider cp = vm.getValue();
            cp.terminateAll();
        }
        VM2Provider.clear();
    }

    /**
     * Computes the cost per hour of the whole cloud resource pool
     *
     * @return the cost per hour of the whole pool
     */
    public static float currentCostPerHour() {
        float total = 0;
        for (CloudProvider cp : providers.values()) {
            total += cp.getCurrentCostPerHour();
        }
        return total;
    }

    /**
     * The CloudManager notifies to all the connectors the end of generation of
     * new tasks
     */
    public static void stopReached() {
        for (CloudProvider cp : providers.values()) {
            cp.stopReached();
        }
    }

    /**
     * The CloudManager computes the accumulated cost of the execution
     *
     * @return cost of the whole execution
     */
    public static float getTotalCost() {
        float total = 0;
        for (CloudProvider cp : providers.values()) {
            total += cp.getTotalCost();
        }
        return total;
    }

    /**
     * Returns how long will take a resource ro be ready since the CloudManager
     * asks for it.
     *
     * @return time required for a resource to be ready
     * @throws Exception Can not get the creation time for some providers.
     */
    public static long getNextCreationTime() throws Exception {
        long total = 0;
        for (CloudProvider cp : providers.values()) {
            total = Math.max(total, cp.getNextCreationTime());
        }
        return total;
    }

    /**
     * Gets the currently running machines on the cloud
     *
     * @return amount of machines on the Cloud
     */
    public static int getCurrentVMCount() {
        int total = 0;
        for (CloudProvider cp : providers.values()) {
            total += cp.getCurrentVMCount();
        }
        return total;
    }

    public static void respondedRequest(ResourceCreationRequest rcr) {
        pendingRequests.remove(rcr);
    }

    public static void confirmedRequest(String vmName, String provider, ResourceCreationRequest rcr) {
        CloudProvider cp = providers.get(provider);
        VM2Provider.put(vmName, cp);
        cp.createdVM(vmName, rcr.getGranted());
    }

    public static void refusedRequest(String provider, ResourceCreationRequest rcr) {
        CloudProvider cp = providers.get(provider);
        cp.refusedWorker(rcr.getRequested());
    }

    public static void performReduction(ResourceDestructionRequest reduction) {
        CloudProvider cp;
        cp = VM2Provider.get(reduction.getRequested().getName());
        cp.performReduction(reduction);
    }

    public static void addPendingReduction(ResourceDestructionRequest reduction) {
        CloudProvider cp;
        cp = VM2Provider.get(reduction.getRequested().getName());
        cp.addPendingReduction(reduction);

    }

    public static void confirmReduction(ResourceDestructionRequest reduction) {
        CloudProvider cp;
        cp = VM2Provider.get(reduction.getRequested().getName());
        cp.confirmReduction(reduction);
    }

    public static void terminatedInstance(String vm) {

    }

    public static HashMap<ResourceDescription, Integer> getVMComposition(String name) {
        CloudProvider cp;
        cp = VM2Provider.get(name);
        return cp.getVMComposition(name);
    }

    public static void newCoreElementsDetected(LinkedList<Integer> newCores) {
        for (CloudProvider cp : providers.values()) {
            cp.newCoreElementsDetected(newCores);
        }

    }

    public static String getCurrentState(String prefix) {
        StringBuilder sb = new StringBuilder(prefix + "Current Cloud State:\n");
        for (CloudProvider cp : providers.values()) {
            sb.append(cp.getCurrentState(prefix + "\t"));
        }
        sb.append(prefix).append("Pending Requests:\n");
        for (ResourceCreationRequest rcr : pendingRequests) {
            sb.append(prefix).append("\t").append(rcr.getRequested().getType());
        }
        return sb.toString();
    }
}
