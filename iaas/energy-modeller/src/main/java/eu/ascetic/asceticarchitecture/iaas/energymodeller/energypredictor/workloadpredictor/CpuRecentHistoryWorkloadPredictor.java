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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.workloadpredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlyEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This produces workload estimates for the purpose of providing better energy
 * estimations. It utilises a host's recent history in order to make an estimate
 * of what the future power consumption will be.
 *
 * @author Richard Kavanagh
 */
public class CpuRecentHistoryWorkloadPredictor extends AbstractWorkloadEstimator {

    private int cpuUtilObservationTimeMin = 15;
    private int cpuUtilObservationTimeSec = 0;
    private int cpuUtilObservationTimeSecTotal = 0;
    private static final String CONFIG_FILE = "energy-modeller-predictor.properties";

    /**
     * This sets up a CPU Recent History Workload Predictor. The main need is to
     * establish the time window by which the workload predictor must work.
     */
    public CpuRecentHistoryWorkloadPredictor() {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            readSettings(config);
        } catch (ConfigurationException ex) {
            Logger.getLogger(CpuOnlyEnergyPredictor.class.getName()).log(Level.SEVERE,
                    "Taking the default load from the settings file did not work", ex);
        }
    }

    /**
     * This sets up a CPU Recent History Workload Predictor. The main need is to
     * establish the time window by which the workload predictor must work.
     *
     * @param config The config to use in order to create the abstract energy
     * predictor.
     */
    public CpuRecentHistoryWorkloadPredictor(PropertiesConfiguration config) {
        readSettings(config);
    }
    
    /**
     * This takes the settings and reads them into memory and sets defaults
     * as needed.
     * @param config The settings to read.
     */
    private void readSettings(PropertiesConfiguration config) {
        cpuUtilObservationTimeMin = config.getInt("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min", cpuUtilObservationTimeMin);
        config.setProperty("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min", cpuUtilObservationTimeMin);
        cpuUtilObservationTimeSec = config.getInt("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec", cpuUtilObservationTimeSec);
        config.setProperty("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec", cpuUtilObservationTimeSec);
        cpuUtilObservationTimeSecTotal = cpuUtilObservationTimeSec + (int) TimeUnit.MINUTES.toSeconds(cpuUtilObservationTimeMin);        
    }

    /**
     * This provides an average of the recent CPU utilisation for a given host,
     * based upon the CPU utilisation time window set for the energy predictor.
     *
     * @param host The host for which the average CPU utilisation over the last
     * n seconds will be calculated for.
     * @param virtualMachines The set of virtual machines that are on the
     * physical host in question.
     * @return The average recent CPU utilisation based upon the energy
     * predictor's configured observation window.
     */
    @Override
    public double getCpuUtilisation(Host host, Collection<VM> virtualMachines) {
        return datasource.getCpuUtilisation(host, cpuUtilObservationTimeSecTotal);
    }

    @Override
    public boolean requiresVMInformation() {
        return false;
    }

}
