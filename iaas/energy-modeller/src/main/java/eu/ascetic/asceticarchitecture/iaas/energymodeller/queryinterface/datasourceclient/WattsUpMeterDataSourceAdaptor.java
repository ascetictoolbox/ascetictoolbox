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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.ioutils.Settings;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
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
 * The aim of this class is initially to take data from the Watts Up meter and
 * to place it into a format that is suitable for the Energy modeller.
 *
 * @author Richard Kavanagh
 */
public class WattsUpMeterDataSourceAdaptor implements HostDataSource {

    private static final String VOLTAGE_KPI_NAME = "Voltage";
    private static final String CURRENT_KPI_NAME = "Current";
    private final Host host;
    private WattsUp meter;
    private WattsUpTailer fileTailer;
    private Tailer tailer;
    private static Sigar sigar = new Sigar();
    private HostMeasurement lowest = null;
    private HostMeasurement highest = null;
    private HostMeasurement current = null;
    private final LinkedList<CPUUtilisation> cpuMeasure = new LinkedList<>();
    private final Settings settings = new Settings("energy-modeller-watts-up-meter.properties");

    /**
     * SingletonHolder is loaded on the first execution of
     * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
     * not before.
     */
    private static class SingletonHolder {

        private static final WattsUpMeterDataSourceAdaptor INSTANCE = new WattsUpMeterDataSourceAdaptor();
    }

    /**
     * This creates a new instance of the WattsUp Meter data source adaptor.
     * This adaptor is intended for using the energy modeller on a local
     * machine.
     *
     * @return A singleton instance of a WattsUp? meter data source adaptor.
     */
    public static WattsUpMeterDataSourceAdaptor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * This creates a new WattsUp Meter data source adaptor, that is capable of
     * taking data from a WattsUp Meter, for use inside the ASCETiC
     * architecture.
     *
     */
    private WattsUpMeterDataSourceAdaptor() {
        String port = settings.getString("iaas.energy.modeller.wattsup.port", "COM9");
        int hostId = settings.getInt("iaas.energy.modeller.wattsup.hostId", 1);
        String hostname = settings.getString("iaas.energy.modeller.wattsup.hostname", "localhost");
        if (settings.isChanged()) {
            settings.save("energy-modeller-watts-up-meter.properties");
        }
        host = new Host(hostId, hostname);
        startup(port, -1, 1);
        try {
            Mem mem = sigar.getMem();
            host.setRamMb((int) (Double.valueOf(mem.getTotal()) / 1048576));
        } catch (SigarException ex) {
            Logger.getLogger(WattsUpMeterDataSourceAdaptor.class.getName()).log(Level.SEVERE,
                    "A problem occured with Sigar", ex);
        }

    }

    /**
     * This creates a new WattsUp Meter data source adaptor, that is capable of
     * taking data from a WattsUp Meter, for use inside the ASCETiC
     * architecture.
     *
     * @param port The port to connect to
     * @param duration The duration to connect for (< 0 means forever) 
     * @param interval The interval at which to take logging data.
     */
    public WattsUpMeterDataSourceAdaptor(String port, int duration, int interval) {
        host = new Host(1, "localhost");
        startup(port, duration, interval);
    }

    /**
     * This is the generic startup code for the WattsUp data source adaptor
     *
     * @param port The port to connect to
     * @param duration The duration to connect for (< 0 means forever) 
     * @param interval The interval at which to take logging data.
     */
    public final void startup(String port, int duration, int interval) {
        if (port.equalsIgnoreCase("file")) {
            String filename = settings.getString("iaas.energy.modeller.wattsup.scrape.file", "testnodex-wattsup.log");
            if (settings.isChanged()) {
                settings.save("energy-modeller-watts-up-meter.properties");
            }
            File scrapeFile = new File(filename);
            fileTailer = new WattsUpTailer();
            tailer = Tailer.create(scrapeFile, fileTailer, (interval * 1000) / 16, true);
            Thread tailerThread = new Thread(tailer);
            tailerThread.setDaemon(true);
            tailerThread.start();
            System.out.println("Scraping from WattsUp meter file");
        } else {
            try {
                WattsUpConfig config = new WattsUpConfig().withPort(port).scheduleDuration(duration).withInternalLoggingInterval(interval).withExternalLoggingInterval(interval);
                meter = new WattsUp(config);
                System.out.println("WattsUp Meter Created");
                registerEventListeners(meter);
                System.out.println("WattsUp Meter Connecting");
                meter.connect(false);
                meter.setLoggingModeSerial(1);
                System.out.println("WattsUp Meter Connected " + meter.isConnected());
            } catch (IOException ex) {
                Logger.getLogger(WattsUpMeterDataSourceAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This registers the required meters for the energy modeller.
     *
     * @param meter The meter to register the event listeners for
     */
    private void registerEventListeners(WattsUp meter) {
        meter.registerListener(new WattsUpDataAvailableListener() {
            @Override
            public void processDataAvailable(final WattsUpDataAvailableEvent event) {
                GregorianCalendar calander = new GregorianCalendar();
                long clock = TimeUnit.MILLISECONDS.toSeconds(calander.getTimeInMillis());
                HostMeasurement measurement = new HostMeasurement(host, clock);
                WattsUpPacket[] values = event.getValue();
                String watts = values[0].get("watts").getValue();
                String volts = values[0].get("volts").getValue();
                String amps = values[0].get("amps").getValue();
                String wattskwh = values[0].get("wattskwh").getValue();
                watts = "" + changeOrderOfMagnitude(watts, 1);
                volts = "" + changeOrderOfMagnitude(volts, 1);
                amps = "" + changeOrderOfMagnitude(amps, 3);
                measurement.addMetric(new MetricValue(KpiList.POWER_KPI_NAME, KpiList.POWER_KPI_NAME, watts, clock));
                measurement.addMetric(new MetricValue(KpiList.ENERGY_KPI_NAME, KpiList.ENERGY_KPI_NAME, wattskwh, clock));
                measurement.addMetric(new MetricValue(VOLTAGE_KPI_NAME, VOLTAGE_KPI_NAME, volts, clock));
                measurement.addMetric(new MetricValue(CURRENT_KPI_NAME, CURRENT_KPI_NAME, amps, clock));
                try {
                    CpuPerc cpu = sigar.getCpuPerc();
                    cpuMeasure.add(new CPUUtilisation(clock, cpu));
                    Mem mem = sigar.getMem();
                    measurement.addMetric(new MetricValue(KpiList.CPU_IDLE_KPI_NAME, KpiList.CPU_IDLE_KPI_NAME, cpu.getIdle() * 100 + "", clock));
                    measurement.addMetric(new MetricValue(KpiList.CPU_INTERUPT_KPI_NAME, KpiList.CPU_INTERUPT_KPI_NAME, cpu.getIrq() * 100 + "", clock));
                    measurement.addMetric(new MetricValue(KpiList.CPU_IO_WAIT_KPI_NAME, KpiList.CPU_IO_WAIT_KPI_NAME, cpu.getWait() * 100 + "", clock));
                    measurement.addMetric(new MetricValue(KpiList.CPU_NICE_KPI_NAME, KpiList.CPU_NICE_KPI_NAME, cpu.getNice() * 100 + "", clock));
                    measurement.addMetric(new MetricValue(KpiList.CPU_SOFT_IRQ_KPI_NAME, KpiList.CPU_SOFT_IRQ_KPI_NAME, cpu.getIrq() * 100 + "", clock));
                    measurement.addMetric(new MetricValue(KpiList.CPU_STEAL_KPI_NAME, KpiList.CPU_STEAL_KPI_NAME, cpu.getStolen() * 100 + "", clock));
                    measurement.addMetric(new MetricValue(KpiList.CPU_SYSTEM_KPI_NAME, KpiList.CPU_SYSTEM_KPI_NAME, cpu.getSys() * 100 + "", clock));
                    measurement.addMetric(new MetricValue(KpiList.CPU_USER_KPI_NAME, KpiList.CPU_USER_KPI_NAME, cpu.getUser() * 100 + "", clock));

                    measurement.addMetric(new MetricValue(KpiList.MEMORY_AVAILABLE_KPI_NAME, KpiList.MEMORY_AVAILABLE_KPI_NAME, (int) (Double.valueOf(mem.getActualFree()) / 1048576) + "", clock));
                    measurement.addMetric(new MetricValue(KpiList.MEMORY_TOTAL_KPI_NAME, KpiList.MEMORY_TOTAL_KPI_NAME, (int) (Double.valueOf(mem.getTotal()) / 1048576) + "", clock));

                } catch (SigarException ex) {
                    Logger.getLogger(WattsUpMeterDataSourceAdaptor.class.getName()).log(Level.SEVERE, null, ex);
                }
                current = measurement;
                if (lowest == null || measurement.getPower() < lowest.getPower()) {
                    lowest = measurement;
                }
                if (highest == null || measurement.getPower() > highest.getPower()) {
                    highest = measurement;
                }
            }
        });

        meter.registerListener(new WattsUpMemoryCleanListener() {
            @Override
            public void processWattsUpReset(WattsUpMemoryCleanEvent event) {
                System.out.println("WattsUp Meter Memory Just Cleaned");
            }
        });

        meter.registerListener(new WattsUpStopLoggingListener() {
            @Override
            public void processStopLogging(WattsUpStopLoggingEvent event) {
                System.out.println("WattsUp Meter Logging Stopped");
            }
        });

        meter.registerListener(new WattsUpDisconnectListener() {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event) {
                System.out.println("WattsUp Meter Client Exiting");
            }
        });
    }

    /**
     * The output of a WattsUp? meter has no decimal places. This shifts the
     * output by the correct magnitude in order that the value makes sense.
     *
     * @param meterOutput The output from the WattsUp? meter.
     * @param position The order of magnitude to reduce the size of the value
     * by.
     * @return The double value of the meters output string.
     */
    private static double changeOrderOfMagnitude(String meterOutput, int position) {
        double answer = Double.valueOf(meterOutput);
        if (position > 0) {
            answer = answer / Math.pow(10, position);
        }
        return answer;
    }

    @Override
    public Host getHostByName(String hostname) {
        //ignore hostname and just return the current localhost
        if (hostname.equals(host.getHostName())) {
            return host;
        } else {
            return new Host(1, hostname);
        }
    }

    @Override
    public VmDeployed getVmByName(String name) {
        return null;
    }

    @Override
    public List<Host> getHostList() {
        List<Host> answer = new ArrayList<>();
        answer.add(host);
        return answer;
    }

    @Override
    public List<VmDeployed> getVmList() {
        return new ArrayList<>();
    }

    @Override
    public List<EnergyUsageSource> getHostAndVmList() {
        List<EnergyUsageSource> answer = new ArrayList<>();
        answer.add(host);
        return answer;
    }

    @Override
    public HostMeasurement getHostData(Host host) {
        while (current == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(WattsUpMeterDataSourceAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return current;
    }

    @Override
    public List<HostMeasurement> getHostData() {
        List<HostMeasurement> answer = new ArrayList<>();
        answer.add(getHostData(host));
        return answer;
    }

    @Override
    public List<HostMeasurement> getHostData(List<Host> hostList) {
        List<HostMeasurement> answer = new ArrayList<>();
        if (hostList.contains(host)) {
            answer.add(getHostData(host));
        }
        return answer;
    }

    @Override
    public VmMeasurement getVmData(VmDeployed vm) {
        return null;
    }

    @Override
    public List<VmMeasurement> getVmData() {
        return new ArrayList<>();
    }

    @Override
    public List<VmMeasurement> getVmData(List<VmDeployed> vmList) {
        return new ArrayList<>();
    }

    @Override
    public CurrentUsageRecord getCurrentEnergyUsage(Host host) {
        CurrentUsageRecord answer = new CurrentUsageRecord(host);
        HostMeasurement measurement = getHostData(host);
        answer.setPower(measurement.getMetric(POWER_KPI_NAME).getValue());
        answer.setVoltage(current.getMetric(VOLTAGE_KPI_NAME).getValue());
        answer.setCurrent(current.getMetric(CURRENT_KPI_NAME).getValue());
        return answer;
    }

    @Override
    public double getLowestHostPowerUsage(Host host) {
        return lowest.getPower();
    }

    @Override
    public double getHighestHostPowerUsage(Host host) {
        return highest.getPower();
    }

    @Override
    public synchronized double getCpuUtilisation(Host host, int lastNSeconds) {
        double count = 0.0;
        double sumOfUtil = 0.0;
        GregorianCalendar cal = new GregorianCalendar();
        long now = TimeUnit.MILLISECONDS.toSeconds(cal.getTimeInMillis());
        long nowMinustime = now - lastNSeconds;
        CopyOnWriteArrayList<CPUUtilisation> list = new CopyOnWriteArrayList<>();
        list.addAll(cpuMeasure);
        for (Iterator<CPUUtilisation> it = list.iterator(); it.hasNext();) {
            CPUUtilisation util = it.next();
            if (util.isOlderThan(nowMinustime)) {
                list.remove(util);
                cpuMeasure.remove(util);
            } else {
                sumOfUtil = sumOfUtil + util.getCpuBusy();
                count = count + 1;
            }
        }
        return sumOfUtil / count;
    }

    /**
     * This is a CPU utilisation record for the WattsUp Meter data source
     * adaptor.
     */
    private class CPUUtilisation {

        private final long clock;
        private final CpuPerc cpu;

        /**
         * This creates a new CPU Utilisation record
         *
         * @param clock the time when the CPU Utilisation was taken
         * @param cpu The CPU utilisation record.
         */
        public CPUUtilisation(long clock, CpuPerc cpu) {
            this.clock = clock;
            this.cpu = cpu;
        }

        /**
         * The time when this record was taken
         *
         * @return The UTC time for this record.
         */
        public long getClock() {
            return clock;
        }

        /**
         * This returns the Sigar object representing CPU load information.
         *
         * @return The sigar CPU object
         */
        public CpuPerc getCpu() {
            return cpu;
        }

        /**
         * This returns the percentage of time the CPU was idle.
         *
         * @return 0..1 for how idle the CPU was at a specified time frame.
         */
        public double getCpuIdle() {
            return cpu.getIdle();
        }

        /**
         * This returns the percentage of time the CPU was busy.
         *
         * @return 0..1 for how busy the CPU was at a specified time frame.
         */
        public double getCpuBusy() {
            return 1 - cpu.getIdle();
        }

        /**
         * This indicates if this CPU utilisation object is older than a
         * specified time.
         *
         * @param time The UTC time to compare to
         * @return If the current item is older than the date specified.
         */
        public boolean isOlderThan(long time) {
            return clock < time;
        }

    }

    /**
     * In the event the WattsUp Meter port is captured already and the output is
     * being placed in a log file this can read the file and get the required
     * data.
     */
    private class WattsUpTailer extends TailerListenerAdapter {

        @Override
        public void handle(String line) {
            try {
                //This avoids looking at the header of the file
                if (line.startsWith("W")) {
                    return;
                }
                String[] values = line.split(",");
                GregorianCalendar calander = new GregorianCalendar();
                long clock = TimeUnit.MILLISECONDS.toSeconds(calander.getTimeInMillis());
                /**
                 * The Watts up meter column order is: W,V, A, WH, COST, WH/Mo,
                 * Cost/Mo, Wmax, Vmax, Amax, Wmin, Vmin, Amin, PF, DC, PC, Hz,
                 * VA
                 */
                String watts = values[0];
                String volts = values[1];
                String amps = values[2];
                String wattskwh = values[3];

                HostMeasurement measurement = new HostMeasurement(host, clock);
                measurement.addMetric(new MetricValue(KpiList.POWER_KPI_NAME, KpiList.POWER_KPI_NAME, watts, clock));
                measurement.addMetric(new MetricValue(KpiList.ENERGY_KPI_NAME, KpiList.ENERGY_KPI_NAME, wattskwh, clock));
                measurement.addMetric(new MetricValue(VOLTAGE_KPI_NAME, VOLTAGE_KPI_NAME, volts, clock));
                measurement.addMetric(new MetricValue(CURRENT_KPI_NAME, CURRENT_KPI_NAME, amps, clock));
                boolean valid = true;
                try {
                    CpuPerc cpu = sigar.getCpuPerc();
                    cpuMeasure.add(new CPUUtilisation(clock, cpu));
                    Mem mem = sigar.getMem();
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.CPU_IDLE_KPI_NAME, KpiList.CPU_IDLE_KPI_NAME, cpu.getIdle() * 100 + "", clock));
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.CPU_INTERUPT_KPI_NAME, KpiList.CPU_INTERUPT_KPI_NAME, cpu.getIrq() * 100 + "", clock));
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.CPU_IO_WAIT_KPI_NAME, KpiList.CPU_IO_WAIT_KPI_NAME, cpu.getWait() * 100 + "", clock));
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.CPU_NICE_KPI_NAME, KpiList.CPU_NICE_KPI_NAME, cpu.getNice() * 100 + "", clock));
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.CPU_SOFT_IRQ_KPI_NAME, KpiList.CPU_SOFT_IRQ_KPI_NAME, cpu.getIrq() * 100 + "", clock));
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.CPU_STEAL_KPI_NAME, KpiList.CPU_STEAL_KPI_NAME, cpu.getStolen() * 100 + "", clock));
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.CPU_SYSTEM_KPI_NAME, KpiList.CPU_SYSTEM_KPI_NAME, cpu.getSys() * 100 + "", clock));
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.CPU_USER_KPI_NAME, KpiList.CPU_USER_KPI_NAME, cpu.getUser() * 100 + "", clock));

                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.MEMORY_AVAILABLE_KPI_NAME, KpiList.MEMORY_AVAILABLE_KPI_NAME, (int) (Double.valueOf(mem.getActualFree()) / 1048576) + "", clock));
                    valid = valid && validatedAddMetric(measurement, new MetricValue(KpiList.MEMORY_TOTAL_KPI_NAME, KpiList.MEMORY_TOTAL_KPI_NAME, (int) (Double.valueOf(mem.getTotal()) / 1048576) + "", clock));
                } catch (SigarException ex) {
                    Logger.getLogger(WattsUpMeterDataSourceAdaptor.class.getName()).log(Level.SEVERE,
                            "An exception occured when obtaining data from Sigar", ex);
                    /**
                     * Returning to avoid placing a bad measurement into the
                     * readable output of the WattsUp meter adaptor.
                     */
                    return;
                }
                if (!valid) {
                    return;
                }
                current = measurement;
                if (lowest == null || measurement.getPower() < lowest.getPower()) {
                    lowest = measurement;
                }
                if (highest == null || measurement.getPower() > highest.getPower()) {
                    highest = measurement;
                }
            } catch (NumberFormatException ex) {
                //Ignore these errors and carry on. It may just be the header line.
                ex.printStackTrace();
            }
        }

        /**
         * This ensures that metric values are not added in cases where NaN etc
         * is given as an output from Sigar.
         *
         * @param measurement The measurement to add the value to
         * @param value The value to add.
         * @return The measurement with the added metric only in cases where the
         * values correct.
         */
        private boolean validatedAddMetric(Measurement measurement, MetricValue value) {
            if (Double.isNaN(value.getValue()) || Double.isInfinite(value.getValue())) {
                return false;
            }
            measurement.addMetric(value);
            return true;
        }
    }

}
