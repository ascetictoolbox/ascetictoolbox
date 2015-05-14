import es.bsc.amon.watch.MetricWatch;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.junit.Test;

public class MetricWatchTest extends TestCase {
	@Test
	public void testSomething() {
		System.out.println(MetricWatch.Threshold.LT.forValue("hola perraco"));
		System.out.println(MetricWatch.Threshold.LT.forValue(this));
		System.out.println(MetricWatch.Threshold.LT.forValue(1));
		System.out.println(MetricWatch.Threshold.LT.forValue(1.1f));
	}
}
