package org.jvmmonitor.core;

import java.util.List;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;

public interface IPowerMonitor {

    /** The MXBean name. */
    public final static String POWER_MXBEAN_NAME = "org.jvmmonitor:type=Power";
    
    public double calculatePowerConsumption(double cpuUsage);    
    
    /**
     * Sets the tracking state.
     * 
     * @param tracking
     *            <tt>true</tt> to enable tracking
     * @throws JvmCoreException
     */
    void setTracking(boolean tracking) throws JvmCoreException;

    /**
     * Gets the tracking state.
     * 
     * @return <tt>true</tt> if tracking is enabled
     * @throws JvmCoreException
     */
    boolean isTracking() throws JvmCoreException;
    
    /**
     * This sets the host calibration data
     * @param calibrationData
     */    
    public void setHostCalibrationInputString(String calibrationData) throws JvmCoreException;    
    
    /**
     * This sets the host calibration data
     * @param calibrationData
     */    
    public void setHostCalibrationData(List<HostEnergyCalibrationData> calibrationData) throws JvmCoreException;

    /**
     * Refreshes the resources cache.
     * 
     * @throws JvmCoreException
     */
    void refreshResourcesCache() throws JvmCoreException;

    /**
     * Clears the tracked resources.
     * 
     * @throws JvmCoreException
     */
    void clear() throws JvmCoreException;

    /**
     * Gets the state indicating if SWT resource monitor is supported.
     * 
     * @return <tt>true</tt> if SWT resource monitor is supported
     */
    boolean isSupported();	
	
}
