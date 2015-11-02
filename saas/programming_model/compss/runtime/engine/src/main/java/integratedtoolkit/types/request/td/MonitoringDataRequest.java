/**
 *
 *   Copyright 2015-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.types.request.td;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.ResourceManager;

import java.util.concurrent.Semaphore;

import eu.ascetic.paas.applicationmanager.amqp.model.VM;

/**
 * The MonitoringDataRequest class represents a request to obtain the current
 * resources and cores that can be run
 */
public class MonitoringDataRequest extends TDRequest {

    /**
     * Semaphore where to synchronize until the operation is done
     */
    private Semaphore sem;
    /**
     * Applications progress description
     */
    private String response;

    /**
     * Constructs a new TaskStateRequest
     *
     * @param sem semaphore where to synchronize until the current state is
     * described
     */
    public MonitoringDataRequest(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the semaphore where to synchronize until the current state is
     * described
     *
     * @return the semaphore where to synchronize until the current state is
     * described
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the current state is
     * described
     *
     * @param sem the semaphore where to synchronize until the current state is
     * described
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the progress description in an xml format string
     *
     * @return progress description in an xml format string
     */
    public String getResponse() {
        return response;
    }

    /**
     * Sets the current state description
     *
     * @param response current state description
     */
    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) {
        String prefix = "\t";
        StringBuilder monitorData = new StringBuilder();
        monitorData.append(ts.getCoresMonitoringData(prefix));

        monitorData.append(prefix).append("<ResourceInfo>").append("\n");
        monitorData.append(ResourceManager.getPendingRequestsMonitorData(prefix + "\t"));
        for (Worker r : ResourceManager.getAllWorkers()) {
            monitorData.append(prefix + "\t").append("<Resource id=\"" + r.getName() + "\">").append("\n");
            //CPU, Core, Memory, Disk, Provider, Image --> Inside resource
            monitorData.append(r.getMonitoringData(prefix + "\t\t"));
            String runningTasks = ts.getRunningTasksMonitorData(r);
            if (runningTasks != null) {
                //Resource state = running
                monitorData.append(prefix + "\t\t").append("<Status>").append("Running").append("</Status>").append("\n");
                monitorData.append(prefix + "\t\t").append("<Tasks>").append(runningTasks).append("</Tasks>").append("\n");
            } else {
                //Resource state = on destroy
                monitorData.append(prefix + "\t\t").append("<Status>").append("On Destroy").append("</Status>").append("\n");
                monitorData.append(prefix + "\t\t").append("<Tasks>").append("</Tasks>").append("\n");
            }
            monitorData.append(prefix + "\t").append("</Resource>").append("\n");
        }
        monitorData.append(prefix).append("</ResourceInfo>").append("\n");
        
        monitorData.append(prefix).append("<StatisticParameter id=\"AccumulatedTime\">" + Ascetic.getAccumulatedTime() +" s </StatisticParameter>").append("\n");
        monitorData.append(prefix).append("<StatisticParameter id=\"AccumulatedCost\">" + Ascetic.getAccumulatedCost() + " € </StatisticParameter>").append("\n");
        monitorData.append(prefix).append("<StatisticParameter id=\"AccumulatedEnergy\">" + Ascetic.getAccumulatedEnergy() + " Wh </StatisticParameter>").append("\n");
        
        for (integratedtoolkit.ascetic.VM vm : Ascetic.getResources()) {
        	monitorData.append(ts.getProfileMetrics(vm, prefix));
        	
        }
        response = monitorData.toString();
        sem.release();
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.MONITOR_DATA;
    }
}
