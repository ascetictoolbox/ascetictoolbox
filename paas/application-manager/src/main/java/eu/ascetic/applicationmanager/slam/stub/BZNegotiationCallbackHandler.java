
/**
 * BZNegotiationCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.6  Built on : Aug 30, 2011 (10:00:16 CEST)
 */

    package eu.ascetic.applicationmanager.slam.stub;

    /**
     *  BZNegotiationCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class BZNegotiationCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public BZNegotiationCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public BZNegotiationCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for createAgreement method
            * override this method for handling normal response from createAgreement operation
            */
           public void receiveResultcreateAgreement(
                    eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.CreateAgreementResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createAgreement operation
           */
            public void receiveErrorcreateAgreement(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for negotiate method
            * override this method for handling normal response from negotiate operation
            */
           public void receiveResultnegotiate(
                    eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.NegotiateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from negotiate operation
           */
            public void receiveErrornegotiate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for initiateNegotiation method
            * override this method for handling normal response from initiateNegotiation operation
            */
           public void receiveResultinitiateNegotiation(
                    eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiationResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from initiateNegotiation operation
           */
            public void receiveErrorinitiateNegotiation(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for cancelNegotiation method
            * override this method for handling normal response from cancelNegotiation operation
            */
           public void receiveResultcancelNegotiation(
                    eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.CancelNegotiationResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from cancelNegotiation operation
           */
            public void receiveErrorcancelNegotiation(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for renegotiate method
            * override this method for handling normal response from renegotiate operation
            */
           public void receiveResultrenegotiate(
                    eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.RenegotiateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from renegotiate operation
           */
            public void receiveErrorrenegotiate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for provision method
            * override this method for handling normal response from provision operation
            */
           public void receiveResultprovision(
                    eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.ProvisionResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from provision operation
           */
            public void receiveErrorprovision(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for terminate method
            * override this method for handling normal response from terminate operation
            */
           public void receiveResultterminate(
                    eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.TerminateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from terminate operation
           */
            public void receiveErrorterminate(java.lang.Exception e) {
            }
                


    }
    