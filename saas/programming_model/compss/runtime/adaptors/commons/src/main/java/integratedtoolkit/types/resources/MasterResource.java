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

package integratedtoolkit.types.resources;

import integratedtoolkit.ITConstants;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.comm.CommAdaptor;
import integratedtoolkit.types.COMPSsMaster;
import integratedtoolkit.types.data.location.URI;
import java.io.File;

public class MasterResource extends Resource {

    protected static final String ERROR_COMPSs_LOG_BASE_DIR = "ERROR: Cannot create .COMPSs base log directory";
    protected static final String ERROR_APP_LOG_DIR = "ERROR: Cannot create application log directory";
    protected static final String ERROR_TEMP_DIR = "ERROR: Cannot create temp directory";
    protected static final String ERROR_JOBS_DIR = "ERROR: Cannot create jobs directory";
    protected static final String WARN_FOLDER_OVERLOAD = "WARNING: Reached maximum number of executions for this application. Overwriting entry _01. To avoid this warning please clean .COMPSs folder";

    protected static final int MAX_OVERLOAD = 50;

    private final String userExecutionDirPath;

    private final String COMPSsLogBaseDirPath;
    private static String appLogDirPath;
    private static String tempDirPath;
    private static String jobsDirPath;

    public MasterResource() {
        super(new COMPSsMaster());

        //Gets user execution directory
        userExecutionDirPath = System.getProperty("user.dir");

        //Creates base Runtime structure directories
        COMPSsLogBaseDirPath = System.getProperty("user.home") + File.separator + ".COMPSs" + File.separator;

        if (!new File(COMPSsLogBaseDirPath).exists()) {
            if (!new File(COMPSsLogBaseDirPath).mkdir()) {
                System.err.println(ERROR_COMPSs_LOG_BASE_DIR);
                System.exit(1);
            }
        }

        //Load working directory. Different for regular applications and services
        String appName = System.getProperty(ITConstants.IT_APP_NAME);
        if (System.getProperty(ITConstants.IT_SERVICE_NAME) != null) {
            /* SERVICE
             * - Gets appName
             * - Overloads the service folder for different executions
             * - MAX_OVERLOAD raises warning
             * - Changes working directory to serviceName !!!!
             */
            String serviceName = System.getProperty(ITConstants.IT_SERVICE_NAME);
            int overloadCode = 1;
            appLogDirPath = COMPSsLogBaseDirPath + serviceName + "_0" + String.valueOf(overloadCode) + File.separator;
            while ((new File(appLogDirPath).exists()) && (overloadCode <= MAX_OVERLOAD)) {
                overloadCode = overloadCode + 1;
                if (overloadCode < 10) {
                    appLogDirPath = COMPSsLogBaseDirPath + serviceName + "_0" + String.valueOf(overloadCode) + File.separator;
                } else {
                    appLogDirPath = COMPSsLogBaseDirPath + serviceName + "_" + String.valueOf(overloadCode) + File.separator;
                }
            }
            if (overloadCode > MAX_OVERLOAD) {
                System.err.println(WARN_FOLDER_OVERLOAD);
                appLogDirPath = COMPSsLogBaseDirPath + serviceName + "_01" + File.separator;
            }
            if (!new File(appLogDirPath).mkdir()) {
                System.err.println(ERROR_APP_LOG_DIR);
                System.exit(1);
            }
            System.setProperty(ITConstants.IT_APP_LOG_DIR, appLogDirPath);
        } else {
            /* REGULAR APPLICATION
             * - Gets appName
             * - Overloads the app folder for different executions
             * - MAX_OVERLOAD raises warning
             * - Changes working directory to appName !!!!
             */
            int overloadCode = 1;
            appLogDirPath = COMPSsLogBaseDirPath + appName + "_0" + String.valueOf(overloadCode) + File.separator;
            while ((new File(appLogDirPath).exists()) && (overloadCode <= MAX_OVERLOAD)) {
                overloadCode = overloadCode + 1;
                if (overloadCode < 10) {
                    appLogDirPath = COMPSsLogBaseDirPath + appName + "_0" + String.valueOf(overloadCode) + File.separator;
                } else {
                    appLogDirPath = COMPSsLogBaseDirPath + appName + "_" + String.valueOf(overloadCode) + File.separator;
                }
            }
            if (overloadCode > MAX_OVERLOAD) {
                System.err.println(WARN_FOLDER_OVERLOAD);
                appLogDirPath = COMPSsLogBaseDirPath + appName + "_01" + File.separator;
            }
            if (!new File(appLogDirPath).mkdir()) {
                System.err.println(ERROR_APP_LOG_DIR);
                System.exit(1);
            }
            System.setProperty(ITConstants.IT_APP_LOG_DIR, appLogDirPath);
        }

        /* Create a tmp directory where to store:
         * - Files whose first opened stream is an input one
         * - Object files
         */
        tempDirPath = appLogDirPath + "tmpFiles" + File.separator;
        if (!new File(tempDirPath).mkdir()) {
            System.err.println(ERROR_TEMP_DIR);
            System.exit(1);
        }

        /* Create a jobs dir where to store:
         * - Jobs output files
         * - Jobs error files
         */
        jobsDirPath = appLogDirPath + "jobs" + File.separator;
        if (!new File(jobsDirPath).mkdir()) {
            System.err.println(ERROR_JOBS_DIR);
            System.exit(1);
        }
    }

    public String getCOMPSsLogBaseDirPath() {
        return COMPSsLogBaseDirPath;
    }

    public String getWorkingDirectory() {
        return tempDirPath;
    }

    public String getUserExecutionDirPath() {
        return userExecutionDirPath;
    }

    public String getAppLogDirPath() {
        return appLogDirPath;
    }

    public String getTempDirPath() {
        return tempDirPath;
    }

    public String getJobsDirPath() {
        return jobsDirPath;
    }

    @Override
    public void setInternalURI(URI u) {
        for (CommAdaptor adaptor : Comm.getAdaptors().values()) {
            adaptor.completeMasterURI(u);
        }
    }

    @Override
    public Type getType() {
        return Type.MASTER;
    }

    @Override
    public int compareTo(Resource t) {
        if (t.getType() == Type.MASTER) {
            return getName().compareTo(t.getName());
        } else {
            return 1;
        }
    }

}
