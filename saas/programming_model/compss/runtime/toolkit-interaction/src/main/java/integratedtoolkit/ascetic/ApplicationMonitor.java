/**
 *
 * Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights
 * reserved.
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
package integratedtoolkit.ascetic;

import integratedtoolkit.types.Implementation;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class ApplicationMonitor {

    private static final String endpoint = Configuration.getApplicationMonitorEndpoint();
    //private static final String endpoint = "http://192.168.3.16:9000/";

    public static String startEvent(VM vm, Implementation impl) {
        HttpClient client = new HttpClient();
        BufferedReader br = null;
        PostMethod method;
        if (endpoint.endsWith("/")) {
            method = new PostMethod(endpoint + "event");
        } else {
            method = new PostMethod(endpoint + "/event");
        }

        int coreId = impl.getCoreId();
        int implId = impl.getImplementationId();
        String eventType = generateEventType(coreId, implId);
        float eventWeight = vm.getEventWeight(coreId, implId);
        try {
            StringRequestEntity sre = new StringRequestEntity(
                    "{\"appId\":\"" + Configuration.getApplicationId()
                    + "\", \"nodeId\":\"" + vm.getProviderId()
                    + "\", \"instanceId\":\"" + Configuration.getDeploymentId()
                    + "\", \"data\":{ \"ip\":\"" + vm.getIPv4()
                    + "\", \"eventType\":\"" + eventType
                    + "\", \"eventWeight\":\"" + eventWeight
                    + "\"}}", "application/json", "UTF-8");
            System.out.println("Postin event " + sre.getContent() + " at App Monitor");
            method.setRequestEntity(sre);
            client.executeMethod(method);
            br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            String readLine;
            readLine = br.readLine();
            readLine = readLine.substring(8);
            int index = readLine.indexOf("\"");
            String id = readLine.substring(0, index);
            System.out.println("Posted event " + eventType + " at App Monitor with id " + id);
            return id;
        } catch (Exception e) {
            System.out.println("Error starting event");
            e.printStackTrace();
        } finally {
            method.releaseConnection();
            if (br != null) {
                try {
                    br.close();
                } catch (Exception fe) {
                }
            }
        }
        return "";
    }

    public static void stopEvent(String eventId) {
        String event;
        if (endpoint.endsWith("/")) {
            event = endpoint + "event/" + eventId;
        } else {
            event = endpoint + "/event/" + eventId;
        }
        PostMethod method = new PostMethod(event) {
            @Override
            public String getName() {
                return "PATCH";
            }
        };
        HttpClient client = new HttpClient();
        try {
            client.executeMethod(method);
            System.out.println("Stop event method for " + eventId + " executed.");
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
    }

    public static void main(String[] args) throws Exception {

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(endpoint + "event");
        method.setRequestEntity(new StringRequestEntity("{\"appId\":\"JEPlus\", \"nodeId\":\"a305c18f-dd76-4515-b9b9-1fc488368bbc\", \"instanceId\":\"549\", \"data\":{ \"ip\":\"192.168.13.10\", \"eventType\":\"core0impl0\"}}", "application/json", "UTF-8"));
        try {
            client.executeMethod(method);
            BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            //System.out.println("Return: " +new String(method.getResponseBody()));
            String readLine;
            readLine = br.readLine();
            readLine = readLine.substring(8);
            int index = readLine.indexOf("\"");
            String id = readLine.substring(0, index);
            //startEvent(vm, "core0impl0");

            System.out.println("ID is " + id + "(" + readLine + ")");
            Thread.sleep(20000);
            stopEvent(id);
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        //stopEvent("5602c3a0f4bb2e377080bb7b");

    }

    private static String generateEventType(int coreId, int implId) {
        return "core" + coreId + "impl" + implId;
    }
}
