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

package integratedtoolkit.ascetic;

import org.apache.commons.httpclient.*;
import java.util.concurrent.TimeUnit;


public class ApplicationMonitor {
    
    private static final String endpoint = Configuration.getApplicationMonitorEndpoint();

    
    
    public static void startEvent(){
    //curl -X POST -H "Content-Type: application/json" --data-binary '{"appId":"GreenPrefab", "nodeId":"WorkerNode1234", "instanceId":"1234adb1234", "data":{"qualsevol":"merda que vulguis (opcional)"}}' http://localhost:9000/event
    }
    
    public static void stopEvent(){
        
    }

}
