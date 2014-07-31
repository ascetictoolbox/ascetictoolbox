
/**
 * ProvisioningExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

package eu.ascetic.iaas.slamanager.poc.negotiation.ws;

public class ProvisioningExceptionException extends java.lang.Exception{
    
    private eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.ProvisioningExceptionE faultMessage;

    
        public ProvisioningExceptionException() {
            super("ProvisioningExceptionException");
        }

        public ProvisioningExceptionException(java.lang.String s) {
           super(s);
        }

        public ProvisioningExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public ProvisioningExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.ProvisioningExceptionE msg){
       faultMessage = msg;
    }
    
    public eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.ProvisioningExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    