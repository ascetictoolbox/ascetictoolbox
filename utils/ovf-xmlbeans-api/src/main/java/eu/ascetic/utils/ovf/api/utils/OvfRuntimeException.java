/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.utils.ovf.api.utils;

/**
 * Provides a generic runtime exception originating from the OVF API.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class OvfRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1429968239613511231L;

    /**
     * Constructs a new OVF API runtime exception with null as its detail
     * message. The cause is not initialised, and may subsequently be
     * initialised by a call to {@link #initCause}.
     */
    public OvfRuntimeException() {
        super();
    }

    /**
     * Constructs a new OVF API runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable stack trace
     * enabled or disabled.
     * 
     * @param message
     *            The detail message
     * @param cause
     *            The cause (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown)
     * @param enableSuppression
     *            Whether or not suppression is enabled or disabled
     * @param writableStackTrace
     *            Whether or not the stack trace should be writable
     */
    public OvfRuntimeException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructs a new OVF API runtime exception with the specified detail
     * message and cause.<br>
     * <br>
     * Note that the detail message associated with cause is not automatically
     * incorporated in this runtime exception's detail message.
     * 
     * @param message
     *            The detail message (which is saved for later retrieval by the
     *            getMessage() method)
     * @param cause
     *            The cause (which is saved for later retrieval by the
     *            getCause() method, a null value is permitted, and indicates
     *            that the cause is nonexistent or unknown)
     */
    public OvfRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new OVF API runtime exception with the specified detail
     * message. The cause is not initialised, and may subsequently be
     * initialised by a call to {@link #initCause}.
     * 
     * 
     * 
     * @param message
     *            The detail message, the detail message is saved for later
     *            retrieval by the {@link #getMessage()} method
     */
    public OvfRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new OVF API runtime exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of cause). This
     * constructor is useful for runtime exceptions that are little more than
     * wrappers for other throwables.
     * 
     * @param cause
     *            The cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <tt>null</tt> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     * 
     * 
     */
    public OvfRuntimeException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
