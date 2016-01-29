/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.power_button_presser.vmm;

import com.google.gson.*;
import es.bsc.power_button_presser.httpClient.HttpClient;
import es.bsc.power_button_presser.models.Host;
import es.bsc.power_button_presser.models.ListVms;
import es.bsc.power_button_presser.models.Vm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VmmClient {

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final JsonParser parser = new JsonParser();

    private final String baseUrl;
    private final String vmsPath;
    private final String hostsPath;
    private final String nodePath;
    private final String powerButtonPath;

    public VmmClient(HttpClient httpClient, String baseUrl, String vmsPath, String hostsPath,
                     String nodePath, String powerButtonPath) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.vmsPath = vmsPath;
        this.hostsPath = hostsPath;
        this.nodePath = nodePath;
        this.powerButtonPath = powerButtonPath;
    }
    
    public List<Vm> getVms() {
        String httpResponse = "";
        try {
            httpResponse = httpClient.get(baseUrl + vmsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gson.fromJson(httpResponse, ListVms.class).getVms();
    }
    
    public List<Host> getHosts() {
        String httpResponse = "";
        try {
            httpResponse = httpClient.get(baseUrl + hostsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Host> result = new ArrayList<>();
        JsonArray hostsJsonArray = parser.parse(httpResponse).getAsJsonObject().get("nodes").getAsJsonArray();
        for (JsonElement hostJsonElement: hostsJsonArray) {
            JsonObject hostJsonObject = hostJsonElement.getAsJsonObject();
            result.add(new Host(
                    hostJsonObject.get("hostname").getAsString(),
                    hostJsonObject.get("totalCpus").getAsInt(),
                    hostJsonObject.get("totalMemoryMb").getAsInt(),
                    hostJsonObject.get("totalDiskGb").getAsInt(),
                    hostJsonObject.get("assignedCpus").getAsInt(),
                    hostJsonObject.get("assignedMemoryMb").getAsInt(),
                    hostJsonObject.get("assignedDiskGb").getAsInt(),
                    hostJsonObject.get("turnedOff").getAsJsonObject().get("value").getAsInt() == 1));
        }
        return result;
    }
    
    public void pressPowerButton(String hostname) {
        try {
            httpClient.put(baseUrl + nodePath + hostname + "/" + powerButtonPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
