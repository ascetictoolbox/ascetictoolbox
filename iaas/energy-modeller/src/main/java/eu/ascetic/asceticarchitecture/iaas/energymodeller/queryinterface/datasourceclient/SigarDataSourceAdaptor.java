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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.ioutils.Settings;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * The aim of this class is initially to take data from Sigar and to place it
 * into a format that is suitable for the Watt meter emulator. It will not
 * report on energy consumption, but avoids sending calls across the network to
 * gain local utilisation information such as CPU.
 *
 * This will aid scalability and reduce network overhead but may mean
 * measurements do not match the main data source adaptor in use.
 *
 * @author Richard
 */
public class SigarDataSourceAdaptor implements HostDataSource {

    private static final String VOLTAGE_KPI_NAME = "Voltage";
    private static final String CURRENT_KPI_NAME = "Current";
    private static final Sigar SOURCE = new Sigar();
    private Host host;
    private HostMeasurement lowest = null;
    private HostMeasurement highest = null;
    private final LinkedList<CPUUtilisation> cpuMeasure = new LinkedList<>();
    private final Settings settings = new Settings("energy-modeller-data-source-sigar.properties");

    /**
     * SingletonHolder is loaded on the first execution of
     * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
     * not before.
     */
    private static class SingletonHolder {

        private static final SigarDataSourceAdaptor INSTANCE = new SigarDataSourceAdaptor();
    }

    /**
     * This creates a new instance of the WattsUp Meter data source adaptor.
     * This adaptor is intended for using the energy modeller on a local
     * machine.
     *
     * @return A singleton instance of a WattsUp? meter data source adaptor.
     */
    public static SigarDataSourceAdaptor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * This creates a new Sigar data source adaptor, that is capable of taking
     * data from Sigar, for use inside the ASCETiC architecture.
     *
     */
    private SigarDataSourceAdaptor() {
        int hostId = settings.getInt("hostId", 1);
        String hostname = settings.getString("hostname", "localhost");
        if (settings.isChanged()) {
            settings.save("energy-modeller-data-source-sigar.properties");
        }
        host = new Host(hostId, hostname);
        try {
            Mem mem = SOURCE.getMem();
            host.setRamMb((int) (Double.valueOf(mem.getTotal()) / 1048576));
        } catch (SigarException ex) {
            Logger.getLogger(SigarDataSourceAdaptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This acquires the local host measurement data.
     *
     * @return The latest host measurement data
     */
    public HostMeasurement getMeasurement() {
        GregorianCalendar calander = new GregorianCalendar();
        long clock = TimeUnit.MILLISECONDS.toSeconds(calander.getTimeInMillis());
        HostMeasurement measurement = new HostMeasurement(host, clock);
        try {
            CpuPerc cpu = SOURCE.getCpuPerc();
            cpuMeasure.add(new CPUUtilisation(clock, cpu));
            Mem mem = SOURCE.getMem();
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
            Logger.getLogger(SigarDataSourceAdaptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (lowest == null || measurement.getPower() < lowest.getPower()) {
            lowest = measurement;
        }
        if (highest == null || measurement.getPower() > highest.getPower()) {
            highest = measurement;
        }
        return measurement;
    }

    /**
     * This sets the host object that is currently the local host.
     *
     * @param host The host to report the data for
     */
    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public Host getHostByName(String hostname) {
        //ignore hostname and just return the current localhost
        return host;
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
        return getMeasurement();
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
        answer.setVoltage(getMeasurement().getMetric(VOLTAGE_KPI_NAME).getValue());
        answer.setCurrent(getMeasurement().getMetric(CURRENT_KPI_NAME).getValue());
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
        for (CPUUtilisation util : list) {
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
     * This is a CPU utilisation record for the Sigar data source adaptor.
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

}
