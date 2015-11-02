/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.connectors.utils;

import integratedtoolkit.connectors.VM;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.resources.CloudMethodWorker;
import integratedtoolkit.types.resources.ShutdownListener;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import integratedtoolkit.util.ResourceManager;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class DeletionThread extends Thread {

    private final Operations operations;
    private final CloudMethodWorker worker;
    private final CloudMethodResourceDescription reduction;
    private VM vm;
    private static Integer count = 0;

    private static final Logger logger = Logger.getLogger(Loggers.CONNECTORS);
    private static final boolean debug = logger.isDebugEnabled();

    public DeletionThread(Operations connector, CloudMethodWorker worker, CloudMethodResourceDescription reduction) {
        this.setName("DeletionThread " + worker.getName());
        this.operations = connector;
        synchronized (count) {
            count++;
        }
        this.worker = worker;
        this.reduction = reduction;
        this.vm = null;
    }

    public DeletionThread(Operations connector, VM vm) {
        this.setName("DeletionThread " + vm.getName());
        this.operations = connector;
        synchronized (count) {
            count++;
        }
        this.worker = null;
        this.reduction = null;
        this.vm = vm;
    }

    public static int getCount() {
        return count;
    }

    public void run() {
        if (reduction != null) {
            Semaphore sem = ResourceManager.reduceCloudWorker(worker, reduction);
            try {
                if (sem != null) {
                    sem.acquire();
                }
            } catch (InterruptedException e) {
            }
            this.vm = this.operations.pause(worker);
        }
        if (vm != null) {
            CloudMethodWorker worker = vm.getWorker();
            if (worker.shouldBeStopped()) {
                Semaphore sem = new Semaphore(0);
                ShutdownListener sl = new ShutdownListener(sem);
                worker.stop(true, sl);
                sl.enable();
                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    //Interrupted. No need to do anything.
                }
            }
            try {
                this.operations.poweroff(vm);
            } catch (Exception e) {
                if (debug) {
                    logger.error("", e);
                } else {
                    logger.error("");
                }
            }
        }
        synchronized (count) {
            count--;
        }
    }
}
