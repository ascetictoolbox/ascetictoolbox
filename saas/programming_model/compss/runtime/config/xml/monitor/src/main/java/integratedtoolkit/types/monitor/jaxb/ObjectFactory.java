//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.10.13 at 07:33:41 PM CDT 
//


package integratedtoolkit.types.monitor.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the integratedtoolkit.types.monitor.jaxb package. 
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

    private final static QName _COMPSsState_QNAME = new QName("", "COMPSsState");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: integratedtoolkit.types.monitor.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link COMPSsStateType }
     * 
     */
    public COMPSsStateType createCOMPSsStateType() {
        return new COMPSsStateType();
    }

    /**
     * Create an instance of {@link ResourceInfoType }
     * 
     */
    public ResourceInfoType createResourceInfoType() {
        return new ResourceInfoType();
    }

    /**
     * Create an instance of {@link ResourceType }
     * 
     */
    public ResourceType createResourceType() {
        return new ResourceType();
    }

    /**
     * Create an instance of {@link ActionsType }
     * 
     */
    public ActionsType createActionsType() {
        return new ActionsType();
    }

    /**
     * Create an instance of {@link CoresInfoType }
     * 
     */
    public CoresInfoType createCoresInfoType() {
        return new CoresInfoType();
    }

    /**
     * Create an instance of {@link CoreType }
     * 
     */
    public CoreType createCoreType() {
        return new CoreType();
    }

    /**
     * Create an instance of {@link TasksInfoType }
     * 
     */
    public TasksInfoType createTasksInfoType() {
        return new TasksInfoType();
    }

    /**
     * Create an instance of {@link ApplicationType }
     * 
     */
    public ApplicationType createApplicationType() {
        return new ApplicationType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link COMPSsStateType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "COMPSsState")
    public JAXBElement<COMPSsStateType> createCOMPSsState(COMPSsStateType value) {
        return new JAXBElement<COMPSsStateType>(_COMPSsState_QNAME, COMPSsStateType.class, null, value);
    }

}
