package eu.ascetic.architecture.iaas.main.beans.client;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.slasoi.gslam.core.negotiation.INegotiation;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Customisable;
import org.slasoi.slamodel.sla.Party;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

public class RequestHelper implements Params.VMCharacteristic
{
    public RequestHelper( INegotiation negotiation, SLATemplate template )
    {
        iNegotiation = negotiation;
        iTemplate    = template;
    }
    
    public void run( RequestHelper helper ) throws Exception
    {
        setParties();
        
        for ( int i = 0; i < 10; i++ ) // 10 request
        {
            System.out.println( "*** Simulating request#"+i+"..." );
            int customers = 5;
            
            Vector<Hashtable<String, String>> requestSet = createRequests( customers );
            for ( int j = 0; j < requestSet.size(); j++ )
            {
                Hashtable<String, String> props = requestSet.elementAt( j );
                
                SLATemplate newTemplate = helper.updateTemplate( props );
                negotiation( newTemplate );
            }
        }
        
        // notify cloudsim about end of simulation
        SLATemplate newTemplate = helper.getEndTemplate();
        negotiation( newTemplate );
        
        System.out.println( "*** Last VM Request sent.  End of Simulation will be scheduled!" );
    }
    
    public Vector<Hashtable<String, String>> createRequests( int users )
    {
        Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>( 5, 3 );
        
        for ( int i = 0; i < users; i++ )
        {
            Hashtable<String, String> params = new Hashtable<String, String>();
            String vms = getRandomVMs();
            params.put( VMTYPES, vms );
          
            if ( vms.contains( VM_DEVELOPERS ) )
            {
                //VM_DEVELOPERS
                VmRandomFeature vmRF = getVmRandomFeature();
                params.put( "VM_DEVELOPERS." + VM_QUANTITY_VAR     , ""+vmRF.instances               );
                params.put( "VM_DEVELOPERS." + VM_CORES_VAR        , "4"                             );
                params.put( "VM_DEVELOPERS." + VM_CPU_SPEED_VAR    , "3.0"                           );
                params.put( "VM_DEVELOPERS." + VM_MEMORY_SIZE_VAR  , "2048"                          );
                params.put( "VM_DEVELOPERS." + VM_HARDDISK_SIZE_VAR, "4"                             );
                params.put( "VM_DEVELOPERS." + START_TIME_VAR      , vmRF.start                      );
                params.put( "VM_DEVELOPERS." + END_TIME_VAR        , vmRF.end                        );
                params.put( "VM_DEVELOPERS." + VM_ISOLATION_VAR    , "true"                          );
                params.put( "VM_DEVELOPERS." + VM_PERSISTENCE_VAR  , "false"                         );
                params.put( "VM_DEVELOPERS." + VM_IMAGE_VAR        , "http://my.image.VM_DEVELOPERS" );
            }
            if ( vms.contains( VM_DEVELOPERS_LOW ) )
            {
                //VM_DEVELOPERS_LOW
                VmRandomFeature vmRF = getVmRandomFeature();
                params.put( "VM_DEVELOPERS_LOW." + VM_QUANTITY_VAR     , ""+vmRF.instances                   );
                params.put( "VM_DEVELOPERS_LOW." + VM_CORES_VAR        , "2"                                 );
                params.put( "VM_DEVELOPERS_LOW." + VM_CPU_SPEED_VAR    , "2.6"                               );
                params.put( "VM_DEVELOPERS_LOW." + VM_MEMORY_SIZE_VAR  , "1024"                              );
                params.put( "VM_DEVELOPERS_LOW." + VM_HARDDISK_SIZE_VAR, "2"                                 );
                params.put( "VM_DEVELOPERS_LOW." + START_TIME_VAR      , vmRF.start                          );
                params.put( "VM_DEVELOPERS_LOW." + END_TIME_VAR        , vmRF.end                            );
                params.put( "VM_DEVELOPERS_LOW." + VM_ISOLATION_VAR    , "false"                             );
                params.put( "VM_DEVELOPERS_LOW." + VM_PERSISTENCE_VAR  , "false"                             );
                params.put( "VM_DEVELOPERS_LOW." + VM_IMAGE_VAR        , "http://my.image.VM_DEVELOPERS_LOW" );
            }
            if ( vms.contains( VM_OFFICE ) )
            {
                //VM_OFFICE
                VmRandomFeature vmRF = getVmRandomFeature();
                params.put( "VM_OFFICE." + VM_QUANTITY_VAR       ,""+vmRF.instances              );
                params.put( "VM_OFFICE." + VM_CORES_VAR          , "1"                           );
                params.put( "VM_OFFICE." + VM_CPU_SPEED_VAR      , "2.0"                         );
                params.put( "VM_OFFICE." + VM_MEMORY_SIZE_VAR    , "1024"                        );
                params.put( "VM_OFFICE." + VM_HARDDISK_SIZE_VAR  , "1"                           );
                params.put( "VM_OFFICE." + START_TIME_VAR        , vmRF.start                    );
                params.put( "VM_OFFICE." + END_TIME_VAR          , vmRF.end                      );
                params.put( "VM_OFFICE." + VM_ISOLATION_VAR      , "true"                        );
                params.put( "VM_OFFICE." + VM_PERSISTENCE_VAR    , "true"                        );
                params.put( "VM_OFFICE." + VM_IMAGE_VAR          , "http://my.image.VM_OFFICE"   );
            }
            if ( vms.contains( VM_DESIGN ) )
            {
                //VM_DESIGN
                VmRandomFeature vmRF = getVmRandomFeature();
                params.put( "VM_DESIGN." + VM_QUANTITY_VAR       , ""+vmRF.instances             );
                params.put( "VM_DESIGN." + VM_CORES_VAR          , "2"                           );
                params.put( "VM_DESIGN." + VM_CPU_SPEED_VAR      , "3.2"                         );
                params.put( "VM_DESIGN." + VM_MEMORY_SIZE_VAR    , "4096"                        );
                params.put( "VM_DESIGN." + VM_HARDDISK_SIZE_VAR  , "5"                           );
                params.put( "VM_DESIGN." + START_TIME_VAR        , vmRF.start                    );
                params.put( "VM_DESIGN." + END_TIME_VAR          , vmRF.end                      );
                params.put( "VM_DESIGN." + VM_ISOLATION_VAR      , "true"                        );
                params.put( "VM_DESIGN." + VM_PERSISTENCE_VAR    , "false"                       );
                params.put( "VM_DESIGN." + VM_IMAGE_VAR          , "http://my.image.VM_DESIGN"   );
            }
            if ( vms.contains( VM_RESEARCH ) )
            {
                //VM_RESEARCH
                VmRandomFeature vmRF = getVmRandomFeature();
                params.put( "VM_RESEARCH." + VM_QUANTITY_VAR     , ""+vmRF.instances             );
                params.put( "VM_RESEARCH." + VM_CORES_VAR        , "1"                           );
                params.put( "VM_RESEARCH." + VM_CPU_SPEED_VAR    , "2.4"                         );
                params.put( "VM_RESEARCH." + VM_MEMORY_SIZE_VAR  , "1024"                        );
                params.put( "VM_RESEARCH." + VM_HARDDISK_SIZE_VAR, "2"                           );
                params.put( "VM_RESEARCH." + START_TIME_VAR      , vmRF.start                    );
                params.put( "VM_RESEARCH." + END_TIME_VAR        , vmRF.end                      );
                params.put( "VM_RESEARCH." + VM_ISOLATION_VAR    , "true"                        );
                params.put( "VM_RESEARCH." + VM_PERSISTENCE_VAR  , "false"                       );
                params.put( "VM_RESEARCH." + VM_IMAGE_VAR        , "http://my.image.VM_RESEARCH" );
            }
            
            result.add( params );
        }
        
        return result;
    }
    
    public SLATemplate updateTemplate( Hashtable<String, String> params )
    {
        String selected = params.get( "VMTYPES" );
        String vms[] = selected.split( "," );
        
        for( String vm : vms )
        {
            update( vm, params );
        }
        
        // reset others VMs
        for ( String vm : VMTYPES_SET )
        {
            if ( !selected.contains( vm ) )
            {
                reset( vm );
            }
        }

        return iTemplate;
    }
    
    public SLATemplate getEndTemplate()
    {
        Hashtable<String, String> params = new Hashtable<String, String>();
        VmRandomFeature vmRF = getVmRandomFeature();
        params.put( "VM_RESEARCH." + VM_QUANTITY_VAR     , ""+vmRF.instances );
        params.put( "VM_RESEARCH." + VM_CORES_VAR        , "2"               );
        params.put( "VM_RESEARCH." + VM_CPU_SPEED_VAR    , "2.8"             );
        params.put( "VM_RESEARCH." + VM_MEMORY_SIZE_VAR  , "1024"            );
        params.put( "VM_RESEARCH." + VM_HARDDISK_SIZE_VAR, "10"              );
        params.put( "VM_RESEARCH." + START_TIME_VAR      , vmRF.start        );
        params.put( "VM_RESEARCH." + END_TIME_VAR        , vmRF.end          );
        params.put( "VM_RESEARCH." + VM_ISOLATION_VAR    , "true"            );
        params.put( "VM_RESEARCH." + VM_PERSISTENCE_VAR  , "false"           );
        params.put( "VM_RESEARCH." + VM_IMAGE_VAR        , "END_SIMULATION"  );
        update( "VM_RESEARCH", params );
        
        return iTemplate;
    }
    
    protected void update( String vm, Hashtable<String, String> params )
    {
        AgreementTerm agreementTerm = iTemplate.getAgreementTerm( vm + "_ID" );
        
        if ( agreementTerm != null )
        {
            ((Customisable)agreementTerm.getVariableDeclr( VM_QUANTITY_VAR      )).getValue().setValue( params.get( vm + ".VM_QUANTITY_VAR"      ));
            ((Customisable)agreementTerm.getVariableDeclr( VM_CORES_VAR         )).getValue().setValue( params.get( vm + ".VM_CORES_VAR"         ));
            ((Customisable)agreementTerm.getVariableDeclr( VM_CPU_SPEED_VAR     )).getValue().setValue( params.get( vm + ".VM_CPU_SPEED_VAR"     ));
            ((Customisable)agreementTerm.getVariableDeclr( VM_MEMORY_SIZE_VAR   )).getValue().setValue( params.get( vm + ".VM_MEMORY_SIZE_VAR"   ));
            ((Customisable)agreementTerm.getVariableDeclr( VM_HARDDISK_SIZE_VAR )).getValue().setValue( params.get( vm + ".VM_HARDDISK_SIZE_VAR" ));
            ((Customisable)agreementTerm.getVariableDeclr( START_TIME_VAR       )).getValue().setValue( params.get( vm + ".START_TIME_VAR"       ));
            ((Customisable)agreementTerm.getVariableDeclr( END_TIME_VAR         )).getValue().setValue( params.get( vm + ".END_TIME_VAR"         ));
            ((Customisable)agreementTerm.getVariableDeclr( VM_ISOLATION_VAR     )).getValue().setValue( params.get( vm + ".VM_ISOLATION_VAR"     ));
            ((Customisable)agreementTerm.getVariableDeclr( VM_PERSISTENCE_VAR   )).getValue().setValue( params.get( vm + ".VM_PERSISTENCE_VAR"   ));
            ((Customisable)agreementTerm.getVariableDeclr( VM_IMAGE_VAR         )).getValue().setValue( params.get( vm + ".VM_IMAGE_VAR"         ));
        }
        else System.out.println( "Unsupported VM Type :: "+vm );
    }
    
    protected void reset( String vm )
    {
        AgreementTerm agreementTerm = iTemplate.getAgreementTerm( vm + "_ID" );
        if ( agreementTerm != null )
        {
            ((Customisable)agreementTerm.getVariableDeclr( VM_QUANTITY_VAR )).getValue().setValue( "0" );
        }
        else System.out.println( "Unsupported VM Type :: "+vm );
    }
    
    public void negotiation( SLATemplate newTemplate )
    {
        try
        {
            String negotiationID = iNegotiation.initiateNegotiation( newTemplate );
            System.out.println( String.format( "NegotiationID='%s'", negotiationID ) );
            SLATemplate slaTemplates[] = iNegotiation.negotiate( negotiationID, newTemplate );
            System.out.println( String.format( "Counter-Offers returned='%n'", slaTemplates.length ) );
            SLA sla = iNegotiation.createAgreement( negotiationID, slaTemplates[0] );
            System.out.println( String.format( "SLA.AgreedAt='%s' SLA.EffectiveFrom='%s' SLA.EffectiveUntil='%s'",
                         sla.getAgreedAt(), sla.getEffectiveFrom(), sla.getEffectiveUntil() ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    public void setParties()
    {
        try
        {
            SLATemplate newTemplate = iTemplate;
            Party[] parties = newTemplate.getParties();
            Party[] modifiedParties = new Party[parties.length + 1];
            
            for ( int i = 0; i < parties.length; i++ )
            {
                    modifiedParties[ i ] = parties[ i ];
            }
             
            Party customerParty = new Party( new ID( "customer-id-254742" ), org.slasoi.slamodel.vocab.sla.customer );
            customerParty.setPropertyValue( org.slasoi.slamodel.vocab.sla.gslam_epr, "i-customer" );
            modifiedParties[parties.length] = customerParty;
            newTemplate.setParties(modifiedParties);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    protected String getRandomVMs()
    {
        String result = "";
        
        int index = (int)(Math.random()*10 % VMTYPES_SET.length );
        if ( index == 0 ) index++;
        
        Vector<String> selected = new Vector<String>( 3, 2 );
        while ( index > 0 )
        {
            int s = (int)(Math.random()*10 % VMTYPES_SET.length );
            if ( !selected.contains( VMTYPES_SET[ s ] ) )
            {
                selected.add( VMTYPES_SET[ s ] );
                index--;
            }
        }
        
        StringBuffer sb = new StringBuffer();
        int sz = selected.size();
        for ( int i = 0; i < sz; i++ )
        {
            sb.append( selected.get( i ) );
            if ( i < sz-1 )
            {
                sb.append( "," );
            }
        }
        result = sb.toString();
        
        return result;
    }
    
    protected VmRandomFeature getVmRandomFeature()
    {
        VmRandomFeature result = new VmRandomFeature();
        int s = (int)(Math.random()*10 % 5 );
        if ( s == 0 ) s++;
        
        result.instances = s;
        
        result.start = encode( new Date() );
        result.end   = encode( new Date() );
        
        return result;
    }
    
    protected String encode( Date dt )
    {
        String result = "";
        try
        {
            ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream( bytesOutput );
            os.writeObject( dt );
            result = new String( Base64.encodeBase64( bytesOutput.toByteArray() ) );
            os.close();
        }
        catch ( Exception e )
        {
        }
        
        return result;
    }
    
    class VmRandomFeature
    {
        int instances;
        String start ;
        String end   ;
        
        public String toString()
        {
            return String.format( "[%d,(%s:%s)]", instances, start, end );
        }
    }
    
    public static void main( String[] args )
    {
        RequestHelper nh = new RequestHelper( null, null );
        System.out.println( "VMs="+nh.getRandomVMs() );
        for ( int i = 0; i < 5; i++ )
        {
            System.out.println( nh.createRequests( 3 ) );
        }
    }
    
    protected INegotiation    iNegotiation;
    protected SLATemplate     iTemplate   ;
    
    private static final Logger LOGGER = Logger.getLogger( RequestHelper.class );
}
