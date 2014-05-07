package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;

public class ProductProperty extends AbstractElement<XmlBeanProductSectionType.Property>
{
    public ProductProperty( XmlBeanProductSectionType.Property base )
    {
        super( base );
    }

    public void setType( String type )
    {
        delegate.setType( type );
    }

    public void setKey( String key )
    {
        delegate.setKey( key );
    }

    public String getKey()
    {
        return delegate.getKey();
    }

    public String getType()
    {
        return delegate.getType();
    }

    public String getValue()
    {
        return delegate.getValue2();
    }

    public void setValue( String value2 )
    {
        delegate.setValue2( value2 );
    }
}
