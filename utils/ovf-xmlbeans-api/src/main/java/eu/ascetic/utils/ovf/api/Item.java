package eu.ascetic.utils.ovf.api;

import java.math.BigInteger;
import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanRASDType;
import org.dmtf.schemas.wbem.wscim.x1.common.CimString;

import eu.ascetic.utils.ovf.api.AbstractElement;

public class Item extends AbstractElement<XmlBeanRASDType>
{
    public Item( XmlBeanRASDType base )
    {
        super( base );
    }

    public String getDescription()
    {
        if ( delegate.isSetDescription() )
        {
            return delegate.getDescription().getStringValue();
        }
        return null;
    }

    public String getElementName()
    {
        return delegate.getElementName().getStringValue();
    }

    public String getInstanceID()
    {
        return delegate.getInstanceID().getStringValue();
    }

    public int getResourceType()
    {
        return delegate.getResourceType().getIntValue();
    }

    public BigInteger getVirtualQuantity()
    {
        if ( delegate.isSetVirtualQuantity() )
        {
            return delegate.getVirtualQuantity().getBigIntegerValue();
        }
        return null;
    }

    public String getAllocationUnits()
    {
        if ( delegate.isSetAllocationUnits() )
        {
            return delegate.getAllocationUnits().getStringValue();
        }
        return null;
    }

    public Boolean getAutomaticAllocation()
    {
        return delegate.getAutomaticAllocation().getBooleanValue();
    }

    public String[] getConnectionArray()
    {
        Vector<String> vector = new Vector<String>();
        for ( CimString type : delegate.getConnectionArray() )
        {
            vector.add( type.getStringValue() );
        }
        return vector.toArray( new String[ vector.size() ] );
    }

    public String getConnectionArray( int i )
    {
        return delegate.getConnectionArray( i ).getStringValue();
    }

    public String getParent()
    {
        if ( delegate.isSetParent() )
        {
            return delegate.getParent().getStringValue();
        }
        return null;
    }

    public String[] getHostResourceArray()
    {
        Vector<String> vector = new Vector<String>();
        for ( CimString cimString : delegate.getHostResourceArray() )
        {
            vector.add( cimString.getStringValue() );
        }
        return vector.toArray( new String[ vector.size() ] );
    }

    public String getHostResourceArray( int i )
    {
        if ( delegate.getHostResourceArray().length > i )
        {
            return delegate.getHostResourceArray( i ).getStringValue();
        }
        return null;
    }
}
