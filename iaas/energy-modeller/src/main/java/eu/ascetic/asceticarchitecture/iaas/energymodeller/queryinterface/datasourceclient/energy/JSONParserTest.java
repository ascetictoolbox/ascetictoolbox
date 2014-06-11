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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;

/**
 * See: http://www.oracle.com/technetwork/articles/java/json-1973242.html See
 * tutorial: http://docs.oracle.com/javaee/7/tutorial/doc/jsonp001.htm
 *
 * @author Richard
 */
public class JSONParserTest {

    public JSONParserTest() {
    }

    public static void main(String[] args) {
        try {
            new JSONParserTest().test(new File("C:\\Users\\Richard\\Documents\\University Work\\Research\\ASCETiC\\ProjectSVN\\trunk\\iaas\\EnergyModeller\\src\\main\\java\\eu\\ascetic\\asceticarchitecture\\iaas\\energymodeller\\queryinterface\\datasourceclient\\energy\\sample.json"));
        } catch (Exception ex) {
            Logger.getLogger(JSONParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void test() throws MalformedURLException, IOException {

        URL url = new URL("https://ascetic.cit.tu-berlin.de/www-data/meters.php");
        try (InputStream is = url.openStream();
                JsonReader rdr = Json.createReader(is)) {

            JsonObject obj = rdr.readObject();
            navigateTree(obj, null);
        }
    }

    public void test(File file) throws IOException {

        try (FileInputStream is = new FileInputStream(file);
                JsonReader rdr = Json.createReader(is)) {

            JsonObject obj = rdr.readObject();
//            navigateTree(obj, null);
            JsonObject power = obj.getJsonObject("power");
            Set<Map.Entry<String, JsonValue>> machineList = power.entrySet();
            JsonObject machine = power.getJsonObject("asok11");
            JsonNumber value = machine.getJsonNumber("value");
            JsonNumber timestamp = machine.getJsonNumber("timestamp");

            System.out.println("Via Named Machine Method");
            System.out.println("Value: " + value.doubleValue());
            System.out.println("timestamp: " + timestamp.doubleValue());
            System.out.println("Via Listing Method");
            for (Map.Entry<String, JsonValue> resource : machineList) {
                String key = resource.getKey();
                JsonValue jsonValue = resource.getValue();
                if (!"unit".equals(key)) {
//                    System.out.println(jsonValue.getValueType());
                    System.out.println("Machine: " + key);
                    
                    JsonObject values = (JsonObject) jsonValue;
                    System.out.println("Value: " + values.getJsonNumber("value"));
                    System.out.println("timestamp: " + values.getJsonNumber("timestamp"));
                    Calendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(values.getJsonNumber("timestamp").longValue() * 1000);
                    System.out.println("timestamp: " + cal.getTime());
                }

            }
            //navigateTree(power, null);
        }
    }

    public static void navigateTree(JsonValue tree, String key) {
        if (key != null) {
            System.out.print("Key " + key + ": ");
        }
        switch (tree.getValueType()) {
            case OBJECT:
                System.out.println("OBJECT");
                JsonObject object = (JsonObject) tree;
                for (String name : object.keySet()) {
                    navigateTree(object.get(name), name);
                }
                break;
            case ARRAY:
                System.out.println("ARRAY");
                JsonArray array = (JsonArray) tree;
                for (JsonValue val : array) {
                    navigateTree(val, null);
                }
                break;
            case STRING:
                JsonString st = (JsonString) tree;
                System.out.println("STRING " + st.getString());
                break;
            case NUMBER:
                JsonNumber num = (JsonNumber) tree;
                System.out.println("NUMBER " + num.toString());
                break;
            default: //i.e. TRUE, FALSE, or NULL
                System.out.println(tree.getValueType().toString());
                break;
        }
    }
}
