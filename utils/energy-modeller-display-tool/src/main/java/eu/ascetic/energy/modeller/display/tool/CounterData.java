/**
 * Copyright 2015 University of Leeds
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
package eu.ascetic.energy.modeller.display.tool;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import java.text.DecimalFormat;

/**
 * This data collector displays energy data for the IaaS Energy Modeller.
 *
 * @author Richard Kavanagh
 */
public class CounterData implements Comparable<CounterData> {

    private final EnergyUsageSource energyUser;
    private long startTime = -1;
    private long lastTime = -1;
    private double lastPower = -1;
    private double energy = 0;
    private boolean emulated = false;

    public CounterData(EnergyUsageSource energyUser) {
        this.energyUser = energyUser;
    }

    /**
     * This gets the name of the VM or physical host
     *
     * @return
     */
    public String getName() {
        if (this.energyUser.getClass().equals(Host.class)) {
            return ((Host) energyUser).getHostName();
        } else {
            return ((VmDeployed) energyUser).getName();
        }
    }

    /**
     * Indicates if this counter data is for a physical host or not
     *
     * @return
     */
    public boolean isHost() {
        return this.energyUser.getClass().equals(Host.class);
    }

    /**
     * This adds another value to the counter
     *
     * @param time
     * @param power
     */
    public void add(long time, double power) {
        if (lastTime != -1) {
            double deltaTime = time - lastTime;
            double avgPower = lastPower + power / 2;
            energy = energy + (deltaTime * avgPower);
        } else {
            startTime = time;
        }
        lastTime = time;
        lastPower = power;
    }

    public double getAveragePower() {
        double deltaTime = lastTime - startTime;
        if (energy != -1) {
            return energy / deltaTime;
        }
        return 0;
    }

    public double getEnergy() {
        return energy;
    }

    /**
     * This resets this energy counter back to zero
     */
    public void resetCounter() {
        lastTime = -1;
        lastPower = -1;
        startTime = -1;
        energy = 0;
        emulated = false;
    }

    @Override
    public String toString() {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        return (isHost() ? "HOST: " : "VM: ") + getName() + " Energy (J): " + formatter.format(energy) + " Average Power (W): " + formatter.format(getAveragePower()) + (isEmulated() ? " Emulated" : "");
    }

    @Override
    public int compareTo(CounterData data) {
        return this.getName().compareTo(data.getName());
    }

    /**
     * This indicates if an emulated watt meter value was used.
     * @return the emulated
     */
    public boolean isEmulated() {
        return emulated;
    }

    /**
     * This is used to indicate if an emulated watt meter value was used.
     * @param emulated If emulation has been used or not
     */
    public void setEmulated(boolean emulated) {
        this.emulated = emulated;
    }

}
