package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import java.util.List;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ListVmEstimates {

    private List<VmEstimate> estimates;

    public ListVmEstimates(List<VmEstimate> estimates) {
        this.estimates = estimates; 
    }
    
    public List<VmEstimate> getVmsEstimates() {
        return estimates;
    }


}
