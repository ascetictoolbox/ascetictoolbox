/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integratedtoolkit.ascetic;

import integratedtoolkit.api.ITExecution;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.components.scheduler.SchedulerPolicies;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Parameter;
import integratedtoolkit.types.Resource;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.util.ResourceManager;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

public class AsceticSchedulerPolicies extends SchedulerPolicies {

    public PriorityQueue<SchedulerPolicies.Object_Value<Task>> sortTasksForResource(Resource host, List<Task> tasks, TaskScheduler.ExecutionProfile[][] profiles) {
        PriorityQueue<SchedulerPolicies.Object_Value<Task>> pq = new PriorityQueue<SchedulerPolicies.Object_Value<Task>>();
        for (Task t : tasks) {
            if (ResourceManager.matches(host.getName(), t.getTaskParams().getId())) {
                int score = 0;
                Parameter[] params = t.getTaskParams().getParameters();
                for (Parameter p : params) {
                    if (p instanceof Parameter.DependencyParameter) {
                        Parameter.DependencyParameter fp = (Parameter.DependencyParameter) p;
                        DataInstanceId dId = null;
                        switch (fp.getDirection()) {
                            case IN:
                                DataAccessId.RAccessId raId = (DataAccessId.RAccessId) fp.getDataAccessId();
                                dId = raId.getReadDataInstance();
                                break;
                            case INOUT:
                                DataAccessId.RWAccessId rwaId = (DataAccessId.RWAccessId) fp.getDataAccessId();
                                dId = rwaId.getReadDataInstance();
                                break;
                            case OUT:
                                break;
                        }
                        if (dId != null) {
                            TreeSet<String> hosts = FTM.getHosts(dId);
                            for (String h : hosts) {
                                if (h.compareTo(host.getName()) == 0) {
                                    score++;
                                }
                            }
                        }

                    }
                }
                pq.add(new SchedulerPolicies.Object_Value<Task>(t, score));
            }
        }

        return pq;
    }

    public PriorityQueue<SchedulerPolicies.Object_Value<Resource>> sortResourcesForTask(Task t, Set<Resource> resources, TaskScheduler.ExecutionProfile[][] profiles) {
        PriorityQueue<SchedulerPolicies.Object_Value<Resource>> pq = new PriorityQueue<SchedulerPolicies.Object_Value<Resource>>();

        Parameter[] params = t.getTaskParams().getParameters();
        HashMap<String, Integer> hostToScore = new HashMap<String, Integer>(params.length * 2);

        // Obtain the scores for each host: number of task parameters that are located in the host
        for (Parameter p : params) {
            if (p instanceof Parameter.DependencyParameter && p.getDirection() != ITExecution.ParamDirection.OUT) {
                Parameter.DependencyParameter dp = (Parameter.DependencyParameter) p;
                DataInstanceId dId = null;
                switch (dp.getDirection()) {
                    case IN:
                        DataAccessId.RAccessId raId = (DataAccessId.RAccessId) dp.getDataAccessId();
                        dId = raId.getReadDataInstance();
                        break;
                    case INOUT:
                        DataAccessId.RWAccessId rwaId = (DataAccessId.RWAccessId) dp.getDataAccessId();
                        dId = rwaId.getReadDataInstance();
                        break;
                    case OUT:
                        break;
                }

                if (dId != null) {
                    TreeSet<String> hosts = FTM.getHosts(dId);
                    for (String host : hosts) {
                        Integer score;
                        if ((score = hostToScore.get(host)) == null) {
                            score = new Integer(0);
                            hostToScore.put(host, score);
                        }
                        hostToScore.put(host, score + 1);
                    }
                }
            }
        }
        int coreId= t.getTaskParams().getId();
        
        for (Resource resource : resources) {
            int[] consumptions = Ascetic.getConsumptions(resource.getName(), coreId);
            int minConsumption = Integer.MAX_VALUE;
            
            for (int implId=0;implId<profiles[coreId].length;implId++){
                int consumption = profiles[coreId][implId].getAverageExecutionTime(0l).intValue()*consumptions[implId];
                if (minConsumption>consumption){
                    minConsumption=consumption;
                }
            }
            Integer score = hostToScore.get(resource.getName());
            if (score == null) {
                pq.offer(new SchedulerPolicies.Object_Value<Resource>(resource, 0));
            } else {
                pq.offer(new SchedulerPolicies.Object_Value<Resource>(resource, -minConsumption));
            }
        }
        return pq;
    }

    //Unused
    @Override
    public OwnerTask[] stealTasks(String destResource, HashMap<String, LinkedList<Task>> pendingTasks, int numberOfTasks, TaskScheduler.ExecutionProfile[][] profiles) {
        OwnerTask[] stolenTasks = new OwnerTask[numberOfTasks];
        return stolenTasks;
    }

    @Override
    public LinkedList<Implementation> sortImplementationsForResource(LinkedList<Implementation> runnable, Resource resource, TaskScheduler.ExecutionProfile[][] profiles) {
        LinkedList<Implementation> sorted = new LinkedList<Implementation>();
        PriorityQueue<SchedulerPolicies.Object_Value<Implementation>> pq = new PriorityQueue<SchedulerPolicies.Object_Value<Implementation>>();
        if (!runnable.isEmpty()) {
            int coreId = runnable.getFirst().getCoreId();
            String IPv4 = resource.getName();
            int[] consumptions = Ascetic.getConsumptions(IPv4, coreId);
            for (Implementation impl : runnable) {
                pq.add(new SchedulerPolicies.Object_Value<Implementation>(
                        impl, 
                        (profiles[impl.getCoreId()][impl.getImplementationId()].getAverageExecutionTime(100l)).intValue()*consumptions[impl.getImplementationId()]
                        )
                );
            }
        }
        for (SchedulerPolicies.Object_Value<Implementation> impl: pq){
            sorted.addLast(impl.o);
        }
        return sorted;
    }
}
