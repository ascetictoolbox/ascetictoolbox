/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
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



package eu.ascetic.paas.slam.poc.provision;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.slasoi.gslam.core.context.SLAManagerContext;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.slam.poc.exceptions.RequestNotCorrectException;
import eu.ascetic.paas.slam.poc.optimization.RequestProcessor;

import datastructure.Request;

import slaparser.SLAParser;
import utils.Constant;

public class RunnerImpl{
    private SLATemplate slat;
    private static final Logger LOGGER = Logger.getLogger(RunnerImpl.class);
    public static SLAManagerContext context;

    public RunnerImpl(SLATemplate sla, SLAManagerContext context) {
        this.slat = sla;
        RunnerImpl.context = context;
    }

    /**
     * Starts to run.
     */
    public SLATemplate run() {
        SLAParser parser = new SLAParser(this.slat);
        
        ArrayList<LinkedHashMap<String, Request>> paths = new ArrayList<LinkedHashMap<String, Request>>();
        paths.add(parser.getResourceRequest());
        LOGGER.info("=============Generated all the paths that might to lead to a successful deal, the number of the paths is : "+paths.size()+"=================");
        // plan + query ISM + reservation
        RequestProcessor mainProvider = new RequestProcessor(Constant.Main_Provider);
        LOGGER.info("Starting to analyze each path...");
            for (LinkedHashMap<String, Request> path : paths) {
                try {
                    mainProvider.startProcess(path.values());
                }
                catch (RequestNotCorrectException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        return this.slat;
    }
}
