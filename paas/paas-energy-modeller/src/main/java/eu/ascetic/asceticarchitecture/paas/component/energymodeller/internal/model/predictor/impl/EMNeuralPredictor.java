package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl;

import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;

public class EMNeuralPredictor implements PredictorInterface{

	@Override
	public double estimate(String providerid, String applicationid,	List<String> vmids, String eventid, Unit unit, long timelater) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setEnergyService(EnergyDataAggregatorService service) {
		// TODO Auto-generated method stub
		
	}

}
