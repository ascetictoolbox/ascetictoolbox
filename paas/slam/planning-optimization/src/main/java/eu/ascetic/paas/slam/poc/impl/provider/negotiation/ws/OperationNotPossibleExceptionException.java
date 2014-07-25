
/**
 * OperationNotPossibleExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

package eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws;

public class OperationNotPossibleExceptionException extends java.lang.Exception{
    
    private eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.OperationNotPossibleExceptionE faultMessage;

    
        public OperationNotPossibleExceptionException() {
            super("OperationNotPossibleExceptionException");
        }

        public OperationNotPossibleExceptionException(java.lang.String s) {
           super(s);
        }

        public OperationNotPossibleExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public OperationNotPossibleExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.OperationNotPossibleExceptionE msg){
       faultMessage = msg;
    }
    
    public eu.ascetic.paas.slam.poc.impl.provider.negotiation.ws.BZNegotiationStub.OperationNotPossibleExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    