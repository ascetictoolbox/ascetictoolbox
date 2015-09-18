package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor;

import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.legacy.EnergyDataAggregatorServiceZabbix;

public interface PredictorInterface {
	
	double estimate(String providerid, String applicationid, String deploymentid, List<String> vmids, String eventid, Unit unit, long timelater);
	
	double estimate(List<DataConsumption> samples, Unit unit, long timelater);
	
	void setEnergyService(EnergyDataAggregatorServiceZabbix service);
	
	
	
}
