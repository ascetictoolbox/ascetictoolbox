/**
 * Copyright 2015 University of Leeds
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
package eu.ascetic.paas.self.adaptation.manager.rules.loggers;

import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.EventData;
import java.io.File;

/**
 *
 * This logs out a history of event arrivals to disk.
 * @author Richard Kavanagh
 */
public class EventHistoryLogger extends GenericLogger<EventData> {

    /**
     * This creates a new logger that is used to write out to disk event data.
     * @param file The file to write the log out to
     * @param overwrite If the file should be overwritten on starting the logger
     */
    public EventHistoryLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }    
    
    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Time");
        store.append("Application ID");
        store.append("Deployment ID");
        store.append("Sla Uuid");
        store.append("Agreement Term");
        store.append("Guranteed Value");          
        store.append("Raw Value");   
        store.append("Gurantee Operator");   
    }

    @Override
    public void writebody(EventData eventData, ResultsStore store) {
        store.add(eventData.getTime());
        store.append(eventData.getApplicationId());
        store.append(eventData.getDeploymentId());
        store.append(eventData.getSlaUuid());
        store.append(eventData.getAgreementTerm());
        store.append(eventData.getGuranteedValue());          
        store.append(eventData.getRawValue());   
        store.append(eventData.getGuranteeOperator()); 
    }
    
}
