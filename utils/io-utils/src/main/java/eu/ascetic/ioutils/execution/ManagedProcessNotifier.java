/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.ioutils.execution;

import java.util.HashSet;

/**
 * This waits upon a process and indicates its completion to a set of 
 * managed process listeners.
 * @author Richard Kavanagh
 */
public class ManagedProcessNotifier {

    private ManagedProcess process;
    private NotifierThread notifier;
    private HashSet<ManagedProcessListener> listening = new HashSet<>();

    /**
     * The no-args constructor made private to stop its use.
     */
    private ManagedProcessNotifier() {
    }

    /**
     * The default constructor for the managed process notifier.
     * @param process
     */
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public ManagedProcessNotifier(ManagedProcess process) {
        this.process = process;
    }

    /**
     * This registers an interested party to the event of the process finishing.
     * Notifications to the completion are sent in an unordered fashion.
     * @param listener The listener that is to be registered.
     */
    public synchronized void listen(ManagedProcessListener listener) {
        if (listener != null) {
            listening.add(listener);
        }
        /**
         * start a new notifier thread upon first adding something to the set
         * of listening objects.
         */
        if (notifier == null) {

            notifier = new NotifierThread(this);
            new Thread(notifier).start();
        }
    }

    /**
     * This removes a listener from the list of interested parties to the end
     * of the processes execution.
     * @param listener The listener to deregister
     */
    public synchronized void stoplistening(ManagedProcessListener listener) {
        listening.remove(listener);
    }

    /**
     * This removes all listeners from the list of interested parties to the end
     * of the processes execution.
     */
    public synchronized void stoplistening() {
        listening.clear();
    }

    /**
     * This alerts all current listeners to the completion of the task event.
     */
    public synchronized void alert() {
        for (ManagedProcessListener element : listening) {
            element.receiveProcessFinishedEvent(getProcess());
        }
    }

    /**
     * @return the process
     */
    public ManagedProcess getProcess() {
        return process;
    }
}
