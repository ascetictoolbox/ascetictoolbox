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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import eu.ascetic.ioutils.ResultsStore;
import eu.ascetic.ioutils.Settings;
import eu.ascetic.ioutils.execution.CompletedListener;
import eu.ascetic.ioutils.execution.ManagedProcess;
import eu.ascetic.ioutils.execution.ManagedProcessCommandData;
import eu.ascetic.ioutils.execution.ManagedProcessListener;
import eu.ascetic.ioutils.execution.ManagedProcessLogger;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of this class is to launch a set of managed processes one after
 * another at the specified time. During the launches of the application
 * calibration data will be taken and at the end of the experiment the data will
 * be written to the database.
 *
 * @author Richard Kavanagh
 */
public class CalibrationRunManager implements ManagedProcessListener {

    private static final String TO_EXECUTE_SETTINGS_FILE = "Apps.csv";
    private String workingDir;
    private final GregorianCalendar startTime = new GregorianCalendar();
    private final ResultsStore appsToExecute;
    private final ArrayList<ManagedProcessCommandData> commandSet = new ArrayList<>();
    private ManagedProcessLogger logger = null;
    private Actioner actioner = new Actioner();
    private int counter = 0;
    private final CompletedListener sender;
    private boolean logging = true;
    private final Host host;

    private final ArrayList<HostMeasurement> measurements = new ArrayList<>();
    private final HostDataSource datasource;
    private final DatabaseConnector database;
    private int calibratorWaitSec = 2;
    private boolean simulateCalibrationRun = false;
    private final Settings settings = new Settings("calibration_settings.properties");

    /**
     * Internal state variables ensuring that new values for measurements have
     * changed between measurement intervals
     */
    private long lastClock = 0;
    private double lastPowerValue = 0;

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public CalibrationRunManager(CompletedListener closeable, HostDataSource datasource, DatabaseConnector database, Host host) {

        workingDir = settings.getString("working_directory", ".");
        if (!workingDir.endsWith("/")) {
            workingDir = workingDir + "/";
        }
        appsToExecute = new ResultsStore(workingDir + TO_EXECUTE_SETTINGS_FILE);
        logging = settings.getBoolean("log_executions", true);
        calibratorWaitSec = settings.getInt("poll_interval", calibratorWaitSec);
        simulateCalibrationRun = settings.getBoolean("simulate_calibration_run", simulateCalibrationRun);
        if (settings.isChanged()) {
            settings.save("calibration_settings.properties");
        }
        writeOutDefaults(appsToExecute);
        this.sender = closeable;
        this.datasource = datasource;
        this.database = database;
        this.host = host;
        if (logging) {
            logger = new ManagedProcessLogger(new File(workingDir + "AppLog.csv"), false);
            Thread loggerThread = new Thread(logger);
            loggerThread.setDaemon(true);
            loggerThread.start();
        }
        Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "FILE LOCATION: {0}", new File(workingDir + TO_EXECUTE_SETTINGS_FILE).getAbsolutePath());
        getApplicationListFromFile(appsToExecute);
        new Thread(actioner).start();
    }

    /**
     * This performs a check to see if the settings file is empty or not. It
     * will write out a blank file if the file is not present and the attempt to
     * write to the database will be stopped.
     *
     * @param appsList The list of apps to execute
     * @return If the defaults settings have been written out to disk or not.
     */
    private boolean writeOutDefaults(ResultsStore appsList) {
        boolean answer = false;
        if (!new File(workingDir + TO_EXECUTE_SETTINGS_FILE).exists()) {
            appsList.add("Time From Start");
            appsList.append("Command");
            appsList.append("stdOut");
            appsList.append("stdError");
            appsList.append("Working Directory");
            appsList.append("Output To Screen");
            appsList.save();
            answer = true;
            if (logging) {
                logging = false;
                if (logger != null) {
                    logger.stop();
                }
            }
            sender.finished();
        }
        return answer;
    }

    /**
     * This loads in from file the list of applications to run
     * @param appsList The file on disk that has the list of applications to run.
     * @return The result store after having performed a load operation.
     */
    private ResultsStore getApplicationListFromFile(ResultsStore appsList) {
        appsList.load();
        //ignore the header of the file
        for (int i = 1; i < appsList.size(); i++) {
            ArrayList<String> current = appsList.getRow(i);
            ManagedProcessCommandData data = new ManagedProcessCommandData();
            //Set the start time
            data.setStartTime(Integer.parseInt(current.get(0)));
            GregorianCalendar actionStart = (GregorianCalendar) startTime.clone();
            actionStart.add(Calendar.SECOND, data.getStartTime());
            data.setActualStart(actionStart);

            data.setCommand(current.get(1));
            data.setStdOut(current.get(2));
            data.setStdError(current.get(3));
            data.setWorkingDirectory(new File(current.get(4)));
            data.setOutToScreen(Boolean.parseBoolean(current.get(5)));
            commandSet.add(data);
        }
        return appsList;
    }

    /**
     * Performs the execution of a command.
     *
     * @param cmd The command to execute.
     */
    private void execute(ManagedProcessCommandData cmd) {
        execute(cmd.getCommand(), cmd.getStdOut(), cmd.getStdError(), cmd.getWorkingDirectory(), cmd.isOutToScreen());
    }

    /**
     * This performs the execution of a given command and registers for the
     * commands completion event.
     *
     * @param command The command to execute
     * @param stdOut The directory for standard out
     * @param stdError The directory for standard error
     * @param workingDirectory The working directory of the application
     * @param toScreen If the output should also be logged to screen or not.
     */
    private void execute(String command, String stdOut, String stdError, File workingDirectory, boolean toScreen) {
        counter = counter + 1;
        ManagedProcess process;
        process = new ManagedProcess(command, stdOut, stdError, workingDirectory, toScreen);
        process.listen(this);
        if (logging) {
            logger.printToFile(process);//prints the starting version of a process
        }
    }

    @Override
    public void receiveProcessFinishedEvent(ManagedProcess process) {
        if (logging) {
            logger.printToFile(process); //prints the finished version of a process
        }
        counter--;
        Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "An action has just finished! Actions still running: {0}", counter);
        if (counter == 0 && actioner == null) {
            if (logging) {
                logger.stop();
            }
            sender.finished();
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "Process Exector: Exiting Thread");
        }
    }

    /**
     * This method aims to read the energy data for a host and convert it into
     * the calibration data required for the energy modeller.
     *
     * @param host The host to get the calibration data for
     * @return The updated host
     */
    private HostMeasurement readEnergyDataForHost(Host host) {

        try {
            HostMeasurement dataEntry = datasource.getHostData(host);
            long currentClock = dataEntry.getClock();
            double currentPower = dataEntry.getPower();
            /**
             * The next checks ensure, that at least one metric value has been
             * updated since the last poll interval. Plus it checks that the
             * power and CPU values are within close enough proximity to be
             * still useful. and finally it checks to make sure the dependant
             * and independent variables are within close enough to be still
             * representing the same time period.
             */
            if (currentClock > lastClock
                    && dataEntry.isContemporary(POWER_KPI_NAME,
                            dataEntry.getCpuUtilisationTimeStamp(), 3)
                    && absdifference(currentPower, lastPowerValue) < 0.1) {
                System.out.println("New Datapoint Generated!");
                return dataEntry;
            }
            lastClock = dataEntry.getClock();
            lastPowerValue = dataEntry.getPower();
        } catch (Exception ex) { //This should always try to gather data from the data source.
            Logger.getLogger(CalibrationRunManager.class.getName()).log(Level.SEVERE, "The calibrator's data logger had a problem.", ex);
        }
        System.out.println("Data Proximity Criteria Not Met");
        return null;
    }

    /**
     * This sets the host calibration data
     *
     * @param measurements The new measurements to convert to calibration data
     * @param host The host to perform the calibration for
     * @return The list of host calibration data values for the named host.
     */
    private ArrayList<HostEnergyCalibrationData> setHostCalibrationData(ArrayList<HostMeasurement> measurements, Host host) {
        ArrayList<HostEnergyCalibrationData> calibrationData = host.getCalibrationData();
        System.out.println("New Calibration Datapoints: " + measurements.size());
        //This gives the opportunity to run a load without saving the result
        if (simulateCalibrationRun == false) {
            calibrationData.addAll(HostEnergyCalibrationData.getCalibrationData(measurements));
            host.setCalibrationData(calibrationData);
            if (database != null && !measurements.isEmpty()) {
                database.setHostCalibrationData(host);
            }
        }
        return calibrationData;
    }

    /**
     * This returns the absolute difference between two values
     *
     * @param value1 The first value
     * @param value2 The second value
     * @return The absolute difference between the two values
     */
    private double absdifference(double value1, double value2) {
        return Math.max(value1, value2) - Math.min(value1, value2);
    }

    /**
     * The actioner looks at the current states event queue and waits to call an
     * action on a worker.
     */
    private class Actioner implements Runnable {

        private boolean stop = false;

        /**
         * This makes the actioner go through the action queue for actions to
         * perform.
         */
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "FILE LOCATION: {0}", new File(workingDir + TO_EXECUTE_SETTINGS_FILE).getAbsolutePath());
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "Actioner:Starting Thread");
            boolean justExecuted = false;
            while (!stop) {
                try {
                    if (commandSet.isEmpty() && counter == 0) {
                        //No more work is to be done thus can terminate.
                        stop = true;
                        continue;
                    }
                    Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: current State is not empty");
                    //An action is present check if it is to be fired yet!!

                    ManagedProcessCommandData head = null;
                    if (commandSet.size() > 0) {
                        head = commandSet.get(0);
                    }
                    Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: Head null status = {0}", (head != null));

                    if (head != null && new GregorianCalendar().after(head.getActualStart())) {
                        Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: Executing: The heads type was {0}", head.getClass());
                        commandSet.remove(0);
                        execute(head);
                        justExecuted = true;
                    }
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(calibratorWaitSec));

                    } catch (InterruptedException ex) {
                        Logger.getLogger(Actioner.class.getName()).log(Level.WARNING, "Actioner: InterruptedException", ex);
                        if (sender != null) {
                            sender.finished();
                        }
                    }
                    //Measurements should only be taken with an application runing
                    if (counter >= 1 && !justExecuted) {
                        //Take measurements if a process is currently running
                        HostMeasurement measurement = readEnergyDataForHost(host);
                        if (measurement != null) {
                            measurements.add(measurement);
                        }
                    }
                    justExecuted = false;
                } catch (Exception ex) { //outer most try statement
                    Logger.getLogger(Actioner.class.getName()).log(Level.SEVERE, "Actioner: Exception", ex);
                    if (sender != null) {
                        sender.finished();
                    }
                } //outer most try statement
            } //While not stopped
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "Actioner: Exiting Thread");
            if (sender != null) {
                sender.finished();
            }
            actioner = null;
            setHostCalibrationData(measurements, host);
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(20));
            } catch (InterruptedException ex) {
                Logger.getLogger(Actioner.class.getName()).log(Level.WARNING, "Waiting at the end for DB writes was interupted", ex);
            }
        }
    }

}
