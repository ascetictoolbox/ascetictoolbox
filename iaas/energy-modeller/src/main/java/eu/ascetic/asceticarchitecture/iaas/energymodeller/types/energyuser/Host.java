/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * This class stores the basic data for physical machine. This represents a host
 * in the energy modeller.
 *
 * An important similar class is!
 *
 * @see eu.ascetic.monitoring.api.datamodel.host
 *
 * @author Richard
 */
public class Host extends EnergyUsageSource implements Comparable<Host> {

    private int id = -1;
    private String hostName = "";
    private boolean available = true;
    private ArrayList<HostEnergyCalibrationData> calibrationData = new ArrayList<>();

    /**
     * E_i^0: is the "idle power consumption" in Watts (with zero number of VMs
     * running) E_i^c: power consumption of a CPU cycle (or instruction)
     *
     * This value acts as the default idle power consumption for a host and is
     * used when no calibration data is available.
     *
     */
    private double defaultIdlePowerConsumption = 0.0; //i.e. 27.1w for an idle laptop.

    /**
     * An idea of power consumption scale:
     * http://www.xbitlabs.com/articles/memory/display/ddr3_13.html
     * http://superuser.com/questions/40113/does-installing-larger-ram-means-consuming-more-energy
     * http://www.tomshardware.com/reviews/power-saving-guide,1611-4.html
     */
    /**
     * This creates a new instance of a host
     *
     * @param id The host id
     * @param hostName The host name
     */
    public Host(int id, String hostName) {
        this.id = id;
        this.hostName = hostName;
    }

    /**
     * This returns the host's id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * This sets the host's id.
     *
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * This returns the host's name.
     *
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * This sets the hosts name.
     *
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * This indicates if the host is currently available.
     *
     * @return the available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * This sets the flag to state the host is available.
     *
     * @param available the available to set
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "HostID: " + id + " Host Name: " + hostName + " Available :" + available;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Host) {
            Host host = (Host) obj;
            if (hostName != null && host.getHostName() != null) {
                return this.hostName.equals(host.getHostName());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.hostName);
        return hash;
    }

    @Override
    public int compareTo(Host o) {
        return this.getHostName().compareTo(o.getHostName());
    }

    /**
     * This returns a list of all the calibration data that is held on the host.
     *
     * @return the calibration data of the host.
     */
    public ArrayList<HostEnergyCalibrationData> getCalibrationData() {
        return calibrationData;
    }

    /**
     * This allows the calibration data of a host to be set.
     *
     * @param calibrationData the calibrationData to set
     */
    public void setCalibrationData(ArrayList<HostEnergyCalibrationData> calibrationData) {
        this.calibrationData = calibrationData;
    }
    
    /**
     * This allows for an additional piece of calibration data to be set.
     * @param calibrationData 
     */
    public void addCalibrationData(HostEnergyCalibrationData calibrationData) {
        this.calibrationData.add(calibrationData);
    }  
    
    /**
     * This indicates if the energy modeller has calibration data for a given host.
     * @return true if calibration data exists for this host.
     */
    public boolean isCalibrated() {
        return !calibrationData.isEmpty();
    }    

    /**
     * This returns the lowest energy consumption found from the calibration
     * data.
     *
     * @return The idle power consumption of a host
     */
    public double getIdlePowerConsumption() {
        if (calibrationData.isEmpty()) {
            return defaultIdlePowerConsumption;
        }
        double answer = Double.MAX_VALUE;
        for (HostEnergyCalibrationData hostEnergyCalibrationData : calibrationData) {
            if (hostEnergyCalibrationData.getWattsUsed() < answer) {
                answer = hostEnergyCalibrationData.getWattsUsed();
            }            
        }
        return answer;
    }

    /**
     * This returns the value for the default idle power consumption of a host.
     * This value is recorded and used in cases where calibration has yet to
     * occur.
     *
     * @return the defaultIdlePowerConsumption
     */
    public double getDefaultIdlePowerConsumption() {
        return defaultIdlePowerConsumption;
    }

    /**
     * This sets the value for the default idle power consumption of a host.
     * This value is recorded and used in cases where calibration has yet to
     * occur.
     *
     * @param defaultIdlePowerConsumption the default Idle Power Consumption to
     * set
     */
    public void setDefaultIdlePowerConsumption(double defaultIdlePowerConsumption) {
        this.defaultIdlePowerConsumption = defaultIdlePowerConsumption;
    }
    
}
