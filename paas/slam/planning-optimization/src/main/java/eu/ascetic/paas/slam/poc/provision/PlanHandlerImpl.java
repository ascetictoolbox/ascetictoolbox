/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.ascetic.paas.slam.poc.provision;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.slasoi.gslam.commons.plan.Plan;
import org.slasoi.gslam.commons.plan.RootFoundException;
import org.slasoi.gslam.commons.plan.Task;
import org.slasoi.gslam.commons.plan.TaskFoundException;
import org.slasoi.gslam.core.context.SLAManagerContext;
import org.slasoi.gslam.core.context.SLAManagerContext.SLAManagerContextException;
import org.slasoi.infrastructure.servicemanager.types.ProvisionRequestType;
import org.slasoi.infrastructure.servicemanager.types.ReservationResponseType;
import org.slasoi.isslam.commons.InfrastructureTask;
import org.slasoi.monitoring.common.configuration.MonitoringSystemConfiguration;
import org.slasoi.monitoring.common.features.ComponentMonitoringFeatures;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import eu.ascetic.paas.slam.poc.negotiation.ServiceManagerHandlerImpl;

import slaparser.SLAParser;

public class PlanHandlerImpl {
    public static SLAManagerContext context;
    private SLA sla;
    private ArrayList<ProvisionRequestType> requestList;
    private Plan plan;
    private InfrastructureTask node;
    private ArrayList<Task> taksList;
    private static int planId = 0;
    private static int nodeId = 0;
    private static final String PLAN_ID = "MyPlan";
    public static final STND PLAN_ID_SLA = new STND(PLAN_ID);
    private static final String TASK_ID = "MyTask";
    private static final String PROVISION = "PROVISION";
    private static final String Service_Manager_ID = "INFRA_SERVICE_MANAGER_ID";
    public static final String propertiesFile =
            "generic-slamanager" + System.getProperty("file.separator") + "provisioning-adjustment"
                    + System.getProperty("file.separator") + "provisioning_adjustment.properties";

    private static final Logger LOGGER = Logger.getLogger(PlanHandlerImpl.class);

    public PlanHandlerImpl(SLATemplate slat) {

        super();
        if (slat instanceof SLA) {
            this.sla = (SLA) slat;
            this.requestList = new ArrayList<ProvisionRequestType>();
            this.taksList = new ArrayList<Task>();
        }
        else {
            LOGGER.error("In order to make a plan, SLA object is a reasonable parameter.");
            
        }
    }

    public Plan planMaker() {
        try {
            // create a ProvisionRequest for each node
            this.generateProvisionRequest();
            // create the plan
            plan = new Plan(this.getPlanId());
            LOGGER.info("Plan is created.");
            // create the nodes
            // new InfrastructureTask(taskId, slaId, actionName, serviceManagerId);
            if (this.requestList.size() > 0) {
                for (ProvisionRequestType provisionRequestType : this.requestList) {
                    // String infraID = provisionReservation.get(provisionRequestType).getInfrastructureID();
                    if (plan.getRootTask() == null) {

                        node =
                                new InfrastructureTask(this.getTaskId(), this.sla.getUuid().getValue(),
                                        PlanHandlerImpl.PROVISION, PlanHandlerImpl.Service_Manager_ID);
                        node.setProvisionRequest(provisionRequestType);
                        plan.setRoot(node);
                        LOGGER.info("The main node of the plan is created and set into plan.");
                    }
                    else {
                        taksList.add(new InfrastructureTask(this.getTaskId(), this.sla.getUuid().getValue(),
                                PlanHandlerImpl.PROVISION, PlanHandlerImpl.Service_Manager_ID));
                    }
                }
                this.requestList.clear();
                plan.addChildren(node, taksList);
                LOGGER.info("The children of the node are created and set into the node.");
                return plan;
            }
            else
                return null;
        }
        catch (TaskFoundException e) {
            e.printStackTrace();
            return null;
        }
        catch (RootFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a sample ProvisionRequestType String imageID, String slaTypeID, String locationID, int cores, int
     * memory, String hostName, String notificationURI String MonitoringRequest
     */
    private void generateProvisionRequest() {
        // start to parser the SLA object
        try {
            SLAParser slaParser = new SLAParser(sla);
            // create Monotoring Config
            ComponentMonitoringFeatures[] monitoringFeatures = (ComponentMonitoringFeatures[])ServiceManagerHandlerImpl.getInstance().getMonitoringFeature();
                               
            if(monitoringFeatures==null){
                LOGGER.error("LLMS returns monitoringFeatures as null");
                return;
            }
            LOGGER.info("Get the context of Monitoring Manager.");
            MonitoringSystemConfiguration monitoringConfig;
            if(PlanHandlerImpl.context!=null){
                monitoringConfig =
                    PlanHandlerImpl.context.getMonitorManager().checkMonitorability(sla, monitoringFeatures);
            }
            else {
                monitoringConfig=null;
            }

            ProvisionRequestType temp;
            temp =
                    ServiceManagerHandlerImpl.getInstance().generateProvisionRequestType(
                            slaParser.getResourceRequest().values(), monitoringConfig);
            if (ServiceManagerHandlerImpl.getInstance().query(temp) instanceof ProvisionRequestType) {
                ReservationResponseType reservation = ServiceManagerHandlerImpl.getInstance().reserve(temp);
                requestList.add(temp);
            }
        }
        catch (SLAManagerContextException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getPlanId() {
        planId++;
        return (PLAN_ID + "_" + planId);
    }

    private String getTaskId() {
        nodeId++;
        return (TASK_ID + "_" + nodeId);
    }
}
