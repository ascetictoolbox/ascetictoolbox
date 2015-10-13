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

package integratedtoolkit.api;

public interface ITExecution {

    // Parameter types
    public enum ParamType {

        FILE_T,
        BOOLEAN_T,
        CHAR_T,
        STRING_T,
        BYTE_T,
        SHORT_T,
        INT_T,
        LONG_T,
        FLOAT_T,
        DOUBLE_T,
        OBJECT_T;
    }

    // Parameter directions
    public enum ParamDirection {

        IN,
        OUT,
        INOUT;
    }

    // Method
    public int executeTask(Long appId,
            String methodClass,
            String methodName,
            boolean priority,
            boolean hasTarget,
            int parameterCount,
            Object... parameters);

    // Service
    public int executeTask(Long appId,
            String namespace,
            String service,
            String port,
            String operation,
            boolean priority,
            boolean hasTarget,
            int parameterCount,
            Object... parameters);

    public void noMoreTasks(Long appId, boolean terminate);

}
