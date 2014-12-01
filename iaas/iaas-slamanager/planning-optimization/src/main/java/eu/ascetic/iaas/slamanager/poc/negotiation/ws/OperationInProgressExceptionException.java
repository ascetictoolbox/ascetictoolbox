/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
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

/**
 * OperationInProgressExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

package eu.ascetic.iaas.slamanager.poc.negotiation.ws;

public class OperationInProgressExceptionException extends java.lang.Exception{
    
    private eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.OperationInProgressExceptionE faultMessage;

    
        public OperationInProgressExceptionException() {
            super("OperationInProgressExceptionException");
        }

        public OperationInProgressExceptionException(java.lang.String s) {
           super(s);
        }

        public OperationInProgressExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public OperationInProgressExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.OperationInProgressExceptionE msg){
       faultMessage = msg;
    }
    
    public eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.OperationInProgressExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    