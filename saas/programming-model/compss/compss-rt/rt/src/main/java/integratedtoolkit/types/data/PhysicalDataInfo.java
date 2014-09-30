/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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

package integratedtoolkit.types.data;

import integratedtoolkit.util.Serializer;
import org.gridlab.gat.io.LogicalFile;


public class PhysicalDataInfo {

        Object value;
        LogicalFile logicalFile;

        public PhysicalDataInfo(Object value){
            this.value=value;
            this.logicalFile=null;
        }
        public PhysicalDataInfo(LogicalFile logicalFile){
            this.value=null;
            this.logicalFile=logicalFile;
        }

        public PhysicalDataInfo(Object value, LogicalFile logicalFile){
            this.value=value;
            this.logicalFile=logicalFile;
        }

        public PhysicalDataInfo(){
            this.value=null;
            this.logicalFile=null;
        }


        public boolean isInMemory(){
            return this.value!=null;
        }

        public Object getValue(){
            return this.value;
        }

        public LogicalFile getLogicalFile(){
            return this.logicalFile;
        }

        public void writeToFile() throws Exception{
            Serializer.serialize(value, logicalFile.getURIs().get(0).getPath());
            value = null;
        }
}
