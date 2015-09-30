package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.impl;

import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;

public class EMNeuralPredictor implements PredictorInterface{

	@Override
	public double estimate(String providerid, String applicationid,	String deploymentid, List<String> vmids, String eventid, Unit unit, long timelater) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setEnergyService(EnergyDataAggregatorServiceQueue service) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double estimate(List<DataConsumption> samples, Unit unit,
			long timelater) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double estimate(String providerid, String applicationid,
			String deploymentid, String vm, String eventid, Unit unit,
			long timelater) {
		// TODO Auto-generated method stub
		return 0;
	}

}
