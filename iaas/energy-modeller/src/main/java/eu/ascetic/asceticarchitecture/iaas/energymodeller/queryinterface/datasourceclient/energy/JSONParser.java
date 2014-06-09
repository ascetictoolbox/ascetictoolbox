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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author Richard
 */
public class JSONParser extends DefaultEnergyClient {

    /**
     * This creates a JsonObject from a file based source.
     *
     * @param file The file to get the JDON dataset from
     * @return A JsonObject representing the dataset that was found. This will
     * return null if there is an error.
     */
    protected static JsonObject getJsonObjectFromFile(File file) {

        try (FileInputStream is = new FileInputStream(file);
                JsonReader rdr = Json.createReader(is)) {

            JsonObject obj = rdr.readObject();
            return obj;
        } catch (IOException ex) {
            Logger.getLogger(ZabbixClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This creates a JsonObject from a url based source.
     *
     * @param url The url to get the JSON dataset from
     * @return A JsonObject representing the dataset that was found. This will
     * return null if there is an error.
     */
    protected static JsonObject getJsonObjectFromURL(String url) {
        try { //ASCETiC url = https://ascetic.cit.tu-berlin.de/www-data/meters.php
            URL urls = new URL(url);
            try (
                    final InputStream is = urls.openStream();
                    JsonReader rdr = Json.createReader(is)) {

                JsonObject obj = rdr.readObject();
                return obj;
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(ZabbixClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ZabbixClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
       
    protected static Calendar timeStampToCal(JsonNumber timestamp) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(timestamp.longValue()));
        return cal;
    }    
    
}
