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

import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.connectors.VM;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.resources.CloudMethodWorker;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;

public interface Operations {

    public static final Object knownHosts = new Object();

    //Power on a new Machine
    public Object poweron(String name, CloudMethodResourceDescription rd)
            throws ConnectorException;

    public void destroy(Object envId)
            throws ConnectorException;

    public VM waitCreation(Object envId, CloudMethodResourceDescription request)
            throws ConnectorException;

    //Allow access from master and between VM
    public void configureAccess(String IP, String user, String password)
            throws ConnectorException;

    //Prepare Machine to run tasks
    public void prepareMachine(String IP, CloudImageDescription cid)
            throws ConnectorException;

    //Notification that the vm is available and fully operative
    public void vmReady(VM vm) throws ConnectorException;

    //Shutdown an existing machine
    public void poweroff(VM rd)
            throws ConnectorException;

    public VM pause(CloudMethodWorker worker);

    //Data needed to check if vm are useful
    public boolean getTerminate();

    public boolean getCheck();

}
