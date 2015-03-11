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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.ioutils.execution.CompletedListener;
import java.util.ArrayList;
import java.util.Arrays;

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
        StandaloneCalibrationTool instance;
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
            ArrayList<String> strArgs = new ArrayList<>(Arrays.asList(args));
            if (strArgs.contains("halt-on-calibrated")) {
                instance.setHaltOnCalibratedHost(true);
            }
            instance.induceLoad();
        } else {
            System.out.println("Please provide the name of the host!");
            System.out.println("Usage: host-name [halt-on-calibrated]");
            System.out.println("The halt-on-calibrated flag will prevent calibration"
                    + "in cases where the data has already been gathered.");
        }
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
