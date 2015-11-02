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

package integratedtoolkit.components.scheduler.impl;

import integratedtoolkit.api.ITExecution;
import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.components.impl.TaskScheduler.ExecutionProfile;
import integratedtoolkit.components.scheduler.SchedulerPolicies;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.parameter.Parameter;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.types.resources.Worker;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import org.apache.log4j.Logger;

public abstract class AsceticPolicies extends SchedulerPolicies {

    protected static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    protected static final boolean debug = logger.isDebugEnabled();

    private final double timeWeight;
    private final double costWeight;
    private final double energyWeight;

    public AsceticPolicies(double timeWeight, double costWeight, double energyWeight) {
        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.energyWeight = energyWeight;
    }

    public PriorityQueue<ObjectValue<Task>> sortTasksForResource(Worker host, List<Task> tasks, ExecutionProfile[][] profiles) {
        PriorityQueue<ObjectValue<Task>> pq = new PriorityQueue<ObjectValue<Task>>();
        for (Task t : tasks) {
            if (host.canRun(t.getTaskParams().getId())) {
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
                            HashSet<Resource> hosts = Comm.getData(dId.getRenaming()).getAllHosts();
                            for (Resource h : hosts) {
                                if (h != null && h.compareTo(host) == 0) {
                                    score++;
                                }
                            }
                        }

                    }
                }
                logger.info("Available Resource: " + host.getName() + ". Task: " + t.getId() + ", score: " + score);
                pq.add(new ObjectValue<Task>(t, score));
            }
        }

        return pq;
    }

    public PriorityQueue<ObjectValue<Worker>> sortResourcesForTask(Task t, Set<Worker> resources, ExecutionProfile[][] profiles) {
        PriorityQueue<ObjectValue<Worker>> pq = new PriorityQueue<ObjectValue<Worker>>();

        Parameter[] params = t.getTaskParams().getParameters();
        HashMap<Resource, Integer> hostToScore = new HashMap<Resource, Integer>(params.length * 2);

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
                    HashSet<Resource> hosts = Comm.getData(dId.getRenaming()).getAllHosts();
                    for (Resource host : hosts) {
                        if (host == null) {
                            continue;
                        }
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
        for (Worker resource : resources) {
            Integer score = hostToScore.get(resource);
            if (score == null) {
                pq.offer(new ObjectValue<Worker>(resource, 0));
                logger.info("Resource: " + resource.getName() + ", score: 0");
            } else {
                pq.offer(new ObjectValue<Worker>(resource, score));
                logger.info("Resource: " + resource.getName() + ", score: " + score);
            }
        }
        return pq;
    }

    @Override
    public OwnerTask[] stealTasks(Worker destResource, HashMap<String, LinkedList<Task>> pendingTasks, int numberOfTasks, ExecutionProfile[][] profiles) {

        OwnerTask[] stolenTasks = new OwnerTask[numberOfTasks];
        PriorityQueue<ObjectValue<OwnerTask>> pq = new PriorityQueue<ObjectValue<OwnerTask>>();
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
                if (destResource.canRun(t.getTaskParams().getId())) {
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
                                HashSet<Resource> hosts = Comm.getData(dId.getRenaming()).getAllHosts();
                                for (Resource host : hosts) {
                                    if (host == null) {
                                        continue;
                                    }

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
                pq.offer(new ObjectValue<OwnerTask>(new OwnerTask(ownerName, t), score));
            }
        }

        int i = 0;
        while (pq.iterator().hasNext() && i < numberOfTasks) {
            stolenTasks[i] = pq.iterator().next().o;
            i++;
        }
        return stolenTasks;
    }

    @Override
    public LinkedList<Implementation> sortImplementationsForResource(LinkedList<Implementation> runnable, Worker resource, ExecutionProfile[][] profiles) {
        LinkedList<Implementation> sorted = new LinkedList<Implementation>();
        PriorityQueue<ObjectValue<Implementation>> pq = new PriorityQueue<ObjectValue<Implementation>>();
        String IPv4 = resource.getName();
        if (!runnable.isEmpty()) {
        	//int coreId = runnable.getFirst().getCoreId();
            for (Implementation impl : runnable) {
                pq.add(new ObjectValue<Implementation>(impl, getValue(resource, impl, profiles)));
            	
            }
        }
        logger.debug("Sorted list of implementations for "+IPv4);
        for (SchedulerPolicies.ObjectValue<Implementation> impl : pq) {
        	logger.debug(" - Implementation: core"+impl.o.getCoreId()+"impl"+impl.o.getImplementationId() +" value: "+impl.value);
        	sorted.addLast(impl.o);
        }
        return sorted;
    }

    private double getValue(Worker w, Implementation impl, ExecutionProfile[][] profiles) {
    	logger.debug("Values for core"+impl.getCoreId()+"impl"+impl.getImplementationId()+":");
    	double price = Ascetic.getPrice(w, impl);
        double power = Ascetic.getPower(w, impl);
        double time = profiles[impl.getCoreId()][impl.getImplementationId()].getAverageExecutionTime(1l); 
        double cost = (price*time)/(3600*1000);
        double energy = (power * time)/(3600*1000);
        
        logger.debug(" - Cost = "+ cost +" ("+costWeight+") Energy: "+ energy +" ("+energyWeight+") Time: "+time+" (" + timeWeight+ ")");
        return -(timeWeight * time) - (costWeight * cost) - (energyWeight * energy);
    }
}
