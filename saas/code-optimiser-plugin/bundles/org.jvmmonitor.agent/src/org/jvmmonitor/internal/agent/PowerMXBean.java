package org.jvmmonitor.internal.agent;

import java.util.ArrayList;
import java.util.List;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;

public interface PowerMXBean {

    /**
     * Gets the state indicating if monitoring SWT resources is supported.
     * 
     * @return <tt>true</tt> if monitoring SWT resources is supported
     */
    public boolean isSupported();
    
    /**
     * Gets the value for the attribute power
     * 
     * @return the value for power
     */
    public double getPower();
    
    /**
     * This gets the host calibration data
     * @param calibrationData
     */        
    public String getHostCalibrationInputString();    
    
    /**
     * This sets the host calibration data for this agent.
     * @param datapoints The datapoints to use for estimating energy usage, semi-colon separated.
     */
    public void setHostCalibrationInputString(String datapoints);    
    
    /**
     * This sets the host calibration data for this agent.
     * @param datapoints The datapoints to use for estimating energy usage.
     */
    public void setHostCalibrationData(List<HostEnergyCalibrationData> datapoints);
    
    /**
     * This gets the host calibration data for this agent.
     * @return The datapoints to use for estimating energy usage.
     */
    public List<HostEnergyCalibrationData> getHostCalibrationData();
    
	
}
