package eu.ascetic.energy.modeller.benchtest;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.WattsUpMeterDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of this class is to run the energy modeller in a standalone mode, in
 * order to get a readout of energy usage and predicted estimations side by
 * side.
 *
 */
public class Benchtest {

    private static final HostDataSource datasource = WattsUpMeterDataSourceAdaptor.getInstance();
    private static final DatabaseConnector database = new DefaultDatabaseConnector();
    private static final EnergyModeller modeller = new EnergyModeller(datasource, database);
    private static final Host host = modeller.getHost("localhost");
    private static final ArrayList<VM> vms = new ArrayList<>();

    public static void main(String[] args) {
        CurrentEnergyUsageLogger logger = new CurrentEnergyUsageLogger(new File("Dataset_current.txt"), false);
        new Thread(logger).start();
        EstimatedEnergyUsageLogger logger_est = new EstimatedEnergyUsageLogger(new File("Dataset_estimate.txt"), false);
        new Thread(logger_est).start();
        HistoricEnergyUsageLogger logger_hist = new HistoricEnergyUsageLogger(new File("Dataset_history.txt"), false);
        new Thread(logger_hist).start();
        
        long endTime = new GregorianCalendar().getTimeInMillis();
        endTime = endTime + TimeUnit.MINUTES.toMillis(3);
        
        while (new GregorianCalendar().getTimeInMillis() < endTime) {
            logger.printToFile(modeller.getCurrentEnergyForHost(host));
            logger_est.printToFile(modeller.getHostPredictedEnergy(host, vms));
            logger_hist.printToFile(modeller.getEnergyRecordForHost(host, null));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Benchtest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
