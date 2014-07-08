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

package integratedtoolkit.util;

/**
 * The ElementNotFoundException is an Exception that will arise when some 
 * element that someone was looking for in a set is not inside it. 
 */
public class ElementNotFoundException extends Exception {

    /**
     * Constructs a new ElementNotFoundException with the default messatge
     */
    public ElementNotFoundException() {
        super("Cannot find the requested element");
    }

    /**
     * Constructs a new ElementNotFoundException with that message
     * @param message Message that will return the exception
     */
    public ElementNotFoundException(String message) {
        super(message);
    }
}
