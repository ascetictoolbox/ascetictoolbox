import org.junit.Test;


public class TestScheduler {

	private ServicePredictionManager manager;
	
	@Test
	public void testScheduler(){
		manager = new ServicePredictionManager();
		manager.init();
	}
	
}
