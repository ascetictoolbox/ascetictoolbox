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
                    + "\", \"nodeId\":\"" + vm.getAMId()
                    + "\", \"instanceId\":\"" + Configuration.getDeploymentId()
                    + "\", \"data\":{ \"ip\":\"" + vm.getIPv4()
                    + "\", \"eventType\":\"" + eventType
                    + "\", \"eventWeight\":\"" + eventWeight
                    + "\"}}", "application/json", "UTF-8");
            method.setRequestEntity(sre);
            client.executeMethod(method);
            br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            String readLine;
            readLine = br.readLine();
            readLine = readLine.substring(8);
            int index = readLine.indexOf("\"");
            String id = readLine.substring(0, index);
            return id;
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

    private static String generateEventType(int coreId, int implId) {
        return "core" + coreId + "impl" + implId;
    }
}
