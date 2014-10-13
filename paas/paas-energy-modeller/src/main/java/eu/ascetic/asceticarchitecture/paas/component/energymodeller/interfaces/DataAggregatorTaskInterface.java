/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

import java.sql.Timestamp;
import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySamples;

public interface DataAggregatorTaskInterface {
	double getTotal(String app, String depl, String vmid, String event);
	double getAverage(String app, String depl, String vmid, String event);
	double getAverageInInterval(String app, String vmid, String event,long start, long end);
	//List<EnergySamples> getSamplesInInterval(String app, String depl, String vmid, String event,Timestamp start, Timestamp end, long freq);

}
