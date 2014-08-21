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
import eu.ascetic.ioutils.execution.ManagedProcessSequenceExecutor;
import eu.ascetic.ioutils.execution.CompletedListener;
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
    private static final Host host = modeller.getHost("localhost");
    private static final ArrayList<VM> vms = new ArrayList<>();

    public static void main(String[] args) throws SigarException {
        DummyCompletedListener completedListener = new DummyCompletedListener();
        ManagedProcessSequenceExecutor executor = new ManagedProcessSequenceExecutor(completedListener);
        CurrentEnergyUsageLogger logger = new CurrentEnergyUsageLogger(new File("Dataset_current.csv"), false);
        new Thread(logger).start();
        EstimatedEnergyUsageLogger logger_est = new EstimatedEnergyUsageLogger(new File("Dataset_estimate.csv"), false);
        new Thread(logger_est).start();
        HistoricEnergyUsageLogger logger_hist = new HistoricEnergyUsageLogger(new File("Dataset_history.csv"), false);
        new Thread(logger_hist).start();
        WorkloadLogger logger_workload = new WorkloadLogger(new File("Dataset_workload.csv"), false);
        new Thread(logger_workload).start();

        long endTime = new GregorianCalendar().getTimeInMillis();
        endTime = endTime + TimeUnit.MINUTES.toMillis(5);
        Sigar sigar = new Sigar();

        while (new GregorianCalendar().getTimeInMillis() < endTime) {
            logger_workload.printToFile(sigar.getCpuPerc());
            logger.printToFile(modeller.getCurrentEnergyForHost(host));
            logger_est.printToFile(modeller.getHostPredictedEnergy(host, vms));
            logger_hist.printToFile(modeller.getEnergyRecordForHost(host, null));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Benchtest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //This stops the benchmarking/testing suite from running.
        logger_workload.stop();
        logger.stop();
        logger_est.stop();
        logger_hist.stop();
        modeller = null;
        datasource = null;
        database = null;
    } 
    
    private static class DummyCompletedListener implements CompletedListener {

        @Override
        public void finished() {
        }
        
    }
}
