/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.ascetic.paas.slam.poc.exceptions;

/**
 * The <code>NoVMSpecifiedException</code> exception class represents that there is not VM specified.
 * 
 * @author Kuan Lu
 */
public class NoVMSpecifiedException extends Exception {

    private static final long serialVersionUID = 1L;

    public NoVMSpecifiedException() {
        // TODO Auto-generated constructor stub
    }

    public NoVMSpecifiedException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public NoVMSpecifiedException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public NoVMSpecifiedException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
