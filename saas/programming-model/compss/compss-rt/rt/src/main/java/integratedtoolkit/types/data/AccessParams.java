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

import java.io.Serializable;

import integratedtoolkit.components.DataAccess.AccessMode;


//Parameters of access to a file
public class AccessParams implements Serializable {

	private AccessMode mode;

	public AccessParams(AccessMode mode) {
		this.mode = mode;
	}

    public AccessMode getMode() {
        return mode;
    }
      

    // File access
    public static class FileAccessParams extends AccessParams {
        private String name;
        private String path;
        private String host;
        
        public FileAccessParams(AccessMode mode, String name, String path, String host) {
            super(mode);
            this.name = name;
            this.path = path;
            this.host = host;
        }

        public String getName() {
        	return name;
        }

        public String getPath() {
            return path;
        }
        
        public String getHost() {
            return host;
        }
        
    }
    
    // Object access
    public static class ObjectAccessParams extends AccessParams {
        private int hashCode;
        private Object value;

        public ObjectAccessParams(AccessMode mode, Object value, int hashCode) {
            super(mode);
            this.value = value;
            this.hashCode = hashCode;
        }

        public Object getValue() {
        	return value;
        }
        
        public int getCode() {
            return hashCode;
        }
    }

}
