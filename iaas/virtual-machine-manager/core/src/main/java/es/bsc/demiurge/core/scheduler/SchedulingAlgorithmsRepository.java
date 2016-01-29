package es.bsc.demiurge.core.scheduler;

import es.bsc.demiurge.core.scheduler.schedulingalgorithms.SchedAlgorithm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mmacias on 19/11/15.
 *
 * @deprecated this class must be replaced by clopla related scheduling algorithms
 */
@Deprecated
public class SchedulingAlgorithmsRepository {
	private Map<String, Class<? extends SchedAlgorithm>> schedulingAlgorithms = new HashMap<>();
	private Logger log = LogManager.getLogger(SchedulingAlgorithmsRepository.class);

	/**
	 *
	 * @param schedAlgorithmClasses all the implementing classes must have a default constructor
	 */
	public SchedulingAlgorithmsRepository(Set<Class<? extends SchedAlgorithm>> schedAlgorithmClasses) {
		for(Class<? extends SchedAlgorithm> s : schedAlgorithmClasses) {
			try {
				schedulingAlgorithms.put(s.newInstance().getName(),s);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
	}

	public SchedAlgorithm newInstance(String name) {
		Class<? extends SchedAlgorithm> sa = schedulingAlgorithms.get(name);
		if(sa == null) {
			StringBuilder sb = new StringBuilder("There is no scheduling algorithm called '");
			sb.append(name).append("'. Registered values are: ");
			boolean first = true;
			for(String v : schedulingAlgorithms.keySet()) {
				if(first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(v);
			}
			throw new IllegalArgumentException(sb.toString());
		}
		try {
			return sa.newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getAvailable() {
		return schedulingAlgorithms.keySet();
	}

}
