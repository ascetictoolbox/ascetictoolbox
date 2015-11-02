/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.ascetic.test;

import integratedtoolkit.ITConstants;
import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.ascetic.Configuration;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.components.ResourceUser;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.MethodImplementation;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ResourceManager;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Test {

    public static void main(String[] args) {
        ConsoleAppender console = new ConsoleAppender();
        Logger.getRootLogger().setLevel(Level.INFO);
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
        Comm.init();
        CoreManager.resizeStructures(1);

        Implementation[] implementations = new Implementation[2];
        String[] signatures = new String[]{
            "runEPlusJob(FILE_T, FILE_T, FILE_T, STRING_T, STRING_T, STRING_T, STRING_T, FILE_T, FILE_T)jeplus.worker.JEPlusImpl",
            "runEPlusJob(FILE_T, FILE_T, FILE_T, STRING_T, STRING_T, STRING_T, STRING_T, FILE_T, FILE_T)jeplus.worker.JEPlusImplOptimized"
        };

        Integer coreId = CoreManager.getCoreId(signatures);
        if (coreId == CoreManager.getCoreCount()) {
            CoreManager.increaseCoreCount();
        }

        implementations[0] = new MethodImplementation("jeplus.worker.JEPlusImpl", coreId, 0, new MethodResourceDescription());
        implementations[1] = new MethodImplementation("jeplus.worker.JEPlusImplOptimized", coreId, 1, new MethodResourceDescription());

        CoreManager.registerImplementations(coreId, implementations, signatures);

        String contextLocation = "/home/flordan/ascetic/testbed/master/ascetic_service";
        System.setProperty(ITConstants.IT_CONTEXT, contextLocation);

        String projXML = "/home/flordan/ascetic/testbed/master/ascetic_service/project.xml";
        System.setProperty(ITConstants.IT_PROJ_FILE, projXML);
        String resXML = "/home/flordan/ascetic/testbed/master/ascetic_service/resources.xml";
        System.setProperty(ITConstants.IT_RES_FILE, resXML);

        String projXSD = "/home/flordan/ascetic/testbed/master/ascetic_service/project_schema.xsd";
        System.setProperty(ITConstants.IT_PROJ_SCHEMA, projXSD);
        String resXSD = "/home/flordan/ascetic/testbed/master/ascetic_service/resource_schema.xsd";
        System.setProperty(ITConstants.IT_RES_SCHEMA, resXSD);
        try {
            ResourceManager.load(new Runtime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("App Id=" + Configuration.getApplicationId());
        System.out.println("Deployment Id=" + Configuration.getDeploymentId());
        System.out.println("App Manager Endpoint=" + Configuration.getApplicationManagerEndpoint());
        System.out.println("App Monitoring Endpoint=" + Configuration.getApplicationMonitorEndpoint());

        System.out.println("Economic Boundary" + Ascetic.getEconomicalBoundary());
        System.out.println("Energy Boundary" + Ascetic.getEnergyBoundary());

        while (true) {
            Ascetic.discoverNewResources();
            Ascetic.updateConsumptions();
            //System.out.println(ResourceManager.getCurrentState(""));
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
            }
        }
    }

    private static class Runtime implements ResourceUser {

        @Override
        public void createdResources(Worker r) {
            System.out.println("S'ha afegit el recurs " + r.getName() + " al runtime.  Ara se li assignarien tasques");
        }

        @Override
        public WorkloadStatus getWorkload() {
            return new WorkloadStatus(CoreManager.getCoreCount());
        }

        @Override
        public void updatedConsumptions(Worker r) {
            System.out.println("Les característiques dels consums pel worker " + r.getName() + ". Se li poden assignar noves tasques?");
        }

    }
}
