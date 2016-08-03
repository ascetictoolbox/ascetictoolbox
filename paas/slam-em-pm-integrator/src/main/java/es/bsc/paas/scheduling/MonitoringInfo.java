package es.bsc.paas.scheduling;

import es.bsc.paas.model.InitiateMonitoringCommand;

/**
 * Helper class that wraps the information relative to an
 * "initiateMonitoring" command.
 *
 * @author Mario Macias (http://github.com/mariomac)
 */
class MonitoringInfo implements Comparable<MonitoringInfo> {
    private long startTime;
    private long nextTime;
    private InitiateMonitoringCommand initiateMonitoringCommand;

    public MonitoringInfo(InitiateMonitoringCommand initiateMonitoringCommand) {
            this.initiateMonitoringCommand = initiateMonitoringCommand;
            startTime = System.currentTimeMillis();
            nextTime = startTime + initiateMonitoringCommand.getFrequency();
    }

    /**
     * First timestamp when monitoring information will be requested to the EM and submitted to the SLAM
     * @return
     */
    public long getStartTime() {
            return startTime;
    }

    /**
     * Next time the monitoring information will be requested to the EM and submitted to the SLAM. At each
     * step it is increased in the way: nextTime = nextTime + frequency
     * @return
     */
    public long getNextTime() {
            return nextTime;
    }

    public void setNextTime(long nextTime) {
            this.nextTime = nextTime;
    }

    public String getApplicationId() {
            return initiateMonitoringCommand.getApplicationId();
    }

    public String getDeploymentId() {
            return initiateMonitoringCommand.getDeploymentId();
    }

    public String getSlaId() {
            return initiateMonitoringCommand.getSlaId();
    }

    /**
     * Frequency, in milliseconds, the component repeats the process of requesting estimations to the EM and submitted to the SLAM.
     * @return
     */
    public long getFrequency() {
            return initiateMonitoringCommand.getFrequency();
    }

    /**
     * Helper method to order MonitoringInfo instances according to the "nextTime" property
     * @param o
     * @return
     */
    @Override
    public int compareTo(MonitoringInfo o) {
            return new Long(nextTime).compareTo(o.getNextTime());
    }
}