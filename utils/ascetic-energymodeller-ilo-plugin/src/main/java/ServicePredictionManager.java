import java.util.Timer;

import prediction.Predictor;
import utility.DbUtility;


public class ServicePredictionManager {

	public void init(){
		System.out.println("Running scheduler");
		Timer timer = new Timer();
		Predictor predictor = new Predictor();
		DbUtility dbutility = new DbUtility();
		dbutility.init();
		predictor.setDbUtility(dbutility );
		timer.schedule(predictor, 0,  30000);
		
	}
	
	public static void main(String args[]) {
		System.out.println("Running scheduler");
		Timer timer = new Timer();
		Predictor predictor = new Predictor();
		DbUtility dbutility = new DbUtility();
		dbutility.init();
		predictor.setDbUtility(dbutility );
		timer.schedule(predictor, 0,  30000);
	}
	
}
