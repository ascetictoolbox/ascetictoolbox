package es.bsc.vmmanagercore.model;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmEstimate {

    private String id;
    private double powerEstimate;
    private double priceEstimate;

    public VmEstimate(String id, double powerEstimate, double priceEstimate) {
        this.id = id;
        this.powerEstimate = powerEstimate;
        this.priceEstimate = priceEstimate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPowerEstimate() {
        return powerEstimate;
    }

    public void setPowerEstimate(double powerEstimate) {
        this.powerEstimate = powerEstimate;
    }

    public double getPriceEstimate() {
        return priceEstimate;
    }

    public void setPriceEstimate(double priceEstimate) {
        this.priceEstimate = priceEstimate;
    }

}
