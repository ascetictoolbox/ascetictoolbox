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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import java.util.ArrayList;
import java.util.List;

/**
 * The aim of this class is to store information regarding the energy usage of a
 * host at different levels of resource usage.
 *
 *
 * It is to be recorded as a 3-tuple record indicating the CPU usage, the memory
 * usage and the associate energy used for a given host.
 *
 * @see HostUsage These classes are identical. One may need to supersede the
 * other!
 * @author Richard
 */
public class HostEnergyCalibrationData {

    private double cpuUsage = 0.0; //as a pecentage of full load
    private double memoryUsage = 0.0; //in Megabytes
    private double wattsUsed = 0.0; //The power of the host (W), i.e. joules of energy used per second

    /**
     * This takes a list of host measurements and converts them into calibration
     * data.
     *
     * @param data The host measurements to use to generate the calibration
     * data.
     * @return A list of calibration data points.
     */
    public static List<HostEnergyCalibrationData> getCalibrationData(List<HostMeasurement> data) {
        List<HostEnergyCalibrationData> answer = new ArrayList<>();
        for (HostMeasurement hostMeasurement : data) {
            double power = hostMeasurement.getPower();
            double cpu = hostMeasurement.getCpuUtilisation();
            double memory = hostMeasurement.getMemoryUsed();
            double totalMemory = hostMeasurement.getMemoryTotal();
            HostEnergyCalibrationData newItem = new HostEnergyCalibrationData(cpu, memory / totalMemory, power);
            answer.add(newItem);
        }
        return answer;
    }

    /**
     * This creates a new record for storing the energy calibration data of a
     * host machine.
     */
    public HostEnergyCalibrationData() {
    }

    /**
     * This creates a new record for storing the energy calibration data of a
     * host machine.
     *
     * @param cpuUsage The cpu usage a percentage values 0.0 to 1 are expected.
     * @param memoryUsage The memory usage in Mb
     * @param wattsUsed The measured power consumption of the host (W i.e. j/s)
     */
    public HostEnergyCalibrationData(double cpuUsage, double memoryUsage, double wattsUsed) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.wattsUsed = wattsUsed;
    }

    /**
     * This provides a value for the CPU usage.
     *
     * @return The CPU usage as a percentage with values between 0.0 to 1.
     */
    public double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * This sets a value for the CPU usage, for this calibration record.
     *
     * @param cpuUsage The CPU usage as a percentage with values between 0.0 to
     * 1.
     */
    public void setCpuUsage(double cpuUsage) {
        if (cpuUsage >= 0.0 && cpuUsage <= 1.0) {
            this.cpuUsage = cpuUsage;
        }
    }

    /**
     * This returns the memory usage
     *
     * @return the memoryUsage The memory usage in Mb
     */
    public double getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * This sets the memory usage of this calibration record.
     *
     * @param memoryUsage the memoryUsage The memory usage in Mb
     */
    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    /**
     * The returns the measured power consumption of this calibration record (W
     * i.e. j/s)
     *
     * @return the wattsUsed The measured power consumption of the host (W i.e.
     * j/s)
     */
    public double getWattsUsed() {
        return wattsUsed;
    }

    /**
     * This sets a value for the power usage, for this calibration record.
     *
     * @param wattsUsed The measured power consumption of the host (W i.e. j/s)
     */
    public void setWattsUsed(double wattsUsed) {
        this.wattsUsed = wattsUsed;
    }

}
