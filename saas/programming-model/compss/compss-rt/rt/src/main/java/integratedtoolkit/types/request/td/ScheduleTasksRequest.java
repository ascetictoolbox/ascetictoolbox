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

package integratedtoolkit.types.request.td;

import integratedtoolkit.types.Task;

import java.util.LinkedList;
import java.util.List;

/**
 * The ScheduleTasksRequest class represents the request to execute a group of
 * dependency-free tasks.
 */
public class ScheduleTasksRequest extends TDRequest {

    /** Set of tasks ready to be executed */
    private List<Task> toSchedule;
    /** Were the dependencies of this tasks freed by the execution of a previous
     * task*/
    private boolean waiting;
    /** Counters of how many tasks per core are waiting to the end of a task
     * which can already be executed */
    private int[] waitingCount;
    /** List of obsolete data versions*/
    private LinkedList<String> obsoletes;

    /**
     * Constructs a new ScheduleTasks Request
     * @param toSchedule Set of tasks ready to be executed
     * @param waiting True if the the dependencies of the tasks to schedule were
     * freed by the execution of a prevus task
     * @param waitingCount Counters of how many tasks per core are waiting to
     * the end of a task which can already be executed
     * @param obsoletes List of obsolete data versions
     */
    public ScheduleTasksRequest(List<Task> toSchedule, boolean waiting,
            int[] waitingCount, LinkedList<String> obsoletes) {
        super(TDRequestType.SCHEDULE_TASKS);
        this.toSchedule = toSchedule;
        this.waiting = waiting;
        this.waitingCount = waitingCount;
        this.obsoletes = obsoletes;
    }

    /** 
     * Returns the set of tasks ready to be executed
     * @return set of tasks ready to be executed
     */
    public List<Task> getToSchedule() {
        return toSchedule;
    }

    /**
     * Sets the tasks that are ready to be executed
     * @param toSchedule Set of tasks ready to be executed
     */
    public void setToSchedule(List<Task> toSchedule) {
        this.toSchedule = toSchedule;
    }

    /**
     * Returns true if the dependencies of the tasks were freed by the execution
     * of a previous task
     * @return True if the dependencies of the tasks were freed by the execution
     * of a previous task
     */
    public boolean getWaiting() {
        return waiting;
    }

    /**
     * Sets if the dependencies of the tasks were freed by the execution of a 
     * previous task
     * @param waiting True if the tasks were freed by the execution of a 
     * previous task
     */
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    /**
     * Returns how many tasks per core are waiting to the end of a task which 
     * can already be executed
     * @return how many tasks per core are waiting to the end of a task which 
     * can already be executed
     */
    public int[] getWaitingCount() {
        return waitingCount;
    }

    /**
     * Sets how many tasks per core are waiting to the end of a task which 
     * can already be executed
     * @param how many tasks per core are waiting to the end of a task which 
     * can already be executed
     */
    public void setWaitingCount(int[] waitingCount) {
        this.waitingCount = waitingCount;
    }

    /**
     * Returns a list of fileNames of the obsolete data version
     * @return a list of fileNames of the obsolete data version
     */
    public LinkedList<String> getObsoletes() {
        return obsoletes;
    }

    /**
     * Sets a list of fileNames of the obsolete data version
     * @param obsoletes list of fileNames of the obsolete data version
     */
    public void setObsoletes(LinkedList<String> obsoletes) {
        this.obsoletes = obsoletes;
    }
}
