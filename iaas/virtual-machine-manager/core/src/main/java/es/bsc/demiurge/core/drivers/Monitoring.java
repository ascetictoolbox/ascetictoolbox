package es.bsc.demiurge.core.drivers;

import es.bsc.demiurge.core.monitoring.hosts.Host;

/**
 * @author Mario Macías http://github.com/mariomac
 */
public interface Monitoring<T extends Host> {
	T createHost(String hostName);
}
