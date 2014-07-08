package es.bsc.vmmanagercore.model;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmEstimate {

    private String id;
    private double powerEstimation;
    private double priceEstimation;

    public VmEstimate(String id, double powerEstimation, double priceEstimation) {
        this.id = id;
        this.powerEstimation = powerEstimation;
        this.priceEstimation = priceEstimation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPowerEstimation() {
        return powerEstimation;
    }

    public void setPowerEstimation(double powerEstimation) {
        this.powerEstimation = powerEstimation;
    }

    public double getPriceEstimation() {
        return priceEstimation;
    }

    public void setPriceEstimation(double priceEstimation) {
        this.priceEstimation = priceEstimation;
    }

}
