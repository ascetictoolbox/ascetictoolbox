/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */


/**
 * SLACreationExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

package eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws;

public class SLACreationExceptionException extends java.lang.Exception{
    
    private eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.SLACreationExceptionE faultMessage;

    
        public SLACreationExceptionException() {
            super("SLACreationExceptionException");
        }

        public SLACreationExceptionException(java.lang.String s) {
           super(s);
        }

        public SLACreationExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public SLACreationExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.SLACreationExceptionE msg){
       faultMessage = msg;
    }
    
    public eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.SLACreationExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    