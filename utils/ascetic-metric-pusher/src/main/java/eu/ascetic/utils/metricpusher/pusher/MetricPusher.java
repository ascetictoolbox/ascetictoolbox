package eu.ascetic.utils.metricpusher.pusher;

import eu.ascetic.utils.metricpusher.collector.CurrentMetricPusherFactory;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class is the responsible of launching the metric pusher
 *
 */

public class MetricPusher {

	public static boolean SHOW_DEBUG_TRACES = false;
	
	public static void main( String[] args )
	{ 
		//Generate sender factory
		CurrentMetricPusherFactory metricPusherFactory = new CurrentMetricPusherFactory();
		metricPusherFactory.start();
	}
	
}
