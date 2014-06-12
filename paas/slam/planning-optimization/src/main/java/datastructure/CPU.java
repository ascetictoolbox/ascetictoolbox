package datastructure;

import utils.Constant;


/**
 * <code>CPU</code> represents CPU in resource request.
 * 
 */
public class CPU extends Resource {
    /**
     * CPU speed.
     */
    private float speed;
    /**
     * CPU architecture.
     */
    private String architecture = "";

    public CPU() {
        this.setResourceName(Constant.CPU);
    }

    /**
     * Gets speed.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets speed.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Gets architecture.
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * Sets architecture.
     */
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

}
