package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl.BasiceEnergyModellerPredictor;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl.EnergyModellerPredictor;

public class PredictorBuilder {

	private static EnergyModellerPredictor predictor;
	
	public static PredictorInterface getPredictor(){
		if (predictor == null) predictor = new EnergyModellerPredictor();
		return (PredictorInterface)predictor ;
	}
	
	public static PredictorInterface getPredictor(String type){
		if (predictor == null) return (PredictorInterface)new BasiceEnergyModellerPredictor();
		return (PredictorInterface)predictor ;
	}
	
}
