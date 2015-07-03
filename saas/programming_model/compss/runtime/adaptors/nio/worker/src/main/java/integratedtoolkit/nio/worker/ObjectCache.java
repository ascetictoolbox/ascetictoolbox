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
package integratedtoolkit.nio.worker;

import java.util.HashMap;

public class ObjectCache {

    private final HashMap<String, Object> objectCache = new HashMap<String, Object>();

    public synchronized void store(String name, Object value) {
        try {
            objectCache.put(name, value);
        } catch (NullPointerException e) {
            System.err.println("Object Cache " + objectCache + " dataId " + name + " object " + value);
        }
    }

    public synchronized Object get(String name) {
        return objectCache.get(name);
    }

    public synchronized void remove(String name) {
        objectCache.remove(name);
    }

    public synchronized boolean checkPresence(String name) {
        return objectCache.containsKey(name);
    }

}
