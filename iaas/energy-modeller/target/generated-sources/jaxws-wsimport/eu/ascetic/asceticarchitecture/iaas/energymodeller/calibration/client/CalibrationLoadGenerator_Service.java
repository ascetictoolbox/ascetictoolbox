
package eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.client;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.6b21 
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "CalibrationLoadGenerator", targetNamespace = "http://energymodellerloadinducer.ascetic.eu/", wsdlLocation = "http://localhost:8080/EnergyModellerCalibrationTool/CalibrationLoadGenerator?WSDL")
public class CalibrationLoadGenerator_Service
    extends Service
{

    private final static URL CALIBRATIONLOADGENERATOR_WSDL_LOCATION;
    private final static WebServiceException CALIBRATIONLOADGENERATOR_EXCEPTION;
    private final static QName CALIBRATIONLOADGENERATOR_QNAME = new QName("http://energymodellerloadinducer.ascetic.eu/", "CalibrationLoadGenerator");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/EnergyModellerCalibrationTool/CalibrationLoadGenerator?WSDL");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        CALIBRATIONLOADGENERATOR_WSDL_LOCATION = url;
        CALIBRATIONLOADGENERATOR_EXCEPTION = e;
    }

    public CalibrationLoadGenerator_Service() {
        super(__getWsdlLocation(), CALIBRATIONLOADGENERATOR_QNAME);
    }

    public CalibrationLoadGenerator_Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), CALIBRATIONLOADGENERATOR_QNAME, features);
    }

    public CalibrationLoadGenerator_Service(URL wsdlLocation) {
        super(wsdlLocation, CALIBRATIONLOADGENERATOR_QNAME);
    }

    public CalibrationLoadGenerator_Service(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, CALIBRATIONLOADGENERATOR_QNAME, features);
    }

    public CalibrationLoadGenerator_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public CalibrationLoadGenerator_Service(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns CalibrationLoadGenerator
     */
    @WebEndpoint(name = "CalibrationLoadGeneratorPort")
    public CalibrationLoadGenerator getCalibrationLoadGeneratorPort() {
        return super.getPort(new QName("http://energymodellerloadinducer.ascetic.eu/", "CalibrationLoadGeneratorPort"), CalibrationLoadGenerator.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CalibrationLoadGenerator
     */
    @WebEndpoint(name = "CalibrationLoadGeneratorPort")
    public CalibrationLoadGenerator getCalibrationLoadGeneratorPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://energymodellerloadinducer.ascetic.eu/", "CalibrationLoadGeneratorPort"), CalibrationLoadGenerator.class, features);
    }

    private static URL __getWsdlLocation() {
        if (CALIBRATIONLOADGENERATOR_EXCEPTION!= null) {
            throw CALIBRATIONLOADGENERATOR_EXCEPTION;
        }
        return CALIBRATIONLOADGENERATOR_WSDL_LOCATION;
    }

}