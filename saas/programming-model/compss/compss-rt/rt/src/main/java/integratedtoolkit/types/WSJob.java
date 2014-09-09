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
package integratedtoolkit.types;

import integratedtoolkit.components.JobStatus.JobEndStatus;
import integratedtoolkit.util.RequestDispatcher;
import integratedtoolkit.util.RequestQueue;
import integratedtoolkit.util.Serializer;
import integratedtoolkit.util.ThreadPool;

import integratedtoolkit.api.ITExecution.*;
import integratedtoolkit.api.impl.IntegratedToolkitImpl;
import integratedtoolkit.types.Parameter.DependencyParameter.*;
import integratedtoolkit.types.Parameter.*;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataAccessId.RAccessId;
import integratedtoolkit.types.data.DataAccessId.RWAccessId;
import integratedtoolkit.types.data.Location;

import org.apache.cxf.endpoint.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import org.apache.cxf.endpoint.ClientCallback;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

public class WSJob extends Job {

    private static RequestQueue<WSJob> callerQueue;
    private static WSCaller caller;
    private static final JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
    //wsdl-port--> Client
    private static final HashMap<String, Client> portToClient = new HashMap<String, Client>();

    // Pool of worker threads and queue of requests
    private static ThreadPool callerPool;

    private static final int POOL_SIZE = 10;
    private static final String POOL_NAME = "WS";
    private static final String THREAD_POOL_ERR = "Error starting pool of threads";
    private static final String SUBMIT_ERROR = "Error calling Web Service";

    private Object returnValue;

    private static Map<String, Object> renameToObject;

    public static void init() throws Exception {
        // Create thread that will handle job submission requests
        if (callerQueue == null) {
            callerQueue = new RequestQueue<WSJob>();
        } else {
            callerQueue.clear();
        }

        caller = new WSCaller(callerQueue);
        callerPool = new ThreadPool(POOL_SIZE, POOL_NAME, caller);
        try {
            callerPool.startThreads();
        } catch (Exception e) {
            logger.error(THREAD_POOL_ERR, e);
            throw e;
        }

        renameToObject = Collections.synchronizedSortedMap(new TreeMap<String, Object>());
    }

    public static void end() {
        try {
            callerPool.stopThreads();
        } catch (Exception e) {
        }
    }

    public static void setObjectVersionValue(String name, Object value) {
        renameToObject.put(name, value);
    }

    public static Object getObjectVersionValue(String name) {
        return renameToObject.get(name);
    }

    public static boolean isInMemory(String name) {
        return renameToObject.get(name) != null;
    }

    public WSJob(Task task, Implementation impl, Resource res) {
        super(task, impl, res);
        this.returnValue = null;
    }

    public JobKind getKind() {
        return JobKind.SERVICE;
    }

    public void submit() {
        callerQueue.enqueue(this);
    }

    public void stop() {

    }

    public Object getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        buffer.append("[[Job id: ").append(getJobId()).append("]");
        buffer.append(", ").append(getCore().toString());
        buffer.append(", [Target URL: ").append(res.getName()).append("]]");

        return buffer.toString();

    }

    @Override
    public Location getTransfersLocation() {
        return IntegratedToolkitImpl.masterSafeLocation;
    }

    @Override
    public String getHostName() {
        return this.res.getName();
    }

    static class WSCaller extends RequestDispatcher<WSJob> {

        public WSCaller(RequestQueue<WSJob> queue) {
            super(queue);
        }

        public void processRequests() {
            while (true) {
                WSJob job = null;
                job = queue.dequeue();
                if (job == null) {
                    break;
                }

                try {
                    ArrayList<Object> input = new ArrayList<Object>();
                    TaskParams taskParams = job.task.getTaskParams();
                    Service service = (Service) job.impl;
                    Parameter[] parameters = taskParams.getParameters();
                    for (int i = 0; i < taskParams.getParameters().length; i++) {
                        if (parameters[i].getDirection() == ParamDirection.IN) {
                            switch (parameters[i].getType()) {
                                case OBJECT_T:
                                    ObjectParameter otPar = (ObjectParameter) parameters[i];
                                    checkIfInMemoryOrFile(otPar);
                                    input.add(otPar.getValue());
                                    break;
                                case FILE_T:
                                    //¿¿¿¿?????
                                    //CAN'T USE A FILE AS A PARAMETER
                                    //SKIP!
                                    break;
                                default:// Basic or String
                                    BasicTypeParameter btParB = (BasicTypeParameter) parameters[i];
                                    input.add(btParB.getValue());

                            }

                        }
                    }

                    ServiceInstance si = (ServiceInstance) job.res;
                    String portName = service.getPortName();
                    String operationName = service.getOperation();
                    if (operationName.compareTo("[unassigned]") == 0) {
                        operationName = taskParams.getName();
                    }
                    Client client = getClient(si, portName);

                    //Object[] result = client.invoke(operationName, input.toArray());
                    ClientCallback cb = new ClientCallback();
                    client.invoke(cb, operationName, input.toArray());
                    /*for (Object o : input.toArray()) {
                     logger.debug("Service parameter " + o);
                     }*/
                    Object[] result = cb.get();

                    if (result.length > 0) {
                        job.returnValue = result[0];
                    }
                    //logger.debug("Service result: " + job.returnValue);
                    associatedJM.jobStatusNotification(job, JobEndStatus.OK);
                } catch (Throwable e) {
                    associatedJM.jobStatusNotification(job, JobEndStatus.EXECUTION_FAILED);
                    logger.error(SUBMIT_ERROR, e);
                    return;
                }

            }
        }

        private void checkIfInMemoryOrFile(ObjectParameter otPar) {
            String rename;
            DataAccessId oaId = otPar.getDataAccessId();
            switch (otPar.getDirection()) {
                case IN:
                    rename = ((RAccessId) oaId).getReadDataInstance().getRenaming();
                    break;
                case INOUT:
                    rename = ((RWAccessId) oaId).getReadDataInstance().getRenaming();
                    break;
                case OUT:
                    return;
                default:
                    return;
            }

            // Check if the object was the return value of a service
            Object value = renameToObject.get(rename);
            if (value != null) {
                otPar.setValue(value);
                return;
            }

            // Check if the object is in a local file	
            String path = ((DependencyParameter) otPar).getDataRemotePath();
            try {
                value = Serializer.deserialize(path);
            } catch (Exception e) {
                // Object is not in file
                return;
            }
            otPar.setValue(value);
        }

        private Client getClient(ServiceInstance si, String portName) {
            Client c = portToClient.get(si.getName() + "-" + portName);
            if (c == null) {
                c = addPort(si, portName);
                portToClient.put(si.getName() + "-" + portName, c);
            }
            return c;
        }

        public synchronized Client addPort(ServiceInstance si, String portName) {
            Client client = portToClient.get(portName);
            if (client != null) {
                return client;
            }

            QName serviceQName = new QName(si.getNamespace(), si.getServiceName());
            QName portQName = new QName(si.getNamespace(), portName);
            client = dcf.createClient(si.getWsdl(), serviceQName, portQName);

            HTTPConduit http = (HTTPConduit) client.getConduit();
            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
            httpClientPolicy.setConnectionTimeout(0);
            httpClientPolicy.setReceiveTimeout(0);
            http.setClient(httpClientPolicy);

            portToClient.put(portName, client);
            return client;
        }
    }

}
