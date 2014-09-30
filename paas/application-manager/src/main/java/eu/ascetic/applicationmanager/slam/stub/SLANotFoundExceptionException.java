
/**
 * SLANotFoundExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

package eu.ascetic.applicationmanager.slam.stub;

public class SLANotFoundExceptionException extends java.lang.Exception{
    
    private eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.SLANotFoundExceptionE faultMessage;

    
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
    

    public void setFaultMessage(eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.SLANotFoundExceptionE msg){
       faultMessage = msg;
    }
    
    public eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.SLANotFoundExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    