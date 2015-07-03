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
package integratedtoolkit.types.data.operation;

import integratedtoolkit.api.ITExecution.ParamType;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.Transferable;
import integratedtoolkit.types.resources.Resource;

public abstract class Copy extends DataOperation {

    protected final LogicalData srcData;
    protected final DataLocation srcLoc;
    protected final LogicalData tgtData;
    protected DataLocation tgtLoc;
    protected final Transferable reason;

    public Copy(LogicalData srcData, DataLocation prefSrc, DataLocation prefTgt, LogicalData tgtData, Transferable reason, EventListener listener) {
        super(srcData, listener);
        this.srcData = srcData;
        this.srcLoc = prefSrc;
        this.tgtData = tgtData;
        this.tgtLoc = prefTgt;
        this.reason = reason;
    }

    public LogicalData getSourceData() {
        return srcData;
    }

    public DataLocation getPreferredSource() {
        return srcLoc;
    }

    public DataLocation getTargetLoc() {
        return tgtLoc;
    }

    public LogicalData getTargetData() {
        return tgtData;
    }

    public boolean isRegistered() {
        return tgtData != null;
    }

    public void setProposedSource(Object source) {
        reason.setDataSource(source);
    }

    public void setFinalTarget(String targetAbsolutePath) {
        reason.setDataTarget(targetAbsolutePath);
    }

    public String getFinalTarget() {
        return reason.getDataTarget();
    }

    public ParamType getType() {
        return reason.getType();
    }

    public static class DeferredCopy extends Copy {

        public DeferredCopy(LogicalData srcData, DataLocation prefSrc, DataLocation prefTgt, LogicalData tgtData, Transferable reason, EventListener listener) {
            super(srcData, prefSrc, prefTgt, tgtData, reason, listener);
        }
    }

    public abstract static class ImmediateCopy extends Copy {

        public ImmediateCopy(LogicalData ld, DataLocation prefSrc, DataLocation prefTgt, LogicalData tgtData, Transferable reason, EventListener listener) {
            super(ld, prefSrc, prefTgt, tgtData, reason, listener);
        }

        public void perform() {
            Resource targetHost = tgtLoc.getHosts().getFirst();
            logger.debug("THREAD " + Thread.currentThread().getName() + " - Copy file " + getName() + " to " + tgtLoc);
            synchronized (srcData) {
                if (tgtData != null) {
                    URI u;
                    if ((u = srcData.alreadyAvailable(targetHost)) != null) {
                        setFinalTarget(u.getPath());
                        end(DataOperation.OpEndState.OP_OK);
                        logger.debug("THREAD " + Thread.currentThread().getName() + " - A copy of " + getName() + " is already present at " + targetHost + " on path " + u.getPath());
                        return;
                    }
                    Copy copyInProgress = null;
                    if ((copyInProgress = srcData.alreadyCopying(tgtLoc)) != null) {
                        String path = copyInProgress.tgtLoc.getURIInHost(targetHost).getPath();
                        setFinalTarget(path);
                        // The same operation is already in progress - no need to repeat it
                        end(DataOperation.OpEndState.OP_IN_PROGRESS);

                        // This group must be notified as well when the operation finishes
                        synchronized (copyInProgress.getEventListeners()) {
                            copyInProgress.addEventListeners(getEventListeners());
                        }
                        logger.debug("THREAD " + Thread.currentThread().getName() + " - A copy to " + path + " is already in progress, skipping replication");
                        return;
                    }
                }
                srcData.startCopy(this, tgtLoc);
            }
            try {
                specificCopy();
                //ld.replicate(targetURI);
            } catch (Exception e) {
                end(DataOperation.OpEndState.OP_FAILED, e);
                return;
            } finally {
                DataLocation actualLocation;
                synchronized (srcData) {
                    actualLocation = srcData.finishedCopy(this);
                }
                if (tgtData != null) {
                    synchronized (tgtData) {
                        tgtData.addLocation(actualLocation);
                    }
                }
            }
            String path = tgtLoc.getURIInHost(targetHost).getPath();
            setFinalTarget(path);
            synchronized (srcData) {
                end(DataOperation.OpEndState.OP_OK);
            }
        }

        public abstract void specificCopy() throws Exception;
    }

}
