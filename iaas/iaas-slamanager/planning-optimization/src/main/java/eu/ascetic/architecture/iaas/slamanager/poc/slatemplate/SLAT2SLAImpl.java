package eu.ascetic.architecture.iaas.slamanager.poc.slatemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.slasoi.slamodel.primitives.TIME;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

public class SLAT2SLAImpl
{
    public SLATemplate slat;
    public SLA sla;

    public SLAT2SLAImpl(
                         SLATemplate slat )
    {
        this.slat = slat;
        this.sla = new SLA();
    }

    public SLA transfer()
    {
        this.copy();
        return this.sla;
    }

    private void copy()
    {
        // set SLA ID
        sla.setUuid( new org.slasoi.slamodel.primitives.UUID( java.util.UUID.randomUUID().toString() ) );
        // set Template ID
        this.sla.setTemplateId( this.slat.getUuid() );
        // set AgreedAt
        this.sla.setAgreedAt( new TIME( SLAT2SLAImpl.getCurrentTime_yyyyMMMddHH_mm_ss() ) );
        // create now time
        this.sla.setAgreedAt( new TIME( SLAT2SLAImpl.getCurrentTime_yyyyMMMddHH_mm_ss() ) );
        // set effective From
        this.sla.setEffectiveFrom( new TIME( SLAT2SLAImpl.getEffectiveFromTime_yyyyMMMddHH_mm_ss() ) );
        // set effective Until
        this.sla.setEffectiveUntil( new TIME( SLAT2SLAImpl.getEffectiveUntilTime_yyyyMMMddHH_mm_ss() ) );
        // set Template ID
        this.sla.setTemplateId( this.slat.getUuid() );
        // set parties
        this.sla.setParties( this.slat.getParties() );
        // set InterfaceDeclrs
        this.sla.setInterfaceDeclrs( this.slat.getInterfaceDeclrs() );
        // set agreementTerms
        this.sla.setAgreementTerms( this.slat.getAgreementTerms() );
        //this.sla.setPropertyValue( PlanHandlerImpl.PLAN_ID_SLA, java.util.UUID.randomUUID().toString() );
    }

    /**
     * Creates the current time.
     */
    public static Calendar getCurrentTime_yyyyMMMddHH_mm_ss()
    {
        final SimpleDateFormat formatter = new SimpleDateFormat( "MMM dd,yyyy HH:mm:ss", Locale.US );
        formatter.getCalendar().setTime( new Date() );
        return formatter.getCalendar();
    }

    /**
     * Creates the effective from time.
     */
    public static Calendar getEffectiveFromTime_yyyyMMMddHH_mm_ss()
    {
        final SimpleDateFormat formatter = new SimpleDateFormat( "MMM dd,yyyy HH:mm:ss", Locale.US );
        Date date = new Date();
        // date.setYear(110);
        formatter.getCalendar().setTime( date );
        return formatter.getCalendar();
    }

    /**
     * Creates the effective until time.
     */
    @SuppressWarnings( "deprecation" )
    public static Calendar getEffectiveUntilTime_yyyyMMMddHH_mm_ss()
    {
        final SimpleDateFormat formatter = new SimpleDateFormat( "MMM dd,yyyy HH:mm:ss", Locale.US );
        Date date = new Date();
        date.setYear( date.getYear() + 2 );
        formatter.getCalendar().setTime( date );
        return formatter.getCalendar();
    }

}
