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
package eu.ascetic.ioutils.execution;

import eu.ascetic.ioutils.ResultsStore;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of this class is to launch a set of managed processes one after
 * another at the specified time. The launch times are then logged ready for
 * further analysis.
 *
 * @author Richard Kavanagh
 */
public class ManagedProcessSequenceExecutor implements ManagedProcessListener {

    private static final String TO_EXECUTE_SETTINGS_FILE = "Apps.csv";
    private final String workingDir;
    private final GregorianCalendar startTime = new GregorianCalendar();
    ResultsStore appsToExecute;
    ArrayList<ManagedProcessCommandData> commandSet = new ArrayList<>();
    private ManagedProcessLogger logger = null;
    private Actioner actioner = new Actioner();
    private int counter = 0;
    private final CompletedListener sender;
    private boolean logging = true;

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public ManagedProcessSequenceExecutor(CompletedListener closeable) {
        workingDir = System.getProperty("ascetic.working.directory", "");
        appsToExecute = new ResultsStore(workingDir + TO_EXECUTE_SETTINGS_FILE);
        logging = Boolean.getBoolean(System.getProperty("ascetic.log.executions", "true"));
        this.sender = closeable;
        if (logging) {
            logger = new ManagedProcessLogger(new File(workingDir + "AppLog.csv"), false);
            Thread loggerThread = new Thread(logger);
            loggerThread.setDaemon(true);
            loggerThread.start();
        }
        Logger.getLogger(ManagedProcessSequenceExecutor.class.getName()).log(Level.INFO, "FILE LOCATION: {0}", new File(workingDir));
        if (!new File(workingDir + TO_EXECUTE_SETTINGS_FILE).exists()) {
            appsToExecute.add("Time From Start");
            appsToExecute.append("Command");
            appsToExecute.append("stdOut");
            appsToExecute.append("stdError");
            appsToExecute.append("Working Directory");
            appsToExecute.append("Output To Screen");
            appsToExecute.save();
            if (logging) {
                logger.stop();
            }
            sender.finished();
        } else {
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "FILE LOCATION: {0}", new File(workingDir + TO_EXECUTE_SETTINGS_FILE).getAbsolutePath());
            appsToExecute.load();
        }
        //ignore the header of the file
        for (int i = 1; i < appsToExecute.size(); i++) {
            ArrayList<String> current = appsToExecute.getRow(i);
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
        new Thread(actioner).start();
    }

    private void execute(ManagedProcessCommandData data) {
        execute(data.getCommand(), data.getStdOut(), data.getStdError(), data.getWorkingDirectory(), data.isOutToScreen());
    }

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
            while (!stop) {
                try {
                    if (commandSet.isEmpty()) {
                        //No more work is to be done thus can terminate.
                        stop = true;
                        continue;
                    }
                    Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: current State is not empty");
                    //An action is present check if it is to be fired yet!!
                    ManagedProcessCommandData head = commandSet.get(0);
                    Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: Head null status = {0}", (head != null));
                    if ((new GregorianCalendar().after(head.getActualStart()))) {
                        Logger.getLogger(Actioner.class.getName()).log(Level.FINE, "Actioner: Executing: The heads type was {0}", head.getClass());
                        commandSet.remove(0);
                        execute(head);
                    } else { //end of (head not null) and has either no time or is ready to execute
                        try {
                            /*
                             * wait until the next action should be fired. If an
                             * action has been fired there is no waiting for the
                             * next action!
                             */
                            Thread.sleep(1000);

                        } catch (InterruptedException ex) {
                            Logger.getLogger(Actioner.class.getName()).log(Level.WARNING, "Actioner: InterruptedException", ex);
                            if (sender != null) {
                                sender.finished();
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Actioner.class.getName()).log(Level.SEVERE, "Actioner: Exception", ex);
                    if (sender != null) {
                        sender.finished();
                    }
                }
            }
            Logger.getLogger(Actioner.class.getName()).log(Level.INFO, "Actioner: Exiting Thread");
            if (sender != null) {
                sender.finished();
            }
            actioner = null;
        }
    }

}
