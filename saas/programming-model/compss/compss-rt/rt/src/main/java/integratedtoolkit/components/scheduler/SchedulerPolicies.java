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
package integratedtoolkit.components.scheduler;

import integratedtoolkit.components.impl.FileTransferManager;
import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Resource;
import integratedtoolkit.types.Task;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public abstract class SchedulerPolicies {

    public JobManager JM;
    public FileTransferManager FTM;

    public abstract PriorityQueue<Object_Value<Task>> sortTasksForResource(Resource hostName, List<Task> tasksToReschedule, TaskScheduler.ExecutionProfile[][] profiles);

    public abstract PriorityQueue<Object_Value<Resource>> sortResourcesForTask(Task t, Set<Resource> resources, TaskScheduler.ExecutionProfile[][] profiles);

    public abstract OwnerTask[] stealTasks(String destResource, HashMap<String, LinkedList<Task>> pendingTasks, int numberOfTasks, TaskScheduler.ExecutionProfile[][] profiles);

    public abstract LinkedList<Implementation> sortImplementationsForResource(LinkedList<Implementation> get, Resource chosenResource, TaskScheduler.ExecutionProfile[][] profiles);

    public class OwnerTask {

        public String owner;
        public Task t;

        public OwnerTask(String owner, Task t) {
            this.owner = owner;
            this.t = t;
        }
    }

    public class Object_Value<T> implements Comparable<Object_Value<T>> {

        public T o;
        public double value;

        public Object_Value(T o, double value) {
            this.o = o;
            this.value = value;
        }

        public int compareTo(Object_Value<T> o) {
            double result = o.value - this.value;
            if (result != 0 && (int) result == 0) {
                if (result > 0) {
                    result = 1;
                } else {
                    result = -1;
                }
            }
            return (int) result;
        }
    }

}
