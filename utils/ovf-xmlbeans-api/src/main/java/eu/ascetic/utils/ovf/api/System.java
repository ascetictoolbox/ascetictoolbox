package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVSSDType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class System extends AbstractElement<XmlBeanVSSDType>
{
    public System( XmlBeanVSSDType base )
    {
        super( base );
    }

    public String getElementName()
    {
        return delegate.getElementName().getStringValue();
    }

    public void setElementName( String elementName )
    {
        delegate.setElementName( XmlSimpleTypeConverter.toCimString( elementName ) );
    }

    public String getInstanceID()
    {
        return delegate.getInstanceID().getStringValue();
    }

    public void setInstanceID( String instanceID )
    {
        delegate.setInstanceID( XmlSimpleTypeConverter.toCimString( instanceID ) );
    }

    public String getVirtualSystemType()
    {
        return delegate.getVirtualSystemType().getStringValue();
    }

    public void setVirtualSystemType( String virtualSystemType )
    {
        if ( !delegate.isSetVirtualSystemType() )
        {
            delegate.addNewVirtualSystemType();
        }
        delegate.setVirtualSystemType( XmlSimpleTypeConverter.toCimString( virtualSystemType ) );
    }

}
