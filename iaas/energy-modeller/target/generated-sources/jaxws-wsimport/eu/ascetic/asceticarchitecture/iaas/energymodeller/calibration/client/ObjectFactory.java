
package eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CurrentlyWorkingResponse_QNAME = new QName("http://energymodellerloadinducer.ascetic.eu/", "currentlyWorkingResponse");
    private final static QName _InduceLoad_QNAME = new QName("http://energymodellerloadinducer.ascetic.eu/", "induceLoad");
    private final static QName _FinishedResponse_QNAME = new QName("http://energymodellerloadinducer.ascetic.eu/", "finishedResponse");
    private final static QName _Finished_QNAME = new QName("http://energymodellerloadinducer.ascetic.eu/", "finished");
    private final static QName _InduceLoadResponse_QNAME = new QName("http://energymodellerloadinducer.ascetic.eu/", "induceLoadResponse");
    private final static QName _CurrentlyWorking_QNAME = new QName("http://energymodellerloadinducer.ascetic.eu/", "currentlyWorking");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CurrentlyWorking }
     * 
     */
    public CurrentlyWorking createCurrentlyWorking() {
        return new CurrentlyWorking();
    }

    /**
     * Create an instance of {@link InduceLoadResponse }
     * 
     */
    public InduceLoadResponse createInduceLoadResponse() {
        return new InduceLoadResponse();
    }

    /**
     * Create an instance of {@link Finished }
     * 
     */
    public Finished createFinished() {
        return new Finished();
    }

    /**
     * Create an instance of {@link FinishedResponse }
     * 
     */
    public FinishedResponse createFinishedResponse() {
        return new FinishedResponse();
    }

    /**
     * Create an instance of {@link InduceLoad }
     * 
     */
    public InduceLoad createInduceLoad() {
        return new InduceLoad();
    }

    /**
     * Create an instance of {@link CurrentlyWorkingResponse }
     * 
     */
    public CurrentlyWorkingResponse createCurrentlyWorkingResponse() {
        return new CurrentlyWorkingResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CurrentlyWorkingResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://energymodellerloadinducer.ascetic.eu/", name = "currentlyWorkingResponse")
    public JAXBElement<CurrentlyWorkingResponse> createCurrentlyWorkingResponse(CurrentlyWorkingResponse value) {
        return new JAXBElement<CurrentlyWorkingResponse>(_CurrentlyWorkingResponse_QNAME, CurrentlyWorkingResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InduceLoad }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://energymodellerloadinducer.ascetic.eu/", name = "induceLoad")
    public JAXBElement<InduceLoad> createInduceLoad(InduceLoad value) {
        return new JAXBElement<InduceLoad>(_InduceLoad_QNAME, InduceLoad.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FinishedResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://energymodellerloadinducer.ascetic.eu/", name = "finishedResponse")
    public JAXBElement<FinishedResponse> createFinishedResponse(FinishedResponse value) {
        return new JAXBElement<FinishedResponse>(_FinishedResponse_QNAME, FinishedResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Finished }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://energymodellerloadinducer.ascetic.eu/", name = "finished")
    public JAXBElement<Finished> createFinished(Finished value) {
        return new JAXBElement<Finished>(_Finished_QNAME, Finished.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InduceLoadResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://energymodellerloadinducer.ascetic.eu/", name = "induceLoadResponse")
    public JAXBElement<InduceLoadResponse> createInduceLoadResponse(InduceLoadResponse value) {
        return new JAXBElement<InduceLoadResponse>(_InduceLoadResponse_QNAME, InduceLoadResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CurrentlyWorking }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://energymodellerloadinducer.ascetic.eu/", name = "currentlyWorking")
    public JAXBElement<CurrentlyWorking> createCurrentlyWorking(CurrentlyWorking value) {
        return new JAXBElement<CurrentlyWorking>(_CurrentlyWorking_QNAME, CurrentlyWorking.class, null, value);
    }

}
