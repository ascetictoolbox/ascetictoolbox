package eu.ascetic.architecture.iaas.slamanager.main.beans.client;

import java.io.File;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.slasoi.gslam.core.context.SLAManagerContext;
import org.slasoi.gslam.core.negotiation.INegotiation;
import org.slasoi.gslam.core.negotiation.ISyntaxConverter;
import org.slasoi.gslam.core.negotiation.ISyntaxConverter.SyntaxConverterType;
import org.slasoi.gslam.core.negotiation.SLATemplateRegistry.Metadata;
import org.slasoi.slamodel.sla.Party;
import org.slasoi.slamodel.sla.SLATemplate;

public class NegotiationHelper
{
    public void run( SLAManagerContext ctx )
    {
        try
        {
            // Loading of SLATemplate :: "a4-infrastructure-template.xml";
            String templateFN = System.getProperty( "template" );
            SLATemplate template = template( templateFN, ctx );
            
            // Getting Negotiation interface of dsSLAM (via its context)
            INegotiation negotiation = negotiation( ctx );
            
            // Starts negotiation through this helper
            System.out.println( String.format( "Using template:\n%s", template ) );
            RequestHelper helper = new RequestHelper( negotiation, template );
            helper.run( helper );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        } 
    }
    
    // -----------------------------------------------------------------------
    //  HELPER METHODS 
    
    protected String getProviderUUID( SLATemplate tmpl )
    {
        String result = "";
        Party[] parties = tmpl.getParties();
        for ( Party p : parties )
        {
            if ( p.getAgreementRole().getValue().equalsIgnoreCase( "provider" ) )
            {
                result = p.getId().getValue();
                break;
            }
        }
        return result;
    }
    
    protected INegotiation negotiation( SLAManagerContext ctx )
    {
        try
        {
            Hashtable<SyntaxConverterType, ISyntaxConverter> syntaxConverters = ctx.getSyntaxConverters();
            ISyntaxConverter syc = syntaxConverters.get( SyntaxConverterType.SLASOISyntaxConverter );
            INegotiation iNegotiation = syc.getNegotiation(); // "http://localhost:8080/services/ISNegotiation?wsdl"
            
            return iNegotiation;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return null;
    }

    
    protected SLATemplate template( String location, SLAManagerContext ctx )
    {
        try
        {
            Hashtable<SyntaxConverterType, ISyntaxConverter> syntaxConverters = ctx.getSyntaxConverters();
            ISyntaxConverter slasoi = syntaxConverters.get( SyntaxConverterType.SLASOISyntaxConverter );

            String slaTemplateXml = FileUtils.readFileToString( new File( location ), "UTF-8" );
            SLATemplate slaTemplate = (SLATemplate)slasoi.parseSLATemplate( slaTemplateXml );

            Metadata md = new Metadata();

            md.setPropertyValue( Metadata.registrar_id , "md:auto_registered_from_service_ads" );
            md.setPropertyValue( Metadata.provider_uuid, ctx.getSLAManagerID() + getProviderUUID( slaTemplate ) );
            md.setPropertyValue( Metadata.template_uuid, slaTemplate.getUuid().getValue() );

            return slaTemplate;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
}
