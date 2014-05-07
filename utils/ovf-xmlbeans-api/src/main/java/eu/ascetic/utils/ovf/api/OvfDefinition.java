package eu.ascetic.utils.ovf.api;

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanContentType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanDiskSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanEnvelopeType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanNetworkSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemCollectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.VirtualSystem;

public class OvfDefinition extends AbstractElement<XmlBeanEnvelopeType>
{

    public OvfDefinition( XmlBeanEnvelopeType base )
    {
        super( base );
    }

    public References getReferences()
    {
        return new References( delegate.getReferences() );
    }

    public DiskSection getDiskSection()
    {
        return new DiskSection( (XmlBeanDiskSectionType) delegate.getSectionArray( 0 ) );
    }

    public NetworkSection getNetworkSection()
    {
        return new NetworkSection( (XmlBeanNetworkSectionType) delegate.getSectionArray( 1 ) );
    }

    public VirtualSystem getVirtualSystem()
    {
        return new VirtualSystem( (XmlBeanVirtualSystemType) delegate.getContent() );
    }

    public VirtualSystem[] getVirtualSystemArray()
    {
        Vector<VirtualSystem> vector = new Vector<VirtualSystem>();
        XmlBeanVirtualSystemCollectionType collectionType =
            (XmlBeanVirtualSystemCollectionType) delegate.getContent();
        if ( collectionType != null )
        {
            for ( XmlBeanContentType contentType : collectionType.getContentArray() )
            {
                vector.add( new VirtualSystem( (XmlBeanVirtualSystemType) contentType ) );
            }
            return vector.toArray( new VirtualSystem[vector.size()] );
        }
        return null;
    }

    public VirtualSystem getVirtualSystemArray( int i )
    {
        XmlBeanVirtualSystemCollectionType collectionType =
            (XmlBeanVirtualSystemCollectionType) delegate.getContent();
        if ( collectionType != null )
        {
            return new VirtualSystem( (XmlBeanVirtualSystemType) collectionType.getContentArray( i ) );
        }
        return null;
    }
}
