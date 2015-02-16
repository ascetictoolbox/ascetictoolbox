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
package eu.ascetic.iaas.energy.modeller.standalone.calibration.tool;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.ioutils.execution.CompletedListener;

/**
 * The aim of this is to induce load on the local host and measure the response.
 * The results will then be written into the Energy Modellers database
 *
 * @author Richard
 */
public class StandaloneCalibrationTool implements CompletedListener {

    private boolean working = false;
    private Host host;
    private boolean forceCalibration = true;
    private final HostDataSource source = new ZabbixDirectDbDataSourceAdaptor();
    private final DatabaseConnector database = new DefaultDatabaseConnector();
    private CalibrationRunManager runManager;

    /**
     * Creates a calibration tool for the ASECTiC energy modeller
     *
     * @param hostname The host to calibrate
     */
    public StandaloneCalibrationTool(String hostname) {
        host = source.getHostByName(hostname);
        host = database.getHostCalibrationData(host);
    }

    public static void main(String[] args) {
        StandaloneCalibrationTool instance = null;
        if (args.length != 0) {
            instance = new StandaloneCalibrationTool(args[0]);
            /**
             * Wait for a short time, make sure the host is in an idle enough
             * state. Noting this application may be part of the boot sequence.
             */
            //TODO see if a start delay is relevant
            /**
             * Induce the training workload pattern
             */
            instance.induceLoad();
        } else {
            System.out.println("Please provide the name of the host!");
        }
    }

    /**
     * This induces load on the web server
     *
     * @return If the executor has finished or not.
     */
    public boolean induceLoad() {
        /**
         * Check to see if the host is calibrated. If it is then exit, unless
         * the calibration is been forced through anyway.
         */
        if (host.isCalibrated() && !forceCalibration) {
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

}
