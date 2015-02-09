package vmm;

import com.google.gson.*;
import httpClient.HttpClient;
import models.Host;
import models.ListVms;
import models.Vm;

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
