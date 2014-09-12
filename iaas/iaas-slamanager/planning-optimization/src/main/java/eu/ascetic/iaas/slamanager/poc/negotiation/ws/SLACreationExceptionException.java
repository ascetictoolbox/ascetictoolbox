
/**
 * SLACreationExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

package eu.ascetic.iaas.slamanager.poc.negotiation.ws;

public class SLACreationExceptionException extends java.lang.Exception{
    
    private eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.SLACreationExceptionE faultMessage;

    
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
    

    public void setFaultMessage(eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.SLACreationExceptionE msg){
       faultMessage = msg;
    }
    
    public eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.SLACreationExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    