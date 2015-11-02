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

package integratedtoolkit.connectors.test;

import integratedtoolkit.connectors.AbstractConnector;
import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Test extends AbstractConnector {

    private static AtomicInteger nextId = new AtomicInteger(100);

    public Test(String providerName, HashMap<String, String> props) {
        super(providerName, props);
    }

    @Override
    public Object create(String name, CloudMethodResourceDescription rd) throws ConnectorException {
        TestEnvId envId = new TestEnvId();
        System.out.println("Es demana la creació de la màquina " + rd + "que correspon a " + envId);
        return envId;
    }

    @Override
    public CloudMethodResourceDescription waitUntilCreation(Object vm, CloudMethodResourceDescription requested) throws ConnectorException {
        try{
            Thread.sleep(15000);
        }catch(Exception e){}
        System.out.println("Esperem a que acabi de crear-se " + vm);
        CloudMethodResourceDescription granted = new CloudMethodResourceDescription(requested);
        granted.setName("127.0.0." + nextId.getAndIncrement());
        granted.setOperatingSystemType(requested.getImage().getOperativeSystem());
        return granted;
    }

    @Override
    public float getMachineCostPerTimeSlot(CloudMethodResourceDescription rd) {
        return 0.0f;
    }

    @Override
    public long getTimeSlot() {
        return ONE_HOUR;
    }

    @Override
    public void destroy(Object envId) throws ConnectorException {
        System.out.println("S'esta destruint " + envId);
    }

    @Override
    public void configureAccess(String IP, String user, String password) throws ConnectorException {
        System.out.println("Otorgant accés al master a la màquina "+IP);
    }

    @Override
    public void prepareMachine(String IP, CloudImageDescription cid) throws ConnectorException {
        System.out.println("Copiant tota la informació necessaria a "+IP);
    }

    private static class TestEnvId {

        private static AtomicInteger nextId = new AtomicInteger(0);
        private int id = nextId.getAndIncrement();

        public String toString() {
            return "TestEventId:" + id;
        }
    }
}
