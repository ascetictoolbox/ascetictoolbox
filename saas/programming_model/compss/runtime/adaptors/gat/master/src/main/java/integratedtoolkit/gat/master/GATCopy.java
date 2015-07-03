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
package integratedtoolkit.gat.master;

import static integratedtoolkit.api.ITExecution.ParamType.FILE_T;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.data.Transferable;
import integratedtoolkit.types.data.operation.Copy.ImmediateCopy;
import integratedtoolkit.types.resources.Resource;
import java.io.File;
import java.util.LinkedList;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.FileInterface;

public class GATCopy extends ImmediateCopy {

    private static final String ERR_NO_TGT_URI = "No valid target URIs";
    private static final String ERR_NO_SRC_URI = "No valid source URIs";

    public GATCopy(LogicalData srcData, DataLocation prefSrc, DataLocation prefTgt, LogicalData tgtData, Transferable reason, EventListener listener) {
        super(srcData, prefSrc, prefTgt, tgtData, reason, listener);
        for (URI uri : prefTgt.getURIs()) {
            String path = uri.getPath();
            if (path.startsWith(File.separator)) {
                break;
            } else {
                Resource host = uri.getHost();
                this.tgtLoc = DataLocation.getLocation(host, host.getCompleteRemotePath(FILE_T, path));
            }
        }
    }

    @Override
    public void specificCopy() throws Exception {
        //Fetch valid destination URIs
        LinkedList<URI> targetURIs = tgtLoc.getURIs();
        LinkedList<org.gridlab.gat.URI> gatTargetUris = new LinkedList<org.gridlab.gat.URI>();
        for (URI uri : targetURIs) {
            org.gridlab.gat.URI internalURI = (org.gridlab.gat.URI) uri.getInternalURI(GATAdaptor.ID);
            if (internalURI != null) {
                gatTargetUris.add(internalURI);
            }
        }

        if (gatTargetUris.isEmpty()) {
            throw new GATCopyException(ERR_NO_TGT_URI);
        }

        //Fetch valid source URIs
        LinkedList<URI> sourceURIs;
        LinkedList<org.gridlab.gat.URI> gatSrcUris = new LinkedList<org.gridlab.gat.URI>();
        synchronized (srcData) {
            if (srcLoc != null) {
                sourceURIs = srcLoc.getURIs();
                for (URI uri : sourceURIs) {
                    org.gridlab.gat.URI internalURI = (org.gridlab.gat.URI) uri.getInternalURI(GATAdaptor.ID);
                    if (internalURI != null) {
                        gatSrcUris.add(internalURI);
                    }
                }
            }

            sourceURIs = srcData.getURIs();
            for (URI uri : sourceURIs) {
                org.gridlab.gat.URI internalURI = (org.gridlab.gat.URI) uri.getInternalURI(GATAdaptor.ID);
                if (internalURI != null) {
                    gatSrcUris.add(internalURI);
                }
            }

            if (gatSrcUris.isEmpty()) {
                if (srcData.isInMemory()) {
                    try {
                        srcData.writeToFile();
                        sourceURIs = srcData.getURIs();
                        for (URI uri : sourceURIs) {
                            org.gridlab.gat.URI internalURI = (org.gridlab.gat.URI) uri.getInternalURI(GATAdaptor.ID);
                            if (internalURI != null) {
                                gatSrcUris.add(internalURI);
                            }
                        }
                    } catch (Exception e) {
                        logger.fatal("Exception  writing object to file.", e);
                        throw new GATCopyException(ERR_NO_SRC_URI);
                    }
                } else {
                    throw new GATCopyException(ERR_NO_SRC_URI);
                }
            }
        }

        GATInvocationException exception = new GATInvocationException("default logical file");
        for (org.gridlab.gat.URI src : gatSrcUris) {
            for (org.gridlab.gat.URI tgt : gatTargetUris) {
                try {
                    doCopy(src, tgt);
                    // Try to copy from each location until successful
                } catch (Exception e) {
                    exception.add("default logical file", e);
                    continue;
                }
                return;
            }
        }
        throw exception;
    }

    private static void doCopy(org.gridlab.gat.URI src, org.gridlab.gat.URI dest) throws Exception {
        // Try to copy from each location until successful
        FileInterface f = null;
        f = org.gridlab.gat.GAT.createFile(GATAdaptor.context, src).getFileInterface();
        f.copy(dest);
    }

    private static class GATCopyException extends Exception {

        public GATCopyException(String msg) {
            super(msg);
        }
    }
}
