import java.util.Timer;


public class ServiceDataCollectorManager {

	public void init(){
		System.out.println("Running scheduler");
		Timer timer = new Timer();
		timer.schedule(new Scheduler(), 0,  30000);
		
	}
	
	public static void main(String args[]) {
		System.out.println("Running scheduler");
		Timer timer = new Timer();
		timer.schedule(new Scheduler(), 0, 30000);
		// now to schedule the predictor
	}	
	
	
	
}
