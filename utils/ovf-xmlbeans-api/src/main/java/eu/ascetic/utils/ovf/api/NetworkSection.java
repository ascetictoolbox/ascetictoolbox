package eu.ascetic.utils.ovf.api;

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanNetworkSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class NetworkSection extends AbstractElement<XmlBeanNetworkSectionType>
{

    public NetworkSection( XmlBeanNetworkSectionType base )
    {
        super( base );
    }

    
    public Network[] getNetworkArray()
    {
        Vector<Network> networkArray = new Vector<Network>();
        for ( XmlBeanNetworkSectionType.Network network : delegate.getNetworkArray() )
        {
            networkArray.add( new Network( network ) );
        }
        return networkArray.toArray( new Network[networkArray.size()] );
    }

    
    public Network getNetworkArray( int i )
    {
        return new Network( delegate.getNetworkArray( i ) );
    }

    
    public String getInfo()
    {
        return delegate.getInfo().getStringValue();
    }

    
    public void setInfo( String info )
    {
        delegate.setInfo( XmlSimpleTypeConverter.toMsgType( info ) );
    }
}
