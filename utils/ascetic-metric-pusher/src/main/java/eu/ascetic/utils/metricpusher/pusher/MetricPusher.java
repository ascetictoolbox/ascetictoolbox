package eu.ascetic.utils.metricpusher.pusher;

import eu.ascetic.utils.metricpusher.collector.CurrentMetricPusherFactory;


public class MetricPusher {

	public static void main( String[] args )
	{ 
		//Generate sender factory
		CurrentMetricPusherFactory metricPusherFactory = new CurrentMetricPusherFactory();
		metricPusherFactory.start();
	}
	
}
