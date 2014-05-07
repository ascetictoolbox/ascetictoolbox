/* 
 * Copyright (c) 2012, Fraunhofer-Gesellschaft
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the disclaimer at the end.
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 * 
 * (2) Neither the name of Fraunhofer nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package eu.ascetic.utils.ovf.api;

import java.util.List;
import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanRASDType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualHardwareSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class VirtualHardwareSection extends AbstractElement<XmlBeanVirtualHardwareSectionType>
{
    public VirtualHardwareSection( XmlBeanVirtualHardwareSectionType base )
    {
        super( base );
    }

    public String getInfo()
    {
        return delegate.getInfo().getStringValue();
    }

    public System getSystem()
    {
        return new System( delegate.getSystem() );
    }

    public Item[] getItemArray()
    {
        List<Item> vector = new Vector<Item>();
        for ( XmlBeanRASDType type : delegate.getItemArray() )
        {
            vector.add( new Item( type ) );
        }
        return vector.toArray( new Item[vector.size()] );
    }

    public Item getItemArray( int i )
    {
        return new Item( delegate.getItemArray( i ) );
    }

    public String getVirtualHardwareFamily()
    {
        return delegate.getSystem().getVirtualSystemType().getStringValue();
    }

    public void setVirtualHardwareFamily( String virtualHardwareFamily )
    {
        delegate.getSystem()
                .setVirtualSystemType( XmlSimpleTypeConverter.toCimString( virtualHardwareFamily ) );
    }

    public int getNumberOfVirtualCPUs()
    {
        return delegate.getItemArray( 0 ).getVirtualQuantity().getBigIntegerValue().intValue();
    }

    public void setNumberOfVirtualCPUs( int numberOfVirtualCPUs )
    {
        delegate.getItemArray( 0 ).setVirtualQuantity(
            XmlSimpleTypeConverter.toCimUnsignedLong( numberOfVirtualCPUs ) );
    }

    public int getMemorySize()
    {
        return delegate.getItemArray( 1 ).getVirtualQuantity().getBigIntegerValue().intValue();
    }

    public void setMemorySize( int memorySize )
    {
        if ( memorySize < 0 )
        {
            throw new IllegalArgumentException( "memory size must be > -1" );
        }

        delegate.getItemArray( 1 )
                .setVirtualQuantity( XmlSimpleTypeConverter.toCimUnsignedLong( memorySize ) );
    }

    public void setCPUSpeed( int cpuSpeed )
    {
        if ( !( cpuSpeed > -1 ) )
        {
            throw new IllegalArgumentException( "cpu speed must be > -1" );
        }
        delegate.getItemArray( 2 ).setReservation( XmlSimpleTypeConverter.toCimUnsignedLong( cpuSpeed ) );
    }

    public int getCPUSpeed()
    {
        return delegate.getItemArray( 2 ).getReservation().getBigIntegerValue().intValue();
    }

}
