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

package integratedtoolkit.types.data.location;

import integratedtoolkit.types.resources.Resource;
import java.util.LinkedList;

public class PrivateLocation extends DataLocation{

    final URI uri;

    public PrivateLocation(Resource host, String path) {
        super();
        this.uri = new URI(host, path);
    }

    @Override
    public DataLocation.Type getType() {
        return DataLocation.Type.PRIVATE;
    }

    @Override
    public LinkedList<URI> getURIs() {
        LinkedList<URI> list = new LinkedList<URI>();
        list.add(this.uri);
        return list;
    }

    @Override
    public LinkedList<Resource> getHosts() {
        LinkedList<Resource> list = new LinkedList<Resource>();
        list.add(this.uri.getHost());
        return list;
    }

    @Override
    public URI getURIInHost(Resource targetHost) {
        if (uri.getHost() == targetHost) {
            return uri;
        } else {
            return null;
        }
    }

    @Override
    public boolean isTarget(DataLocation target) {
        if (target.getType() != DataLocation.Type.PRIVATE) {
            return false;
        }
        URI targetURI = ((PrivateLocation) target).uri;
        return (targetURI.getHost() == uri.getHost()
                && targetURI.getPath().contentEquals(uri.getPath()));
    }

    public String toString() {
        return this.uri.toString();
    }

    @Override
    public String getSharedDisk() {
        return null;
    }

    @Override
    public String getPath() {
        return this.uri.getPath();
    }

    @Override
    public String getLocationKey() {
        return uri.getPath() + ":" + uri.getHost().getName();
    }

    @Override
    public int compareTo(DataLocation o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (o.getClass() != PrivateLocation.class) {
            return (this.getClass().getName()).compareTo("integratedtoolkit.types.data.location.PrivateLocation");
        } else {
            return uri.compareTo(((PrivateLocation) o).uri);
        }
    }

}
