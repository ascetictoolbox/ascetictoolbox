import es.bsc.amon.mq.dispatch.AppEstimationsReader;
import static org.fest.assertions.Assertions.*;

import org.junit.Ignore;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by mmacias on 23/6/16.
 */
public class DateFormatTest {
    @Ignore
    @Test
    public void dateTest() throws ParseException {

        DateFormat dateFormat = AppEstimationsReader.emDateFormat; // new SimpleDateFormat("d MMM yyyy HH:mm:ss z", Locale.ENGLISH);

        Calendar c = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(dateFormat.parse("8 Apr 2016 08:57:56 GMT"));

        assertThat(c.get(Calendar.DAY_OF_MONTH)).isEqualTo(8);
        assertThat(c.get(Calendar.MONTH)).isEqualTo(Calendar.APRIL);
        assertThat(c.get(Calendar.ZONE_OFFSET)).isEqualTo(0);
        assertThat(c.get(Calendar.HOUR_OF_DAY)).isEqualTo(8);
    }
}
