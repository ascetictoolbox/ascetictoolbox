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

package eu.ascetic.asceticarchitecture.iaas.energymodellerdatalogger;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.io.File;
import java.util.logging.Level;

/**
 * 
 *
 */
public class Logger 
{
    public static void main( String[] args )
    {
         MeasurementLogger logger = new MeasurementLogger(new File("Dataset.txt"), false);
         new Thread(logger).start();
         ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
         Host host = adaptor.getHostByName("asok10");
         for(int i = 0; i < 10; i++) {
            logger.printToFile(adaptor.getHostData(host));
             try {
                 Thread.sleep(5000);
             } catch (InterruptedException ex) {
                 java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         logger.stop();
        
    }
}
