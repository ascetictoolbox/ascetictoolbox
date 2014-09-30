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
package integratedtoolkit.types;

import integratedtoolkit.util.CoreManager;

public abstract class Implementation {

    public enum Type {

        METHOD,
        SERVICE
    }

    final int coreId;
    final int implementationId;
    final CoreManager.Constraints annot;
    final ResourceDescription resource;

    public Implementation(int coreId, int implementationId, CoreManager.Constraints annot, ResourceDescription resource) {
        this.coreId = coreId;
        this.implementationId = implementationId;
        this.annot = annot;
        this.resource = resource;
    }

    public int getCoreId() {
        return coreId;
    }

    public int getImplementationId() {
        return implementationId;
    }

    public CoreManager.Constraints getAnnot() {
        return annot;
    }

    public ResourceDescription getResource() {
        return resource;
    }

    public abstract Type getType();

    public String toString() {
        StringBuilder sb = new StringBuilder("Implementation ").append(implementationId);
        sb.append(" for core ").append(coreId);
        sb.append(":");
        return sb.toString();
    }
}
