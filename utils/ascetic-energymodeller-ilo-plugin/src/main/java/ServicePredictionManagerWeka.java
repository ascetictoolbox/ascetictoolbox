import java.util.Timer;

import prediction.WekaMultiVarPredictor;
import utility.DbUtility;


public class ServicePredictionManagerWeka {

	public void init(){
		System.out.println("Running scheduler");
		Timer timer = new Timer();
		WekaMultiVarPredictor predictor = new WekaMultiVarPredictor();
		DbUtility dbutility = new DbUtility();
		dbutility.init();
		predictor.setDbUtility(dbutility );
		timer.schedule(predictor, 0,  30000);
		
	}
	
	public static void main(String args[]) {
		System.out.println("Running scheduler");
		Timer timer = new Timer();
		WekaMultiVarPredictor predictor = new WekaMultiVarPredictor();
		DbUtility dbutility = new DbUtility();
		dbutility.init();
		predictor.setDbUtility(dbutility );
		timer.schedule(predictor, 0,  30000);
	}
	
}
