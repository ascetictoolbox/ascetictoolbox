/**
 * 
 */
package datastructure;

/**
 * <code>QoS</code> represents QoS attributes in resource request.
 * 
 */
public class QoS {
    /**
     * Service availability.
     */
    private double availability;
    /**
     * Service isolation attribute.
     */
    private boolean isolation;

    public QoS(double availability, boolean isolation) {
        this.availability = availability;
        this.isolation = isolation;
    }

    /**
     * Gets availability parameter.
     */
    public double getAvailability() {
        return availability;
    }

    /**
     * Sets availability
     */
    public void setAvailability(float availability) {
        this.availability = availability;
    }

    /**
     * To evaluate whether isolation is selected or not.
     */
    public boolean isIsolation() {
        return isolation;
    }

    /**
     * Sets isolation.
     */
    public void setIsolation(boolean isolation) {
        this.isolation = isolation;
    }
}
