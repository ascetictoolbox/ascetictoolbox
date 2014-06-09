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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.energy;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.MachineEnergyUsage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author Richard
 */
public class KwAPIClient extends JSONParser {

    /**
     * This takes a KwAPI formatted JSON dataset and returns the values for a
     * named host/physical machines energy meter readings.
     *
     * @param jsonDataset The Json file with the energy readings
     * @param host The name of the machine to get meter readings for
     * @return The energy usage record for the named resource
     */
    public MachineEnergyUsage parse(JsonObject jsonDataset, String host) {
        /**
         * The path structure to get to the data required is:
         * /probes/machine/metric/
         *///This is the list
        JsonObject probes = jsonDataset.getJsonObject("probes");
        JsonObject hostObj = probes.getJsonObject(host);
        if (hostObj == null) {
            return null;
        }
        JsonNumber powerValue = hostObj.getJsonNumber("w");
        JsonNumber energyValue = hostObj.getJsonNumber("kwh");
        JsonNumber timestamp = hostObj.getJsonNumber("timestamp");
        MachineEnergyUsage answer = new MachineEnergyUsage(host, timeStampToCal(timestamp).getTime(), powerValue.doubleValue(), -1, -1);
        return answer;
    }
    
    /**
     * This takes a json object describing a list of probe ids
     * i.e. machine names and returns a list of them.
     *
     * @param object document returned separates first on metric and then on
     * machine/resource, where the focus here is on a resource first!
     * @return
     */
    public ArrayList<String> parseProbeIDs(JsonObject object) {
        ArrayList<String> answer = new ArrayList<>();
        JsonArray probesIDs = object.getJsonArray("probe_ids");
        List<JsonString> machineList = probesIDs.getValuesAs(JsonString.class);
        for (JsonString host : machineList) {
            answer.add(host.getString());
        }
        return answer;
    }    

    /**
     * This takes a KwAPI formatted JSON dataset and returns the values for a
     * named host/physical machines energy meter readings.
     *
     * @param object document returned separates first on metric and then on
     * machine/resource, where the focus here is on a resource first!
     * @return
     */
    public HashSet<MachineEnergyUsage> parse(JsonObject object) {
        HashSet<MachineEnergyUsage> answer = new HashSet<>();
        JsonObject probes = object.getJsonObject("probes");
        Set<Map.Entry<String, JsonValue>> machineList = probes.entrySet();
        for (Map.Entry<String, JsonValue> host : machineList) {
            String machineName = host.getKey();
            JsonValue jsonValue = host.getValue();
            JsonObject values = (JsonObject) jsonValue;
            MachineEnergyUsage machine = new MachineEnergyUsage(machineName,
                    timeStampToCal(values.getJsonNumber("timestamp")).getTime(),
                    values.getJsonNumber("w").doubleValue(), -1, -1);
            answer.add(machine);
        }
        return answer;
    }

    /**
     * @deprecated some initial test code
     * @param args 
     */
    public static void main(String[] args) {
        try {
            JsonObject obj = getJsonObjectFromFile(new File("C:\\Users\\Richard\\Documents\\University Work\\Research\\ASCETiC\\ProjectSVN\\trunk\\iaas\\EnergyModeller\\src\\main\\java\\eu\\ascetic\\asceticarchitecture\\iaas\\energymodeller\\queryinterface\\datasourceclient\\energy\\sampleKwAPI.json"));
            MachineEnergyUsage test = new KwAPIClient().parse(obj, "Asok11");
            HashSet<MachineEnergyUsage> testing = new KwAPIClient().parse(obj);
            System.out.println("Named Search Test");
            System.out.println(test);
            System.out.println("List All Test");
            for (MachineEnergyUsage machineEnergyUsage : testing) {
                System.out.println(machineEnergyUsage);
            }
            JsonObject problist = getJsonObjectFromFile(new File("C:\\Users\\Richard\\Documents\\University Work\\Research\\ASCETiC\\ProjectSVN\\trunk\\iaas\\EnergyModeller\\src\\main\\java\\eu\\ascetic\\asceticarchitecture\\iaas\\energymodeller\\queryinterface\\datasourceclient\\energy\\probeids.json"));
            System.out.println("Machine List:");
            for(String machineName : new KwAPIClient().parseProbeIDs(problist)) {
                System.out.println(machineName);
            }
        } catch (Exception ex) {
            Logger.getLogger(KwAPIClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
