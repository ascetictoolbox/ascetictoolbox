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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * A client that connects to a Zabbix data source for providing energy readings
 * of the infrastructure. This is the default in the ASCETiC architecture.
 *
 * @author Richard
 */
public class ZabbixClient extends JSONParser {

    /**
     * TUB Testbed Info:
     * https://forge.cetic.be/projects/ascetic/wiki/TUB_Testbed#Zabbix *
     *
     * Zabbix Interaction:
     *
     * The API: https://www.zabbix.com/documentation/2.0/manual/appendix/api/api
     *
     * The Java Gateway: Zabbix V 2.0 and above (A java JMX bridge service
     * https://www.zabbix.com/documentation/2.0/manual/concepts/java#getting_java_gateway
     * http://www.oracle.com/technetwork/java/javase/tech/javamanagement-140525.html
     *
     * ZabCat:
     * http://stackoverflow.com/questions/8637196/jmx-monitoring-using-zabbix
     * http://sourceforge.net/projects/zapcat/
     * http://www.kjkoster.org/zapcat/Zabbix_Java_Template.html
     *
     * JMX_Zabbix_Bridge:
     * http://www.kjkoster.org/zapcat/Zapcat_JMX_Zabbix_Bridge.html
     *
     * JSON: http://mytechattempts.wordpress.com/tag/zabbix-api/
     * https://www.zabbix.com/forum/showthread.php?t=20755
     * https://www.zabbix.com/forum/showthread.php?t=19630
     * http://mytechattempts.wordpress.com/tag/zabbix-api/
     *
     */
    public void read() {
        JsonObject obj = getJsonObjectFromURL("https://ascetic.cit.tu-berlin.de/www-data/meters.php");
        parse(obj, "asok11");
    }

    /**
     * This takes a Zabbix formatted JSON dataset and returns the values for a
     * named host/physical machines energy meter readings.
     *
     * @param jsonDataset The Json file with the energy readings
     * @param host The name of the machine to get meter readings for
     * @return The energy usage record for the named resource
     */
    public MachineEnergyUsage parse(JsonObject jsonDataset, String host) {
        /**
         * The path structure to get to the data required is:
         * /measure/power/value /measure/power/timestamp
         */
        JsonObject power = jsonDataset.getJsonObject("power"); //such as "power"
        JsonObject machine = power.getJsonObject(host);
        JsonNumber powerValue = machine.getJsonNumber("value");
        JsonNumber timestamp = machine.getJsonNumber("timestamp");
        // and /measure/voltage/value
        JsonObject voltage = jsonDataset.getJsonObject("voltage"); //such as "power"
        machine = voltage.getJsonObject(host);
        JsonNumber voltageValue = machine.getJsonNumber("value");
        MachineEnergyUsage answer = new MachineEnergyUsage(host, timeStampToCal(timestamp).getTime(), powerValue.doubleValue(), voltageValue.doubleValue(), -1);
        return answer;
    }

    /**
     * @deprecated some initial test code
     * @param args 
     */    
    public static void main(String[] args) {
        try {
            JsonObject obj = getJsonObjectFromFile(new File("C:\\Users\\Richard\\Documents\\University Work\\Research\\ASCETiC\\ProjectSVN\\trunk\\iaas\\EnergyModeller\\src\\main\\java\\eu\\ascetic\\asceticarchitecture\\iaas\\energymodeller\\queryinterface\\datasourceclient\\energy\\sample.json"));
            MachineEnergyUsage test = new ZabbixClient().parse(obj, "asok11");
            System.out.println(test);
        } catch (Exception ex) {
            Logger.getLogger(JSONParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This 
     * @param object
     * @deprecated This is unlikely to scale well as the 
     * document returned separates first on metric and then on 
     * machine/resource, where the focus here is on a resource first!
     * @return 
     */
    public HashSet<MachineEnergyUsage> parse(JsonObject object) {
        HashSet<MachineEnergyUsage> answer = new HashSet<>();
        JsonObject power = object.getJsonObject("power");
        Set<Map.Entry<String, JsonValue>> machineListPower = power.entrySet();
        for (Map.Entry<String, JsonValue> resource : machineListPower) {
            String machineName = resource.getKey();
            JsonValue jsonValue = resource.getValue();
            if (!"unit".equals(machineName)) {
                JsonObject values = (JsonObject) jsonValue;
                MachineEnergyUsage machine = new MachineEnergyUsage(machineName, 
                        timeStampToCal(values.getJsonNumber("timestamp")).getTime(), 
                        values.getJsonNumber("value").doubleValue(), -1, -1);
                answer.add(machine);
            }
        }
        
        JsonObject voltage = object.getJsonObject("voltage");
        Set<Map.Entry<String, JsonValue>> machineListVoltage = voltage.entrySet();
        for (Map.Entry<String, JsonValue> resource : machineListVoltage) {
            String machineName = resource.getKey();  
            //JsonValue is either unit information or actual data values needed.
            JsonValue jsonValue = resource.getValue();
            if (!"unit".equals(machineName)) {
                JsonObject values = (JsonObject) jsonValue;
                MachineEnergyUsage machine = search(answer, machineName);
                machine.setVolts(values.getJsonNumber("value").doubleValue());
                answer.add(machine);
            }
        }          
        return answer;
    }
    
    private MachineEnergyUsage search(HashSet<MachineEnergyUsage> searchSpace, String resourceID) {
        for (MachineEnergyUsage machineEnergyUsage : searchSpace) {
            if (machineEnergyUsage.getDeviceID().equals(resourceID)) {
                return machineEnergyUsage;
            }
        }
        return null;
    }
}
