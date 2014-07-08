/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.components.scheduler.impl;

import integratedtoolkit.api.ITExecution;
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

public class DefaultSchedulerPolicies  extends SchedulerPolicies {

    public PriorityQueue<Object_Value<Task>> sortTasksForResource(Resource host, List<Task> tasks) {
        PriorityQueue<Object_Value<Task>> pq = new PriorityQueue<Object_Value<Task>>();
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
                pq.add(new Object_Value<Task>(t, score));
            }
        }

        return pq;
    }

    public PriorityQueue<Object_Value<Resource>> sortResourcesForTask(Task t, Set<Resource> resources) {
        PriorityQueue<Object_Value<Resource>> pq = new PriorityQueue<Object_Value<Resource>>();

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
        for (Resource resource : resources) {
            Integer score = hostToScore.get(resource.getName());
            if (score == null) {
                pq.offer(new Object_Value<Resource>(resource, 0));
            } else {
                pq.offer(new Object_Value<Resource>(resource, score));
            }
        }
        return pq;
    }

    @Override
    public OwnerTask[] stealTasks(String destResource, HashMap<String, LinkedList<Task>> pendingTasks, int numberOfTasks) {

        OwnerTask[] stolenTasks = new OwnerTask[numberOfTasks];
        PriorityQueue<Object_Value<OwnerTask>> pq = new PriorityQueue<Object_Value<OwnerTask>>();
        for (java.util.Map.Entry<String, LinkedList<Task>> e : pendingTasks.entrySet()) {
            String ownerName = e.getKey();
            LinkedList<Task> candidates = e.getValue();
            for (Task t : candidates) {
                int score = 0;
                if (t.isSchedulingStrongForced()) {
                    continue;
                } else if (!t.isSchedulingForced()) {
                    score = 10000;
                }
                if (ResourceManager.matches(destResource, t.getTaskParams().getId())) {
                    Parameter[] params = t.getTaskParams().getParameters();
                    for (Parameter p : params) {
                        if (p instanceof Parameter.DependencyParameter) {
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
                                    if (host.equals(ownerName)) {
                                        score--;
                                        break;
                                    }
                                    if (host.equals(destResource)) {
                                        score += 2;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                pq.offer(new Object_Value<OwnerTask>(new OwnerTask(ownerName, t), score));
            }
        }

        int i = 0;
        while (pq.iterator().hasNext() && i < numberOfTasks) {
            stolenTasks[i] = pq.iterator().next().o;
            i++;
        }
        return stolenTasks;
    }

    public LinkedList<Implementation> sortImplementationsForResource(LinkedList<Implementation> runnable, Resource resource) {
        LinkedList<Implementation> sorted = new LinkedList<Implementation>();
        sorted.addAll(runnable);
        return sorted;
    }
}
