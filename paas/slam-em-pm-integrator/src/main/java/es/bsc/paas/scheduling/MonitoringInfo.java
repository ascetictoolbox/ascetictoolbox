package es.bsc.paas.scheduling;

import es.bsc.paas.model.InitiateMonitoringCommand;

/**
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

	public long getStartTime() {
		return startTime;
	}

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

	public long getFrequency() {
		return initiateMonitoringCommand.getFrequency();
	}

	@Override
	public int compareTo(MonitoringInfo o) {
		return new Long(nextTime).compareTo(o.getNextTime());
	}
}
