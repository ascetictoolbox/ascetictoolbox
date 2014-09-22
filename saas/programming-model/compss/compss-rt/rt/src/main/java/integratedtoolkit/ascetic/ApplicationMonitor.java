/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class ApplicationMonitor {

    private static final String endpoint = Configuration.getApplicationMonitorEndpoint();

    public static String startEvent(VM vm, String eventType) {
        HttpClient client = new HttpClient();
        BufferedReader br = null;
        PostMethod method;
        if (endpoint.endsWith("/")) {
            method = new PostMethod(endpoint + "event");
        } else {
            method = new PostMethod(endpoint + "/event");
        }
        try {
            method.setRequestEntity(new StringRequestEntity("{\"appId\":\"" + Configuration.getApplicationId() + "\", \"nodeId\":\"" + vm.getProviderId() + "\", \"instanceId\":\"" + Configuration.getDeploymentId() + "\", \"data\":{ \"ip\":\""+vm.getIPv4()+"\"}}", "application/json", "UTF-8"));
            client.executeMethod(method);
            br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            String readLine;
            readLine = br.readLine();
            readLine = readLine.substring(8);
            int index = readLine.indexOf("\"");
            return readLine.substring(0, index);
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
    }

    /*public static void main(String[] args) throws Exception { 
        Configuration.getComponentDescriptions().put("componentA", new ResourceDescription());
        VM vm = new VM("10.0.0.5", UUID.randomUUID().toString(), "componentA");

        String eventId0 = startEvent(vm, "core" + 0 + "impl" + 0);
        String eventId1 = startEvent(vm, "core" + 0 + "impl" + 1);
        Thread.sleep(5000);
        stopEvent(eventId0);
        stopEvent(eventId1);
        Thread.sleep(5000);

        eventId0 = startEvent(vm, "core" + 0 + "impl" + 0);
        Thread.sleep(300);
        eventId1 = startEvent(vm, "core" + 0 + "impl" + 0);
        Thread.sleep(4700);
        stopEvent(eventId0);
        Thread.sleep(300);
        stopEvent(eventId1);

    }*/

}
