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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import java.io.File;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This predictor uses a simple reading of the current power value and forecasts 
 * that no change will occur. The energy cost for a host or VM is then calculated
 * from this reading.
 *
 * @author Richard Kavanagh
 */
public class AveragePowerEnergyPredictor extends AbstractEnergyPredictor {

    private int powerObservationTimeMin = 15;
    private int powerObservationTimeSec = 0;
    private int observationTime = 0;

    /**
     * This creates a new average power energy predictor. The predictor when
     * running takes the last power reading and makes the assumption no change
     * will occur. An observation time window is used for taking the measurement,
     * which is set via a configuration file.
     */
    public AveragePowerEnergyPredictor() {
        try {
            if (database == null) {
                database = new DefaultDatabaseConnector();
            }
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {

                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            powerObservationTimeMin = config.getInt("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min", powerObservationTimeMin);
            config.setProperty("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min", powerObservationTimeMin);
            powerObservationTimeSec = config.getInt("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec", powerObservationTimeSec);
            config.setProperty("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec", powerObservationTimeSec);
            observationTime = powerObservationTimeSec + (int) TimeUnit.MINUTES.toSeconds(powerObservationTimeMin);
        } catch (ConfigurationException ex) {
            Logger.getLogger(AveragePowerEnergyPredictor.class.getName()).log(Level.SEVERE, "The average power energy predictor failed to initialise", ex);
        }
    }

    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines, TimePeriod duration) {
        return predictTotalEnergy(host, duration);
    }

    /**
     * This provides a prediction of how much energy is to be used by a VM
     *
     * @param vm The vm to be deployed
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @param host The host that the VMs will be running on
     * @param timePeriod The time period the query should run for.
     * @return The prediction of the energy to be used.
     */
    @Override
   public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host, TimePeriod timePeriod) {
        EnergyDivision division = getEnergyUsage(host, virtualMachines);
        EnergyUsagePrediction hostAnswer = predictTotalEnergy(host, timePeriod);
        EnergyUsagePrediction generalHostsAnswer = getGeneralHostPredictedEnergy(timePeriod);
        double generalPower = generalHostsAnswer.getAvgPowerUsed() / (double) virtualMachines.size();
        double generalEnergy = generalHostsAnswer.getTotalEnergyUsed() / (double) virtualMachines.size();
        hostAnswer.setAvgPowerUsed(hostAnswer.getTotalEnergyUsed()
                / ((double) TimeUnit.SECONDS.toHours(timePeriod.getDuration())));
        EnergyUsagePrediction answer = new EnergyUsagePrediction(vm);
        answer.setDuration(hostAnswer.getDuration());
        //Find the fraction to be associated with the VM
        double vmsEnergyFraction = division.getEnergyUsage(hostAnswer.getTotalEnergyUsed(), vm);
        division.setConsiderIdleEnergy(isConsiderIdleEnergy());
        answer.setTotalEnergyUsed(vmsEnergyFraction + generalEnergy);
        double vmsPowerFraction = division.getEnergyUsage(hostAnswer.getAvgPowerUsed(), vm);
        answer.setAvgPowerUsed(vmsPowerFraction + generalPower);
        return answer;
    }

    @Override
    public double predictPowerUsed(Host host) {
        return getAverageHostPower(host, observationTime);
    }

    /**
     * This looks at historic information and gets the average power
     * consumption.
     *
     * @param host The host to get the average power for
     * @param duration The time in seconds before now to get the average for
     * @return The average power of the host
     */
    private double getAverageHostPower(Host host, long duration) {
        double answer = 0;
        double count = 0;
        /**
         * The period of time is so short so average measured power values.
         * Not considering individual time periods, that each measurement lasted
         * for. i.e. assume a regular arrival rate.
         */
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(cal.getTimeInMillis() - TimeUnit.SECONDS.toMillis(duration));
        TimePeriod timePeriod = new TimePeriod(cal, duration);
        List<HostEnergyRecord> data = database.getHostHistoryData(host, timePeriod);
            for (int i = 0; i <= data.size() - 2; i++) {
                HostEnergyRecord power = data.get(i);
                answer = answer + power.getPower();
                count = count + 1;
            }
        return answer / count;
    }

    @Override
    public double predictPowerUsed(Host host, double usageCPU) {
        return predictPowerUsed(host);
    }

    /**
     * This predicts the total amount of energy used by a host.
     *
     * @param host The host to get the energy prediction for
     * @param timePeriod The time period the prediction is for
     * @return The predicted energy usage.
     */
    private EnergyUsagePrediction predictTotalEnergy(Host host, TimePeriod timePeriod) {
        EnergyUsagePrediction answer = new EnergyUsagePrediction(host);
        double powerUsed = predictPowerUsed(host);
        answer.setAvgPowerUsed(powerUsed);
        answer.setTotalEnergyUsed(powerUsed * ((double) TimeUnit.SECONDS.toHours(timePeriod.getDuration())));
        answer.setDuration(timePeriod);
        return answer;
    }

    @Override
    public double getSumOfSquareError(Host host) {
        return Double.MAX_VALUE;
    }

    @Override
    public double getRootMeanSquareError(Host host) {
        return Double.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "Average current power energy predictor";
    }

    @Override
    public void printFitInformation(Host host) {
        System.out.println("This model uses an assumption and no fit data is available");
    }

}
