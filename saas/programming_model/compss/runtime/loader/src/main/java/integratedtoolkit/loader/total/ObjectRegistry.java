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

package integratedtoolkit.loader.total;

import java.util.TreeMap;

import org.apache.log4j.Logger;

import integratedtoolkit.loader.LoaderAPI;
import integratedtoolkit.log.Loggers;

public class ObjectRegistry {

    // Api object used to invoke calls on the Integrated Toolkit
    private LoaderAPI itApi;
    // Temporary directory where the files containing objects will be stored (same as the stream registry dir)
    private String serialDir;
    // Map: hash code -> object
    // Objects 
    private TreeMap<Integer, Object> appTaskObjects;
    private TreeMap<Integer, Object> internalObjects;

    private static final Logger logger = Logger.getLogger(Loggers.LOADER);
    private static final boolean debug = logger.isDebugEnabled();
    
    
    public ObjectRegistry(LoaderAPI api) {
        this.itApi = api;
        this.serialDir = api.getTempDir();
        this.appTaskObjects = new TreeMap<Integer, Object>();
        this.internalObjects = new TreeMap<Integer, Object>();

        itApi.setObjectRegistry(this);
    }

    public int newObjectParameter(Object o) {
        if (o == null) {
            return Integer.MAX_VALUE;
        }

        int hashCode = o.hashCode();
        Object oStored = appTaskObjects.get(hashCode);
        while (oStored != o) {
            if (oStored == null) {
                appTaskObjects.put(hashCode, o);
                // Store it as an internal one too. Read-only objects will always use this same instance
                internalObjects.put(hashCode, o);
                if (debug) {
                    logger.debug("Object " + o + " with hash code " + hashCode + " registered");
                }

                break;
            }
            // (oStored != o != null) {
    		/* Coincidence of two equal hash codes for different objects.
             * Increment the hash code and try again.
             */
            oStored = appTaskObjects.get(++hashCode);
        }

        return hashCode;
    }

    public void newObjectAccess(Object o) {
        newObjectAccess(o, true);
    }

    public void newObjectAccess(Object o, boolean isWriter) {
        if (o == null) {
            return;
        }

        int hashCode = o.hashCode();
        
        Object oStored = appTaskObjects.get(hashCode);
        while (oStored != o) {
            if (oStored == null) {
                return; // Not a task parameter object
            } else {
                oStored = appTaskObjects.get(++hashCode);
            }
        }
        /* The object has been accessed by a task before.
         * Check with the API that the application has the last version,
         * blocking if necessary.
         */
        if (debug) {
            logger.debug("New access to object with hash code " + hashCode + ", for writing: " + isWriter);
        }

        // Get the updated version of the object
        Object oUpdated = itApi.getObject(o, hashCode, serialDir);
        if (oUpdated != null) {
            internalObjects.put(hashCode, oUpdated);
        }
    }

    public void serializeLocally(Object o) {
        if (o == null) {
            return;
        }

        int hashCode = o.hashCode();
        Object oStored = appTaskObjects.get(hashCode);
        while (oStored != o) {
            if (oStored == null) {
                return; // Not a task parameter object
            } else {
                oStored = appTaskObjects.get(++hashCode);
            }
        }
        /* The object has been accessed by a task before.
         * Delegate its serialization to the API.
         * Serialize the internal object
         */
        if (debug) {
            logger.debug("About to serialize locally object with hash code " + hashCode);
        }

        itApi.serializeObject(internalObjects.get(hashCode), hashCode, serialDir);
    }

    public Object getInternalObject(Object o) {
        if (o == null) {
            return null;
        }

        int hashCode = o.hashCode();
        Object oStored = appTaskObjects.get(hashCode);
        while (oStored != o) {
            if (oStored == null) {
                return null; // Not a task parameter object
            } else {
                oStored = appTaskObjects.get(++hashCode);
            }
        }
        /* The object has been accessed by a task before.
         * Return its internal (real) value
         */
        if (debug) {
            logger.debug("Returning internal object " + hashCode);
        }

        return internalObjects.get(hashCode);
    }
}
