/**
 * 
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
