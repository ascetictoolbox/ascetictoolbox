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
package eu.ascetic.utils;

import eu.ascetic.utils.execution.Closeable;
import eu.ascetic.utils.execution.ManagedProcessSequenceExecutor;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.event.WattsUpDataAvailableEvent;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.event.WattsUpMemoryCleanEvent;
import wattsup.jsdk.core.event.WattsUpStopLoggingEvent;
import wattsup.jsdk.core.listener.WattsUpDataAvailableListener;
import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.listener.WattsUpMemoryCleanListener;
import wattsup.jsdk.core.listener.WattsUpStopLoggingListener;
import wattsup.jsdk.core.meter.WattsUp;

/**
 * This logs WattsUp meter data to disk in a continuous fashion.
 *
 */
public class LoggerWithFullTrace implements Closeable {

    private final WattsUp meter;
    private final File file = new File("Dataset.csv");
    private WattsUpLogger logger;
    private WorkloadLogger loggerWorkload;
    private MemoryWorkloadLogger loggerMemoryWorkload;
    private Sigar sigar;
    @SuppressWarnings("unused")
    private final ManagedProcessSequenceExecutor executor = new ManagedProcessSequenceExecutor(this);

    public static void main(String[] args) throws IOException {
        new LoggerWithFullTrace(args);
    }

    public LoggerWithFullTrace(String[] args) throws IOException {
        //Setup the Energy logger
        logger = new WattsUpLogger(file, false);
        Thread loggerThread = new Thread(logger);
        loggerThread.setDaemon(true);
        loggerThread.start();
        //Setup the CPU workload logger
        loggerWorkload = new WorkloadLogger(new File("Dataset_workload.csv"), false);
        Thread workloadLoggerThread = new Thread(loggerWorkload);
        workloadLoggerThread.setDaemon(true);
        new Thread(workloadLoggerThread).start();
        //Setup the memory workload logger
        loggerMemoryWorkload = new MemoryWorkloadLogger(new File("Dataset_memory_workload.csv"), false);
        Thread workloadMemoryLoggerThread = new Thread(loggerMemoryWorkload);
        workloadMemoryLoggerThread.setDaemon(true);
        new Thread(workloadMemoryLoggerThread).start();
        //setup sigar
        sigar = new Sigar();        
        //setup Wattsup
        String port = "COM9";
        if (args.length > 0) {
            port = args[0];
        }
        //Note: negative numbers for the schedule duration makes it run forever
        meter = new WattsUp(new WattsUpConfig().withPort(port).scheduleDuration(-1).withInternalLoggingInterval(1).withExternalLoggingInterval(1));
        System.out.println("WattsUp Meter Created");

        meter.registerListener(new WattsUpDataAvailableListener() {
            @Override
            public void processDataAvailable(final WattsUpDataAvailableEvent event) {
                WattsUpPacket[] values = event.getValue();
                logger.printToFile(values);
                try {
                    loggerWorkload.printToFile(sigar.getCpuPerc());
                    loggerMemoryWorkload.printToFile(sigar.getMem());
                } catch (SigarException ex) {
                    Logger.getLogger(LoggerWithFullTrace.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        meter.registerListener(new WattsUpMemoryCleanListener() {
            @Override
            public void processWattsUpReset(WattsUpMemoryCleanEvent event) {
                System.out.println("Memory Just Cleaned");
            }
        });

        meter.registerListener(new WattsUpStopLoggingListener() {
            @Override
            public void processStopLogging(WattsUpStopLoggingEvent event) {
                System.out.println("Logging Stopped");
                logger.stop();
            }
        });

        meter.registerListener(new WattsUpDisconnectListener() {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event) {
                System.out.println("Application Exiting Due to Disconnect");
                logger.stop();
            }
        });

        System.out.println("WattsUp Meter Connecting");
        meter.connect(true);
        meter.setLoggingModeSerial(1);
        System.out.println("WattsUp Meter Connected " + meter.isConnected());
    }

    @Override
    public void stop() {
        try {
            if (meter == null) {
                //The app hasn't started and is now exiting fast
                System.exit(0);
            } else {
                try {
                    /**
                     * This delays shutting everything down for 20 seconds, the
                     * aim is to get the tail end of the relevant data. Ensuring
                     * everything doesn't switch off straight after the last
                     * application called ends.
                     */
                    Thread.sleep(20000);

                } catch (InterruptedException ex) {
                    Logger.getLogger(LoggerWithFullTrace.class.getName()).log(Level.WARNING, "Actioner: InterruptedException", ex);
                }
                meter.stop();
                meter.disconnect();
            }
            logger.stop();
        } catch (IOException ex) {
            Logger.getLogger(LoggerWithFullTrace.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
