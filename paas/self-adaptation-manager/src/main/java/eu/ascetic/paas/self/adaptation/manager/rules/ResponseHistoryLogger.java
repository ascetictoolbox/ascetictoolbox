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
package eu.ascetic.paas.self.adaptation.manager.rules;

import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.io.File;

/**
 * This logs out a history of event responses to disk.
 * @author Richard Kavanagh
 */
public class ResponseHistoryLogger extends GenericLogger<Response> {

    /**
     * This creates a new response history data logger
     *
     * @param file The file to write the log out to.
     * @param overwrite If the file should be overwritten on starting the energy
     * modeller.
     */
    public ResponseHistoryLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }    
    
    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Time");
        store.append("Application ID");
        store.append("Deployment ID");
        store.append("VM ID");
        store.append("Action Type");
        store.append("Further Details");
        store.append("Sla Uuid");
        store.append("Agreement Term");
        store.append("Guranteed Value");          
        store.append("Raw Value");   
        store.append("Gurantee Operator");
        store.append("Able to Respond");
        store.append("Action Performed"); 
    }

    @Override
    public void writebody(Response response, ResultsStore store) {
        store.add(response.getTime());
        store.append(response.getApplicationId());
        store.append(response.getDeploymentId());
        store.append(response.getVmId());
        store.append(response.getActionType().toString());
        store.append(response.getAdapationDetails());
        store.append(response.getCause().getSlaUuid());
        store.append(response.getCause().getAgreementTerm());
        store.append(response.getCause().getGuranteedValue());          
        store.append(response.getCause().getRawValue());   
        store.append(response.getCause().getGuranteeOperator());
        store.append(response.isPossibleToAdapt());
        store.append(response.isPerformed());   
    }
    
}
