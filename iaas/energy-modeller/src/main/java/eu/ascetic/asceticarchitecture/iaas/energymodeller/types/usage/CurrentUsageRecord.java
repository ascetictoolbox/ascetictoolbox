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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

/**
 * This stores a record of the current energy usage of either a VM or a
 * underlying resource.
 *
 * The values to be recorded are:
 *
 * Watts, current and voltage.
 *
 * @author Richard
 */
public class CurrentUsageRecord extends EnergyUsageRecord {

    private double power; //value given in Watts.
    private double current;
    private double voltage;
    private Calendar time;

    /**
     * This creates a record that indicates a current amount of energy in use.
     * @param energyUser either a VM, a host or a VM -> host mapping
     */
    public CurrentUsageRecord(EnergyUsageSource energyUser) {
        addEnergyUser(energyUser);
        time = new GregorianCalendar();
    }

    /**
     * This creates a record that indicates a current amount of energy in use.
     * @param energyUser either a VM, a host or a VM -> host mapping
     */
    public CurrentUsageRecord(HashSet<EnergyUsageSource> energyUser) {
        addEnergyUser(energyUser);
        time = new GregorianCalendar();
    }

    /**
     * This creates a record that indicates a current amount of energy in use.
     * @param energyUser either a VM, a host or a VM -> host mapping
     * @param power The power this energy user is consuming
     * @param current The current this energy user is using
     * @param voltage The voltage this energy user is using
     */
    public CurrentUsageRecord(EnergyUsageSource energyUser, double power, double current, double voltage) {
        addEnergyUser(energyUser);
        this.power = power;
        this.current = current;
        this.voltage = voltage;
        time = new GregorianCalendar();
    }    
    
    /**
     * This creates a record that indicates a current amount of energy in use.
     * @param energyUser either a VM, a host or a VM -> host mapping
     * @param power The power this energy user is consuming
     * @param current The current this energy user is using
     * @param voltage The voltage this energy user is using
     */
    public CurrentUsageRecord(HashSet<EnergyUsageSource> energyUser, double power, double current, double voltage) {
        addEnergyUser(energyUser);
        this.power = power;
        this.current = current;
        this.voltage = voltage;
        time = new GregorianCalendar();
    }

    /**
     * Gets the power that was in use.
     * @return the power
     */
    public double getPower() {
        return power;
    }

    /**
     * Sets the power that was in use.
     * @param power the power to set
     */
    public void setPower(double power) {
        this.power = power;
    }

    /**
     * Gets the current that was used.
     * @return the current
     */
    public double getCurrent() {
        return current;
    }

    /**
     * Sets the current that was used.
     * @param current the current to set
     */
    public void setCurrent(double current) {
        this.current = current;
    }

    /**
     * Gets the voltage that was used.
     * @return the voltage
     */
    public double getVoltage() {
        return voltage;
    }

    /**
     * Sets the voltage that was used.
     * @param voltage the voltage to set
     */
    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    /**
     * The returns the time that this usage record represents.
     * @return the time
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * This sets the time that this usage record represents.
     * @param time the time to set
     */
    public void setTime(Calendar time) {
        this.time = time;
    }

}
