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

package integratedtoolkit.types;

import integratedtoolkit.types.resources.ResourceDescription;

public abstract class Implementation <T extends ResourceDescription> {

    public enum Type {

        METHOD,
        SERVICE
    }

    protected final int coreId;
    protected final int implementationId;
    protected T requirements;

    public Implementation(int coreId, int implementationId, T annot) {
        this.coreId = coreId;
        this.implementationId = implementationId;
        this.requirements = annot;
    }

    public int getCoreId() {
        return coreId;
    }

    public int getImplementationId() {
        return implementationId;
    }

    public T getRequirements() {
        return requirements;
    }

    public abstract Type getType();

    public String toString() {
        StringBuilder sb = new StringBuilder("Implementation ").append(implementationId);
        sb.append(" for core ").append(coreId);
        sb.append(":");
        return sb.toString();
    }

}
