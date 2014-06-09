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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import java.util.Date;
import java.util.HashMap;

/**
 * This represents the dataset that was taken from a Watt meter.
 *
 * @author Richard
 */
public class MachineEnergyUsage {

    private String deviceID; //The id of the machine which the energy usage value belongs.
    private EnergyUsageSource sourceDevice; // A better reference to the device?
    private Date date; //Date the values recorded was taken.
    private double watts;
    private double volts;
    private double amps;
    private double powerRatio = 1.0;
    private HashMap<String, Double> additonalValues = null;

    /**
     * TODO Consider output from the Testbed! 
     * This is the output from the TUB testbed 
     * see: https://ascetic.cit.tu-berlin.de/www-data/meters.php
     *
     * Sample:
     *     
  "power": {
    "unit": "W",
    "asok11": {
      "value": 320.79399141631,
      "timestamp": 1400161015
    }
  },
  "energy": {
    "unit": "kWh",
    "asok11": {
      "value": 592.6842578125,
      "timestamp": 1400161015
    }
  },
  "voltage": {
    "unit": "V",
    "asok11": {
      "value": 228.7,
      "timestamp": 1400161015
    }
  },
  "timedrift": {
    "unit": "sec",
    "asok11": {
      "value": -2,
      "timestamp": 1400161015
    }
  }
}
     * 
     * The   "energy": { "unit": "kWh", Value will be counting from
     * an arbritrary and ill defined point in time. It would be best
     * to take a set start value for this, recording the time calling this
     * boot and then from there calculating energy used from that point on.
     * 
     * Power and voltage are immediate values, not representing a period of time.
     * Time drift as a value is not understood. Can it be ignored?
     * 
     */
    /**
     * Constructs a machine energy usage object.
     *
     * @param deviceID The unique identifier of the device.
     * @param date The date the value was taken.
     * @param watts The amount of Watts that was measured as being used by the
     * device.
     * @param volts The voltage that was measured.
     * @param amps The current that was measured.
     */
    public MachineEnergyUsage(String deviceID, Date date, double watts, double volts, double amps) {
        this.deviceID = deviceID;
        this.date = date;
        this.watts = watts;
        this.volts = volts;
        this.amps = amps;
    }

    /**
     * @return the deviceID
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * @param deviceID the deviceID to set
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the watts
     */
    public double getWatts() {
        return watts;
    }

    /**
     * @param watts the watts to set
     */
    public void setWatts(double watts) {
        this.watts = watts;
    }

    /**
     * @return the volts
     */
    public double getVolts() {
        return volts;
    }

    /**
     * @param volts the volts to set
     */
    public void setVolts(double volts) {
        this.volts = volts;
    }

    /**
     * @return the amps
     */
    public double getAmps() {
        return amps;
    }

    /**
     * @param amps the amps to set
     */
    public void setAmps(double amps) {
        this.amps = amps;
    }

    /**
     * @return the powerRatio
     */
    public double getPowerRatio() {
        return powerRatio;
    }

    /**
     * @param powerRatio the powerRatio to set
     */
    public void setPowerRatio(double powerRatio) {
        this.powerRatio = powerRatio;
    }

    /**
     * This is a generic store of additional KPI values
     *
     * @param kpi The name of the KPI to store.
     * @param value The value associated with the KPI value
     */
    public void add(String kpi, Double value) {
        if (additonalValues == null) {
            additonalValues = new HashMap<>();
        }
        additonalValues.put(kpi, value);
    }

    /**
     * This removes a KPI value from the set of additional values.
     *
     * @param kpi The name of the KPI to remove from the store.
     */
    public void remove(String kpi) {
        additonalValues.remove(kpi);
    }

    @Override
    public String toString() {
        return "Device: " + deviceID + " Date: " + date + " Power:" + watts + " Volts: " + volts + " Amps: " + amps;
    }
    
}
