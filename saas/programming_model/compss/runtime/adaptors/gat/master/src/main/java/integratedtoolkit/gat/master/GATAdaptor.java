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

package integratedtoolkit.gat.master;

import java.net.URISyntaxException;

import org.gridlab.gat.GATContext;

import java.util.LinkedList;

import integratedtoolkit.ITConstants;
import integratedtoolkit.comm.CommAdaptor;
import integratedtoolkit.comm.Dispatcher;
import integratedtoolkit.types.data.operation.Copy;
import integratedtoolkit.types.data.operation.DataOperation;
import integratedtoolkit.util.RequestQueue;
import integratedtoolkit.util.ThreadPool;
import java.io.File;
import java.util.HashMap;

public class GATAdaptor implements CommAdaptor {

    public static final String ID = GATAdaptor.class.getCanonicalName();

    private static final int GAT_POOL_SIZE = 5;
    protected static final String POOL_NAME = "FTM";
    protected static final String SAFE_POOL_NAME = "SAFE_FTM";
    protected static final String THREAD_POOL_ERR = "Error starting pool of threads";
    protected static final String POOL_ERR = "Error deleting pool of threads";
    protected static final int SAFE_POOL_SIZE = 1;

    // Copy request queues
    // copyQueue is for ordinary copies
    // safeQueue is for priority copies
    public static RequestQueue<DataOperation> copyQueue;
    public static RequestQueue<DataOperation> safeQueue;

    protected static ThreadPool pool;
    protected static ThreadPool safePool;

    private static String masterUser = System.getProperty("user.name");
    // GAT context
    public static GATContext context;

    public GATAdaptor() {

    }

    public void init() {
        // Create request queues
        copyQueue = new RequestQueue<DataOperation>();
        safeQueue = new RequestQueue<DataOperation>();

        String adaptor = System.getProperty(ITConstants.GAT_FILE_ADAPTOR);

        GATJob.init();

        pool = new ThreadPool(GAT_POOL_SIZE, POOL_NAME, new Dispatcher(copyQueue));
        try {
            pool.startThreads();
        } catch (Exception e) {
            logger.error(THREAD_POOL_ERR, e);
            System.exit(1);
        }

        safePool = new ThreadPool(SAFE_POOL_SIZE, SAFE_POOL_NAME, new Dispatcher(safeQueue));
        try {
            safePool.startThreads();
        } catch (Exception e) {
            logger.error(THREAD_POOL_ERR, e);
            System.exit(1);
        }

        // GAT adaptor path
        logger.debug("Initializing GAT Context");
        context = new GATContext();

        /* We need to try the local adaptor when both source and target hosts
         * are local, because ssh file adaptor cannot perform local operations
         */
        context.addPreference("File.adaptor.name", adaptor + ", srcToLocalToDestCopy, local");

    }

    // GAT adaptor initializes the worker each time it sends a new job
    @Override
    public GATWorkerNode initWorker(String name, HashMap<String, String> properties) {
        GATWorkerNode node = new GATWorkerNode(name, properties);
        return node;
    }

    public static void addFileAdaptorPreferences(String name, String value) {
        context.addPreference(name, value);
    }

    public LinkedList<DataOperation> getPending() {
        LinkedList<DataOperation> l = new LinkedList<DataOperation>();

        for (DataOperation c : copyQueue.getQueue()) {
            l.add(c);
        }
        for (DataOperation c : safeQueue.getQueue()) {
            l.add(c);
        }
        return l;
    }

    @Override
    public void stop() {
        // Make pool threads finish
        try {
            pool.stopThreads();
            safePool.stopThreads();
        } catch (Exception e) {
            logger.error(POOL_ERR, e);
        }

        GATJob.end();
    }

    @Override
    public void stopSubmittedJobs() {

    }

    @Override
    public void completeMasterURI(integratedtoolkit.types.data.location.URI uri) {
        String scheme = uri.getScheme();
        String user = masterUser + "@";
        String host = uri.getHost().getName();
        String path = uri.getPath();
        if (!path.contains(File.separator)) {
            return;
        }

        String s = (scheme
                + user
                + host + "/"
                + path);
        try {
            uri.setInternalURI(ID, new org.gridlab.gat.URI(s));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void enqueueCopy(Copy c) {
        copyQueue.enqueue(c);
    }
}
