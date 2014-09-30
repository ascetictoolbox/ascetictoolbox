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
package eu.ascetic.energy.modeller.benchtest;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.WattsUpMeterDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.ioutils.execution.CompletedListener;
import eu.ascetic.ioutils.execution.ManagedProcessSequenceExecutor;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * The aim of this class is to run the energy modeller in a standalone mode, in
 * order to get a readout of energy usage and predicted estimations side by
 * side.
 *
 */
public class Benchtest {

    private static HostDataSource datasource = WattsUpMeterDataSourceAdaptor.getInstance();
    private static DatabaseConnector database = new DefaultDatabaseConnector();
    private static EnergyModeller modeller = new EnergyModeller(datasource, database);
    private static final Host HOST = modeller.getHost("localhost");
    private static final ArrayList<VM> VMS = new ArrayList<>();

    /**
     * This runs the main benchmarking tool. It runs for 5 minutes, logging data
     * regarding the experiment.
     * @param args The first argument is the running time of the experiment.
     * @throws SigarException Thrown if the CPU monitoring fails.
     */
    public static void main(String[] args) throws SigarException {
        int duration = 5;
        if (args.length > 0) {
            duration = Integer.valueOf(args[0]);
        }
        DummyCompletedListener completedListener = new DummyCompletedListener();
        ManagedProcessSequenceExecutor executor = new ManagedProcessSequenceExecutor(completedListener);
        CurrentEnergyUsageLogger loggerCurrent = new CurrentEnergyUsageLogger(new File("Dataset_current.csv"), false);
        new Thread(loggerCurrent).start();
        EstimatedEnergyUsageLogger loggerEstimate = new EstimatedEnergyUsageLogger(new File("Dataset_estimate.csv"), false);
        new Thread(loggerEstimate).start();
        HistoricEnergyUsageLogger loggerHistory = new HistoricEnergyUsageLogger(new File("Dataset_history.csv"), false);
        new Thread(loggerHistory).start();
        WorkloadLogger loggerWorkload = new WorkloadLogger(new File("Dataset_workload.csv"), false);
        new Thread(loggerWorkload).start();

        long endTime = new GregorianCalendar().getTimeInMillis();
        endTime = endTime + TimeUnit.MINUTES.toMillis(duration);
        Sigar sigar = new Sigar();

        while (new GregorianCalendar().getTimeInMillis() < endTime) {
            loggerWorkload.printToFile(sigar.getCpuPerc());
            loggerCurrent.printToFile(modeller.getCurrentEnergyForHost(HOST));
            loggerEstimate.printToFile(modeller.getHostPredictedEnergy(HOST, VMS));
            loggerHistory.printToFile(modeller.getEnergyRecordForHost(HOST, null));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Benchtest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //This stops the benchmarking/testing suite from running.
        loggerWorkload.stop();
        loggerCurrent.stop();
        loggerEstimate.stop();
        loggerHistory.stop();
        modeller.stop();
    } 
    
    /**
     * The aim of this class is to consume the workload generators completion 
     * signal and to ignore it.
     */
    private static class DummyCompletedListener implements CompletedListener {

        @Override
        public void finished() {
        }
        
    }
}
