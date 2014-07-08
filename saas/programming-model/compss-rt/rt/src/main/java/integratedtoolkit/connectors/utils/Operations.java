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
package integratedtoolkit.connectors.utils;

import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.ProjectWorker;
import integratedtoolkit.types.ResourceDescription;
import java.util.LinkedList;

public interface Operations {

    public static final Object knownHosts = new Object();

    //Power on a new Machine
    public Object poweron(String name, ResourceDescription rd)
            throws ConnectorException;

    public ResourceDescription waitCreation(Object vm, ResourceDescription request)
            throws ConnectorException;

    //Allow access from master and between VM
    public void configureAccess(String IP, String user)
            throws ConnectorException;

    public void announceCreation(String IP, String user, LinkedList<ProjectWorker> existingIPs)
            throws ConnectorException;

    //Prepare Machine to run tasks
    public void prepareMachine(String IP, CloudImageDescription cid)
            throws ConnectorException;

    //Shutdown an existing machine
    public void poweroff(ResourceDescription rd)
            throws ConnectorException;

    public void announceDestruction(String IP, LinkedList<ProjectWorker> existingIPs)
            throws ConnectorException;

    //Data needed to check if vm are useful
    public boolean getTerminate();

    public boolean getCheck();

    public void destroy(Object vm)
            throws ConnectorException;
}
