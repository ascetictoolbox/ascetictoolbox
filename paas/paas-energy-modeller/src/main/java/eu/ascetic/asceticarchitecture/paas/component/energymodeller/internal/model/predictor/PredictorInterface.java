package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor;

import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceZabbix;

public interface PredictorInterface {
	
	double estimate(String providerid, String applicationid,List<String> vmids, String eventid, Unit unit, long timelater);
	
	void setEnergyService(EnergyDataAggregatorServiceZabbix service);
	
}
