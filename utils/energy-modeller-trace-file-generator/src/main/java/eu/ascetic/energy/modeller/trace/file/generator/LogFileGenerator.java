package eu.ascetic.energy.modeller.trace.file.generator;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;

/**
 * This performs analysis on the energy modeller's stored dataset and outputs all
 * information regarding hosts and their VMs to disk.
 */
public class LogFileGenerator 
{
    
    private static final ZabbixDataSourceAdaptor dataSource = new ZabbixDataSourceAdaptor();
    private static final DataCollector collector = new DataCollector(dataSource, new DefaultDatabaseConnector());    
    
    public static void main( String[] args )
    {
        collector.gatherData();
    }
}
