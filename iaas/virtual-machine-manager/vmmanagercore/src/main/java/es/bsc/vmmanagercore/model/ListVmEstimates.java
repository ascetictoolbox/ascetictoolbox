package es.bsc.vmmanagercore.model;

import java.util.List;

/**
 * List of VM estimates.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
// Note: At least for now, this class is only useful to make easier the conversion from JSON using Gson.
public class ListVmEstimates {

    private List<VmEstimate> estimates;

    public ListVmEstimates(List<VmEstimate> estimates) {
        this.estimates = estimates;
    }

}
