package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanOperatingSystemSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class OperatingSystem extends AbstractElement<XmlBeanOperatingSystemSectionType>
{

    public OperatingSystem( XmlBeanOperatingSystemSectionType base )
    {
        super( base );
    }

    public int getId()
    {
        return delegate.getId();
    }
    
    public void setId( int operatingSystemId )
    {
        delegate.setId( operatingSystemId );
    }

    public String getDescription()
    {
        if ( delegate.isSetDescription() )
        {
            return delegate.getDescription().getStringValue();
        }
        return null;
    }

    public void setDescription( String description )
    {
        delegate.setDescription( XmlSimpleTypeConverter.toMsgType( description ) );
    }

    public String getVersion()
    {
        return delegate.getVersion();
    }

    public void setVersion( String version )
    {
        delegate.setVersion( version );
    }

}
