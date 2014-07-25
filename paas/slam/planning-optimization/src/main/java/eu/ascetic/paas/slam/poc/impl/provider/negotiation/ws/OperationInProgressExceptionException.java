
/**
 * OperationInProgressExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

package eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws;

public class OperationInProgressExceptionException extends java.lang.Exception{
    
    private eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.OperationInProgressExceptionE faultMessage;

    
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
    

    public void setFaultMessage(eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.OperationInProgressExceptionE msg){
       faultMessage = msg;
    }
    
    public eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.OperationInProgressExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    