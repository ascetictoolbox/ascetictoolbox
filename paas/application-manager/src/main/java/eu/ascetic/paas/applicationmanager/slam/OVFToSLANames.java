package eu.ascetic.paas.applicationmanager.slam;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Util class that stores all the nomenclatura to translate names between OVF to SLA Template
 *
 */
public class OVFToSLANames {
	// APP WORDS
	public static final String APP_ENERGY_CONSUMPTION_OVF = "app_energy_consumption";
	public static final String APP_ENERGY_CONSUMPTION_SLA = "Energy_Usage_per_app";
	public static final String APP_ENERGY_CONSUMPTION_SLA_OPERATOR = "http://www.slaatsoi.org/resources#energy_usage_per_app";
	public static final String APP_POWER_CONSUMPTION_OVF = "app_power_consumption";
	public static final String APP_POWER_CONSUMPTION_SLA = "Power_Usage_per_app";
	public static final String APP_POWER_CONSUMPTION_SLA_OPERATOR = "http://www.slaatsoi.org/resources#power_usage_per_app";
	public static final String APP_PRICE_PER_HOUR_OVF = "app_price_for_next_hour";
	public static final String APP_PRICE_PER_HOUR_SLA = "App_Price_for_next_hour";
	public static final String APP_PRICE_PER_HOUR_SLA_OPERATOR = "http://www.slaatsoi.org/resources#app_price_for_next_hour";
	
	// VM WORDS
	public static final String VM_GUARANTEES = "_Guarantees";
	public static final String POWER_USAGE_PER_VM_OVF = "power_usage_per_vm";
	public static final String POWER_USAGE_PER_VM_SLA = "Power_Usage_for_";
	public static final String POWER_USAGE_PER_VM_SLA_OPERATOR = "http://www.slaatsoi.org/resources#power_usage_per_vm";
	public static final String ENERGY_USAGE_PER_VM_OVF = "energy_usage_per_vm";
	public static final String ENERGY_USAGE_PER_VM_SLA = "Energy_Usage_for_";
	public static final String ENERGY_USAGE_PER_VM_SLA_OPERATOR = "http://www.slaatsoi.org/resources#energy_usage_per_vm";
	
	// OVF SLA TERMS
	public static final String OVF_ITEM = "OVF-Item-";
	public static final String SUBSET_OF ="http://www.slaatsoi.org/coremodel#subset_of";
	public static final String VM_TYPE = "VM_of_type_";
	
	// Aggregated Terms
	public static final String AGGREGATED_METRIC_SLA = "Aggregated Guarantees";
	public static final String AGGREGATED_METRIC_SLA_OPERATOR ="http://www.slaatsoi.org/resources#aggregated_event_metric_over_period";
	
	// XML DATATYPPES
	public static final String DATATYPE_STRING = "http://www.w3.org/2001/XMLSchema#string";
	public static final String DATATYPE_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
	public static final String DATATYPE_DECIMAL = "http://www.w3.org/2001/XMLSchema#decimal";
	
	// Metric Units
	public static final Map<String, String> METRIC_UNITS = ImmutableMap.of(  
				"WattHour", "http://www.w3.org/2001/XMLSchema#watthour",
				"Watt", "http://www.w3.org/2001/XMLSchema#watt",
				"EUR", "http://www.w3.org/2001/XMLSchema#EUR"
			);
	
	// Metric Units
	public static final Map<String, String> INVERSE_METRIC_UNITS = ImmutableMap.of(
				"http://www.w3.org/2001/XMLSchema#watt", "Watt",
				"http://www.slaatsoi.org/coremodel/units#W", "Watt"
			);
	
	// Comparators
	public static final Map<String, String> COMPARATORS = ImmutableMap.of(
				"LT", "http://www.slaatsoi.org/coremodel#less_than",
				"LTE", "http://www.slaatsoi.org/coremodel#less_than_or_equals",
				"EQ", "http://www.slaatsoi.org/coremodel#equals",
				"GT", "http://www.slaatsoi.org/coremodel#greater_than",
				"GTE", "http://www.slaatsoi.org/coremodel#greater_than_or_equals"
			);
}
