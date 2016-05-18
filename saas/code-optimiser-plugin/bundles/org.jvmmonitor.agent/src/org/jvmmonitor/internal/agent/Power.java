package org.jvmmonitor.internal.agent;

import java.lang.instrument.Instrumentation;

public class Power implements PowerMXBean {

	/** The MXBean name. */
	public final static String POWER_MXBEAN_NAME = "org.jvmmonitor:type=Power";	

    /** The instrumentation. */
    private Instrumentation inst;    
    
    /**
     * The constructor.
     * 
     * @param inst
     *            The instrumentation
     */
    public Power(Instrumentation inst) {
        this.inst = inst;
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
        return 0.5;
    }    
    
}
