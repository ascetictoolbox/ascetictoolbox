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

package integratedtoolkit.components.scheduler;

import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.resources.Worker;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public abstract class SchedulerPolicies {

    public JobManager JM;

    public abstract PriorityQueue<ObjectValue<Task>> sortTasksForResource(Worker hostName, List<Task> tasksToReschedule, TaskScheduler.ExecutionProfile[][] profiles);

    public abstract PriorityQueue<ObjectValue<Worker>> sortResourcesForTask(Task t, Set<Worker> resources, TaskScheduler.ExecutionProfile[][] profiles);

    public abstract OwnerTask[] stealTasks(Worker destResource, HashMap<String, LinkedList<Task>> pendingTasks, int numberOfTasks, TaskScheduler.ExecutionProfile[][] profiles);

    public abstract LinkedList<Implementation> sortImplementationsForResource(LinkedList<Implementation> get, Worker chosenResource, TaskScheduler.ExecutionProfile[][] profiles);

    public class OwnerTask {

        public String owner;
        public Task t;

        public OwnerTask(String owner, Task t) {
            this.owner = owner;
            this.t = t;
        }
    }

    public class ObjectValue<T> implements Comparable<ObjectValue<T>> {

        public T o;
        public double value;

        public ObjectValue(T o, double value) {
            this.o = o;
            this.value = value;
        }

        @Override
        public int compareTo(ObjectValue<T> o) {
            return Double.compare(o.value, this.value);
        }
    }

}
