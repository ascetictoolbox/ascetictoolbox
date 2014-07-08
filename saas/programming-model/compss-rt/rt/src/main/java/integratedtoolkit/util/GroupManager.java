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

package integratedtoolkit.util;

import java.util.Map;
import java.util.TreeMap;


import integratedtoolkit.components.FileTransfer.FileRole;
import java.util.concurrent.Semaphore;

/* Class that allows creation and management of groups
 * Modifications (addition and deletion of groups) must be synchronized
 */
public class GroupManager {

    private static final int FIRST_GROUP_ID = 1;
    /**
     * Pending operations associated to each group
     */
    private Map<Integer, Integer> groups;
    /**
     * Objective of the operations associated to each group
     */
    private Map<Integer, FileRole> roles;
    /**
     * Amount of failures produced on operations of each group
     */
    private Map<Integer, Integer> failures;
    /**
     * Amount of failures produced on operations of each group
     */
    private Map<Integer, Semaphore> semaphores;
    /**
     * Id for the next group
     */
    private int nextGroupId;

    /**
     * Constructs a new Group Manager
     */
    public GroupManager() {
        this.groups = new TreeMap<Integer, Integer>();
        this.roles = new TreeMap<Integer, FileRole>();
        this.semaphores = new TreeMap<Integer, Semaphore>();
        this.failures = new TreeMap<Integer, Integer>();
        this.nextGroupId = FIRST_GROUP_ID;
    }

    /**
     * Adds a new group with id 0 and no role
     *
     * @return 0
     */
    public int addGroup() {
        return addGroup(0, FileRole.NO_ROLE, null);
    }

    /**
     * Adds a new group
     *
     * @param numberOfMembers amount of operations to complete
     * @param fr Goal of the operations in the group
     * @return the Id of the group
     */
    public synchronized int addGroup(int numberOfMembers, FileRole fr, Semaphore sem) {
        groups.put(nextGroupId, numberOfMembers);
        roles.put(nextGroupId, fr);
        semaphores.put(nextGroupId, sem);
        failures.put(nextGroupId, 0);
        return nextGroupId++;
    }

    /**
     * Get the amount of operations still pending for a group
     *
     * @param groupId identifier of the group
     * @return the amount of pending operations to complete
     * @throws ElementNotFoundException The group does not exist
     */
    public synchronized int getGroupMembers(int groupId) throws ElementNotFoundException {
        Integer n;
        if ((n = groups.get(groupId)) == null) {
            throw new ElementNotFoundException();
        }

        return n;
    }

    /**
     * Removes a group
     *
     * @param groupId identifier of the group to delete
     * @return the goal for the operations in the group
     */
    public synchronized FileRole removeGroup(int groupId) {
        groups.remove(groupId);
        failures.remove(groupId);
        Semaphore sem = semaphores.remove(groupId);
        if (sem != null) {
            sem.release();
        }
        return roles.remove(groupId);
    }

    /**
     * Adds one operation to one group
     *
     * @param groupId identifier of the group
     * @throws ElementNotFoundException There is no group with that identifier
     */
    public void addMember(int groupId) throws ElementNotFoundException {
        addMembers(groupId, 1);
    }

    /**
     * Adds many operations to a group
     *
     * @param groupId identifier of the group
     * @param numberOfMembers nomber of operations to add
     * @throws ElementNotFoundException There's no group with that identifier
     */
    public synchronized void addMembers(int groupId, int numberOfMembers)
            throws ElementNotFoundException {
        Integer n;
        if ((n = groups.get(groupId)) == null) {
            throw new ElementNotFoundException();
        }

        groups.put(groupId, n + numberOfMembers);
    }

    /**
     * Gets the goal of the operations in that group
     *
     * @param groupId identifier of the group
     * @return the goal of the operations in the group
     */
    public FileRole getRole(int groupId) {
        return roles.get(groupId);
    }

    /**
     * Removes a pending operation and adds a failure to the group
     *
     * @param groupId identifier of the group
     * @return amount of pending operations after removing that one
     * @throws ElementNotFoundException
     */
    public int failedMember(int groupId) throws ElementNotFoundException {
        failures.put(groupId, failures.get(groupId) + 1);
        return removeMembers(groupId, 1);
    }

    /**
     * Checks if there is any failed operation for a group
     *
     * @param groupId identifier of the group
     * @return true if there is any failed operations
     */
    public int hasFailedMembers(int groupId) {
        return failures.get(groupId);
    }

    /**
     * Removes a pending operation from a group
     *
     * @param groupId identifier of the group
     * @return amout of pending operations after removing that one
     * @throws ElementNotFoundException There's no group with that id
     */
    public int removeMember(int groupId) throws ElementNotFoundException {
        return removeMembers(groupId, 1);
    }

    /**
     * Removes many pending operations from that group
     *
     * @param groupId identifier of the group
     * @param numberOfMembers identifier of the group
     * @return amount of pending operations after removing that ones
     * @throws ElementNotFoundException There's no group with that id
     */
    public synchronized int removeMembers(int groupId, int numberOfMembers)
            throws ElementNotFoundException {
        Integer n;
        if ((n = groups.get(groupId)) == null) {
            throw new ElementNotFoundException();
        }

        if (n > numberOfMembers) {
            n -= numberOfMembers;
        } else {
            n = 0;
        }

        groups.put(groupId, n);
        return n;
    }

    /**
     * Modifies the goal of the operations in the group
     *
     * @param groupId identifier of the group
     * @param fr new goal for the operations
     */
    public synchronized void setGroupFileRole(Integer groupId, FileRole fr) {
        roles.put(groupId, fr);
    }

    public void setSemaphore(Integer gId, Semaphore sem) {
        semaphores.put(gId, sem);
    }

    /**
     * Checks if a group exists
     *
     * @param groupId identifier of the group
     * @return true if there is any group with that identifier
     */
    public synchronized boolean exists(int groupId) {
        return groups.containsKey(groupId);
    }

    /**
     * Checks if a group has any pending operation
     *
     * @param groupId
     * @return
     * @throws ElementNotFoundException
     */
    public synchronized boolean hasMembers(int groupId)
            throws ElementNotFoundException {
        Integer n;
        if ((n = groups.get(groupId)) == null) {
            throw new ElementNotFoundException();
        }

        return n > 0;
    }

    /**
     * Removes all the groups and reinitializes the Group manager
     */
    public synchronized void clear() {
        groups.clear();
        roles.clear();
        failures.clear();
        nextGroupId = FIRST_GROUP_ID;
    }
}
