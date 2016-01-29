package es.bsc.demiurge.monitoring.ganglia;

import es.bsc.demiurge.core.drivers.Monitoring;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
public class GangliaMonitoring implements Monitoring<HostGanglia> {
	@Override
	public HostGanglia createHost(String hostName) {
		return new HostGanglia(hostName);
	}
}
