/**
 *  Copyright 2013 University of Leeds
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
package eu.ascetic.vmc.api.core;

/**
 * Custom exception class for system calls.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.1
 */
public class SystemCallException extends Exception {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -4985438448235773464L;

    /**
     * Constructs a new exception with null as its detail message.
     */
    public SystemCallException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message
     * 
     * @param message
     *            the detail message.
     */
    public SystemCallException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     * 
     */
    public SystemCallException(String message, Throwable cause) {
        super(message, cause);
    }

}
