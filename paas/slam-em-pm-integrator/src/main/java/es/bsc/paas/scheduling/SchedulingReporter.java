package es.bsc.paas.scheduling;

import es.bsc.paas.components.PaasEnergyModeller;
import es.bsc.paas.components.SLAManager;
import es.bsc.paas.model.InitiateMonitoringCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.TreeSet;

/**
 * This class checks, every second (or the value specified by the "min.reporting.rate" property), for those
 * deployments
 *
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class SchedulingReporter {
    
    @Autowired
    PaasEnergyModeller em;

    @Autowired
    private SLAManager slam;

    // stop reporting metrics for an app/deployment/sla after 24 hours
    // TO DO: - Enable a "endTime" field in the initiateMonitoring command, or
    //        - Enable a "stopMonitoring" command
    private static final long REMOVE_REPORTING_AFTER_MS = 24 * 3600 * 1000;

    private Logger log = LoggerFactory.getLogger(SchedulingReporter.class);

    private TreeSet<MonitoringInfo> monitoringEntries = new TreeSet<>();

    public void onInitiateMonitoringCommandInfo(InitiateMonitoringCommand imc) {
        synchronized (monitoringEntries) {
            monitoringEntries.add(new MonitoringInfo(imc));
        }
    }

    /**
     * This task is executed periodically. It retrieves the info from the Energy Modeller
     * (in the future, also from the Price Modeller) and resubmits the information to the SLA
     * Manager through an MQ topic
     */
    @Scheduled(fixedRateString = "${min.reporting.rate}")
    public void reportValuesToSLAM() {
        try {
            synchronized (monitoringEntries) {
                if (monitoringEntries.size() > 0) {
                    long until = System.currentTimeMillis();
                    long latestNextTime = monitoringEntries.first().getNextTime();
                    while (latestNextTime <= until && monitoringEntries.size() > 0) {
                        MonitoringInfo mi = monitoringEntries.pollFirst();
                        try {
                            slam.reportEstimation(
                                mi.getApplicationId(), mi.getDeploymentId(), until,
                                em.getEnergyEstimation(mi.getApplicationId(), mi.getDeploymentId(), mi.getFrequency()),
                                em.getPowerEstimation(mi.getApplicationId(), mi.getDeploymentId(), mi.getFrequency()),
                                em.getPriceEstimation(mi.getApplicationId(), mi.getDeploymentId())
                            );
                            
                            slam.reportMeasurement(
                                mi.getApplicationId(), mi.getDeploymentId(), until,
                                em.getEnergyConsumption(mi.getApplicationId(), mi.getDeploymentId()),
                                em.getPowerConsumption(mi.getApplicationId(), mi.getDeploymentId())
                            );

                            mi.setNextTime(mi.getNextTime() + mi.getFrequency());
                            monitoringEntries.add(mi);
                        } catch (Exception e) {
                            log.warn("Error retrieving energy estimations: " + e.getMessage() + ". Removing app/deployment from reporting scheduler");
                            log.debug("More detail: ", e);
                        }
                        if (monitoringEntries.size() > 0) {
                            latestNextTime = monitoringEntries.first().getNextTime();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}