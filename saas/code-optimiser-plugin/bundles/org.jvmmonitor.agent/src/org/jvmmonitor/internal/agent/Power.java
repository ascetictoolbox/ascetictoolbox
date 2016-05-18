package org.jvmmonitor.internal.agent;

import java.util.concurrent.ThreadLocalRandom;

public class Power implements PowerMXBean {

    /** The MXBean name. */
    public final static String POWER_MXBEAN_NAME = "org.jvmmonitor:type=Power";

    /**
     * The constructor.
     */
    public Power() {
    }

    /**
     * Gets the state indicating if monitoring SWT resources is supported.
     * 
     * @return <tt>true</tt> if monitoring SWT resources is supported
     */
    public boolean isSupported() {
        return true;
    }

    /**
     * Gets the value for the attribute power
     * 
     * @return the value for power
     */
    public double getPower() {
        
        double least = 0.5;
        double bound = 2.5;
        double randomNum = ThreadLocalRandom.current().nextDouble(least, bound);

        return randomNum;
    }

}
