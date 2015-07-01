package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl.EnergyModellerPredictor;

public class PredictorBuilder {

	EnergyModellerPredictor predictor;
	
	public PredictorInterface getPredictor(){
		return (PredictorInterface) new EnergyModellerPredictor();
	}
	
}
