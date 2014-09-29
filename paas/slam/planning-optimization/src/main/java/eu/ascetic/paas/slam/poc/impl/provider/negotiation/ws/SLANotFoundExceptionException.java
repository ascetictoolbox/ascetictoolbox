/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */


/**
 * SLANotFoundExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

package eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws;

public class SLANotFoundExceptionException extends java.lang.Exception{
    
    private eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.SLANotFoundExceptionE faultMessage;

    
        public SLANotFoundExceptionException() {
            super("SLANotFoundExceptionException");
        }

        public SLANotFoundExceptionException(java.lang.String s) {
           super(s);
        }

        public SLANotFoundExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public SLANotFoundExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.SLANotFoundExceptionE msg){
       faultMessage = msg;
    }
    
    public eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.SLANotFoundExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    