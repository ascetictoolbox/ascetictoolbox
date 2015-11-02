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
package eu.ascetic.utils.execution;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is aimed to be a thread that waits for a process to complete.
 * It then notifies all registered listeners to its completion.
 *
 *  * @author Richard Kavanagh
 */
public class NotifierThread implements Runnable {

    private ManagedProcessNotifier parent;

    /**
     * The default thread notifier constructor
     * @param parent The managed process notifier starting this thread.
     */
    public NotifierThread(ManagedProcessNotifier parent) {
        this.parent = parent;
    }

    /**
     * The method that waits for the external process to end.
     */
    @Override
    public void run() {
        try {
            parent.getProcess().getProcess().waitFor(); //Wait until the job is done, if already done progress straight away.
            parent.alert();
        } catch (InterruptedException ex) {
            Logger.getLogger(ManagedProcessNotifier.class.getName()).log(Level.INFO, "A process was interupted", ex);
        }
    }
}
