package eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces;

import java.sql.Timestamp;

public interface DataAggregatorTaskInterface {
	double getTotal(String app, String depl, String event);
	double getTotal(String app, String depl, String vmid, String event);
	double getAverage(String app, String depl, String vmid, String event);
	double getTotalAtTime(String app, String depl, String event, Timestamp time);
	double getTotalAtTime(String app, String depl, String vmid, String event, Timestamp time);
}
