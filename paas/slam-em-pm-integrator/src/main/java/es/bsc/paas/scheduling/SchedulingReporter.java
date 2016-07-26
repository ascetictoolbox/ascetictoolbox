package es.bsc.paas.scheduling;

import es.bsc.paas.components.SLAManager;
import es.bsc.paas.model.InitiateMonitoringCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class SchedulingReporter {

	@Autowired
	private SLAManager slam;

	// stop reporting metrics for an app/deployment/sla after 24 hours
	// TO DO: - Enable a "endTime" field in the initiateMonitoring command, or
	//        - Enable a "stopMonitoring" command
	private static final long REMOVE_REPORTING_AFTER_MS = 24 * 3600 * 1000;

	private Logger log = LoggerFactory.getLogger(SchedulingReporter.class);

	private TreeSet<MonitoringInfo> monitoringEntries = new TreeSet<>();

	public void onInitiateMonitoringCommandInfo(InitiateMonitoringCommand imc) {
		monitoringEntries.add(new MonitoringInfo(imc));
	}

	@Scheduled(fixedRateString = "${min.reporting.rate}")
	public void reportValuesToSLAM() {
		long until = System.currentTimeMillis();
		long latestNextTime = 0;
		while(latestNextTime <= until && monitoringEntries.size() > 0) {
			latestNextTime = monitoringEntries.first().getNextTime();
			if(latestNextTime <= until) {
				MonitoringInfo mi = monitoringEntries.pollFirst();


			}
		}

		for(MonitoringInfo mi : monitoringEntries) {
			if(mi.getNextTime() > until) {
				break;
			}


		}
	}
}
