/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.ascetic.paas.slam.poc.optimization;

import java.util.Collection;
import java.util.Iterator;
import datastructure.Request;
import eu.ascetic.paas.slam.poc.exceptions.RequestNotCorrectException;


/**
 * Each <code>RequestProcessor</code> represents an infrastructure provider.
 * 
 */
public class RequestProcessor {
    private String providerName;
    private Collection<Request> requestList;

    public RequestProcessor(String providerName) {
    }

    /**
     * Gets the name of provider.
     */
    public String getProviderName() {
        return providerName;
    }
    /**
     * Gets the request.
     */
    public Collection<Request> getRequest() {
        return requestList;
    }

    public void startProcess(Collection<Request> requestList) throws RequestNotCorrectException {
        this.requestList = requestList;
        if (this.requestList == null) {
            throw new RequestNotCorrectException("The incoming request is either null or incorrect format.");
        }

        // set the client type
        Iterator<Request> it = this.requestList.iterator();
        while (it.hasNext()) {
            Request request = (Request) it.next();
            // start process
            this.process(request);
        }
    }

    private void process(Request request) {
        this.strategy();
    }
    
    private void strategy() {
        this.processQoS();
        this.processFinalRisk();
        this.processFinalCost();
        this.processFinalProfit();
    }

    private void processFinalProfit() {
    }
    
    private void processFinalCost(){
        
    }
    private void processFinalRisk(){
        
    }
    private void processQoS(){
        
    }

}
