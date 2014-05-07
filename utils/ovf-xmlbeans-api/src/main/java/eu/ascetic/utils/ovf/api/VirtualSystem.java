package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanOperatingSystemSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualHardwareSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class VirtualSystem extends AbstractElement<XmlBeanVirtualSystemType>
{

    public VirtualSystem( XmlBeanVirtualSystemType base )
    {
        super( base );
    }

    public String getInfo()
    {
        return delegate.getInfo().getStringValue();
    }

    public void setInfo( String info )
    {
        delegate.setInfo( XmlSimpleTypeConverter.toMsgType( info ) );
    }

    public String getName()
    {
        if ( delegate.isSetName() )
        {
            return delegate.getName().getStringValue();
        }
        return null;
    }

    public void setName( String name )
    {
        delegate.setName( XmlSimpleTypeConverter.toMsgType( name ) );
    }

    public String getId()
    {
        return delegate.getId();
    }

    public ProductSection getProductSection()
    {
        return new ProductSection( (XmlBeanProductSectionType) delegate.getSectionArray( 0 ) );
    }

    public OperatingSystem getOperatingSystem()
    {
        return new OperatingSystem( (XmlBeanOperatingSystemSectionType) delegate.getSectionArray( 1 ) );
    }

    public VirtualHardwareSection getVirtualHardwareSection()
    {
        return new VirtualHardwareSection(
            (XmlBeanVirtualHardwareSectionType) delegate.getSectionArray( 2 ) );
    }

}
