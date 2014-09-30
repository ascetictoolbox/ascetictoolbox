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

import integratedtoolkit.types.Parameter.DependencyParameter;
import java.util.List;
import java.util.LinkedList;



import org.gridlab.gat.io.File;
import org.gridlab.gat.io.LogicalFile;

public abstract class FileOperation {

    public enum OpEndState {

        OP_OK,
        OP_IN_PROGRESS,
        OP_FAILED,
        OP_PREPARATION_FAILED,
        OP_WAITING_SOURCES;
    }
    private static int opCount = 0;
    private int operationId;
    // Identifiers of the groups to which the operation belongs
    private List<Integer> groupIds;
    // State the operation has finished with
    private OpEndState endState;
    // Possibly thrown exception
    private Exception exception;
    private String name;

    public FileOperation(LogicalFile lf, int groupId) {
        try {
            this.name = lf.getName();
        } catch (Exception e) {
        }
        this.groupIds = new LinkedList<Integer>();
        this.groupIds.add(groupId);
        operationId = opCount;
        opCount++;
    }

    public FileOperation(LogicalFile lf, List<Integer> groupIds) {
        try {
            this.name = lf.getName();
        } catch (Exception e) {
        }
        this.groupIds = groupIds;
        operationId = opCount;
        opCount++;
    }

    public int getId() {
        return operationId;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getGroupIds() {
        return groupIds;
    }

    public OpEndState getEndState() {
        return endState;
    }

    public Exception getException() {
        return exception;
    }

    public void addGroupId(int groupId) {
        this.groupIds.add(groupId);
    }

    public void setEndState(OpEndState state) {
        this.endState = state;
    }

    public void setException(Exception e) {
        this.exception = e;
    }

    public static class Copy extends FileOperation {

        private PhysicalDataInfo data;
        private String targetName;
        private Location targetLocation;
        private boolean workOnCopy;
        private DependencyParameter dp;

        public Copy(int gId,
                PhysicalDataInfo data,
                String targetName,
                Location targetLoc,
                boolean workOnCopy,
                DependencyParameter dp) {

            super(data.logicalFile, gId);
            this.data = data;
            this.targetName = targetName;
            this.targetLocation = targetLoc;
            this.workOnCopy = workOnCopy;
            this.dp = dp;
        }

        public boolean isDataInMemory() {
            return this.data.isInMemory();
        }

        public boolean workOnCopy() {
            return workOnCopy;
        }

        public LogicalFile getLogicalFile() {
            return this.data.logicalFile;
        }

        public PhysicalDataInfo getPhysicalDataInfo() {
            return this.data;
        }

        public void writeValueToFile() throws Exception {
            data.writeToFile();
        }

        public String getTargetName() {
            return targetName;
        }

        public Location getTargetLocation() {
            return targetLocation;
        }

        public DependencyParameter getDependencyParameter() {
            return dp;
        }
    }

    public static class Delete extends FileOperation {

        private File file;

        public Delete(int gId,
                File file) {

            super(null, gId);

            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }
}
