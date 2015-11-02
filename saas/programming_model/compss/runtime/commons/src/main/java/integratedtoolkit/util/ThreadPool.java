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

package integratedtoolkit.util;

import java.util.concurrent.Semaphore;


/**
 * The threadpool is an utility to manage a set of threads
 */
public class ThreadPool {

    public static final int MAX_THREADS = 10;
    private final int size;
    private final String name;
    private final Thread[] workerThreads;
    private final RequestDispatcher<?> runObject;
    private final RequestQueue<?> queue;
    private final Semaphore sem;

    /**
     * Constructs a new threadpool but not the threads inside it. 
     * @param size number of threads that will be in the pool
     * @param name name of the thread pool inherited by the threads
     * @param runObject Request Dispatcher associated to the pool which 
     * implements the function executed by the threads
     */
    public ThreadPool(int size, String name, RequestDispatcher<?> runObject) {
        
        if (size > MAX_THREADS) {
            this.size = MAX_THREADS;
        }else{
            this.size = size;
        }
        this.workerThreads = new Thread[this.size];
        this.name = name;

        this.runObject = runObject;
        this.runObject.setPool(this);

        this.queue = runObject.getQueue();

        this.sem = new Semaphore(size);
    }

    /**
     * Creates and starts the threads of the pool and waits until they are 
     * created
     */
    public void startThreads(){
        int i = 0;
        for (Thread t : workerThreads) {
            t = new Thread(runObject);
            t.setName(name + " pool thread # " + i++);
            t.start();
        }

        sem.acquireUninterruptibly(size);
    }

    /**
     * Stops all the threads. Inserts as many null objects to the queue as 
     * threads are managed. It wakes up all the threads and wait until they 
     * process the null objects inserted which will stop them.
     */
    public void stopThreads(){
        /* Empty queue to discard any pending requests
         * and make threads finish
         */
        synchronized (queue) {
            for (int i = 0; i < size; i++) {
                queue.addToFront(null);
            }
            queue.wakeUpAll();
        }

        // Wait until all threads have completed their last request
        sem.acquireUninterruptibly(size);
    }

    /**
     * Notifies that one of the threads as completed an action required by the 
     * Threadpool (start or stop)
     */
    public void threadEnd() {
        sem.release();
    }

    /**
     * Returns the number of Threads in the pool
     * @return number of Threads in the pool
     */
    public int getNumThreads() {
        return workerThreads.length;
    }
}
