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
package eu.ascetic.iaas.energy.modeller.standalone.calibration.tool;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.WattsUpMeterDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostProfileData;
import eu.ascetic.ioutils.execution.CompletedListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnt.scimark2.Benchmark;
import jnt.scimark2.Result;

/**
 * The aim of this is to induce load on the local host and measure the response.
 * The results will then be written into the Energy Modellers database
 *
 * @author Richard Kavanagh
 */
public class StandaloneCalibrationTool implements CompletedListener {

    private boolean working = false;
    private Host host;
    private boolean stopOnCalibratedHosts = false;
    private HostDataSource source;
    private final DatabaseConnector database = new DefaultDatabaseConnector();
    private CalibrationRunManager runManager;
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";

    /**
     * Creates a calibration tool for the ASECTiC energy modeller
     *
     * @param hostname The host to calibrate
     */
    public StandaloneCalibrationTool(String hostname) {
        source = new ZabbixDirectDbDataSourceAdaptor();
        host = source.getHostByName(hostname);
        host = database.getHostCalibrationData(host);
    }

    /**
     * Creates a calibration tool for the ASECTiC energy modeller
     *
     * @param hostname The host to calibrate
     * @param datasource This allows the data source to be changed to a named
     * other from the default of ZabbixDirectDBDatasourceAdaptor.
     */
    public StandaloneCalibrationTool(String hostname, String datasource) {
        setDataSource(datasource);
        host = source.getHostByName(hostname);
        host = database.getHostCalibrationData(host);
    }

    /**
     * This performs the calibration of a host.
     *
     * @param args The first argument should be the host name after this there
     * are several optional arguments can be passed namely: 
     * halt-on-calibrated which prevents a host from been re-calibrated. 
     * benchmark-only which prevents calibration from running. and
     * use-watts-up-meter which means a watts up meter is used locally for 
     * measurements.
     *
     */
    public static void main(String[] args) {
        ArrayList<String> strArgs = new ArrayList<>(Arrays.asList(args));
        StandaloneCalibrationTool instance;
        if (args.length != 0) {
            if (strArgs.contains("use-watts-up-meter")) {
                instance = new StandaloneCalibrationTool(args[0],
                        DEFAULT_DATA_SOURCE_PACKAGE + ".WattsUpMeterDataSourceAdaptor");
            } else {
                instance = new StandaloneCalibrationTool(args[0]);
            }
            /**
             * Induce the training workload pattern
             */
            if (strArgs.contains("halt-on-calibrated")) {
                instance.setHaltOnCalibratedHost(true);
            }
            instance.performBenchmark();
            if (!strArgs.contains("benchmark-only")) {
                instance.induceLoad();
            }
        } else {
            System.out.println("Please provide the name of the host!");
            System.out.println("Usage: host-name [halt-on-calibrated] [benchmark-only]");
            System.out.println("The halt-on-calibrated flag will prevent calibration "
                    + "in cases where the data has already been gathered.");
            System.out.println("The benchmark-only flag skips the calibration run "
                    + "and performs a benchmark run only.");
            System.out.println("The use-watts-up-meter flag can be used so that "
                    + "Zabbix is not used for calibration but local measurements "
                    + "are performed instead.");
        }
    }

    /**
     * This allows the data source to be set
     *
     * @param dataSource The name of the data source to use for the calibration
     */
    public final void setDataSource(String dataSource) {
        try {
            if (!dataSource.startsWith(DEFAULT_DATA_SOURCE_PACKAGE)) {
                dataSource = DEFAULT_DATA_SOURCE_PACKAGE + "." + dataSource;
            }
            /**
             * This is a special case that requires it to be loaded under the
             * singleton design pattern.
             */
            String wattsUpMeter = DEFAULT_DATA_SOURCE_PACKAGE + ".WattsUpMeterDataSourceAdaptor";
            if (wattsUpMeter.equals(dataSource)) {
                source = WattsUpMeterDataSourceAdaptor.getInstance();
            } else {
                source = (HostDataSource) (Class.forName(dataSource).newInstance());
            }
        } catch (ClassNotFoundException ex) {
            if (source == null) {
                source = new ZabbixDirectDbDataSourceAdaptor();
            }
            Logger.getLogger(StandaloneCalibrationTool.class.getName()).log(Level.WARNING, "The data source specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (source == null) {
                source = new ZabbixDirectDbDataSourceAdaptor();
            }
            Logger.getLogger(StandaloneCalibrationTool.class.getName()).log(Level.WARNING, "The data source did not work", ex);
        }
    }

    /**
     * This performs a basic benchmark that can be used to determine how fast a
     * host is relative to others. It then can be used in calculating
     * performance per watt.
     */
    public void performBenchmark() {
        /**
         * Check to see if the host is calibrated. If it is then exit, unless
         * the calibration is been forced through anyway.
         */
        if (host.isCalibrated() && stopOnCalibratedHosts) {
            System.out.println("Exiting due to being already calibrated");
            System.exit(0);
        }
        Benchmark benchmark = new Benchmark();
        Result result = benchmark.getBenchmarkResult();
        host.addProfileData(new HostProfileData("flops", result.getCompositeScore()));
        database.setHostProfileData(host);
    }

    /**
     * This induces load on the physical host
     *
     * @return If the executor has finished or not.
     */
    public boolean induceLoad() {
        /**
         * Check to see if the host is calibrated. If it is then exit, unless
         * the calibration is been forced through anyway.
         */
        if (host.isCalibrated() && stopOnCalibratedHosts) {
            System.out.println("Exiting due to being already calibrated");
            System.exit(0);
        }

        if (!working) {
            runManager = new CalibrationRunManager(this, source, database, host);
            working = true;
        }
        return working;
    }

    /**
     * This indicates if it has finished inducing load on the web server
     *
     * @return If the executor has finished or not.
     */
    public boolean currentlyWorking() {
        return working;
    }

    @Override
    public void finished() {
        working = false;
    }

    /**
     * This allows the calibration of a physical host to be stopped in cases
     * where the calibration data has already been gathered.
     *
     * @param stopOnCalibratedHosts If true this stops the calibrator from
     * running on an already calibrated host. The default is to run anyway as a
     * calibration run should merely gather more data which has the potential to
     * improve the calibration of a physical host.
     */
    public void setHaltOnCalibratedHost(boolean stopOnCalibratedHosts) {
        this.stopOnCalibratedHosts = !stopOnCalibratedHosts;
    }

}
