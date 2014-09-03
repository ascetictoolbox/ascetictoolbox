/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.iaas.slamanager.poc.utils;

import java.util.ArrayList;
import java.util.Vector;

import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.vocab.core;
import org.slasoi.slamodel.vocab.resources;

import eu.ascetic.iaas.slamanager.poc.enums.AsceticAgreementTerm;
import eu.ascetic.iaas.slamanager.poc.enums.OperatorType;

public class AgreementUtil {

	private static Vector<String> resourceTerms;

	private static ArrayList<String> genericTerms;

	static {
		setResourceTerms();
		setGenericTerms();
	}

	public static AsceticAgreementTerm metricToAgreement(String metric) {
		metric = metric.toLowerCase();
		if (metric.equals("vs_location"))
			return AsceticAgreementTerm.vs_location;
		if (metric.equals("storage_location"))
			return AsceticAgreementTerm.storage_location;
		if (metric.equals("cpu_speed"))
			return AsceticAgreementTerm.cpu_speed;
		if (metric.equals("vm_cores"))
			return AsceticAgreementTerm.vm_cores;
		if (metric.equals("memory"))
			return AsceticAgreementTerm.memory;
		if (metric.equals("disk_size"))
			return AsceticAgreementTerm.disk_size;
		if (metric.equals("reserve"))
			return AsceticAgreementTerm.reserve;
		if (metric.equals("co_location_rack"))
			return AsceticAgreementTerm.co_location_rack;
		if (metric.equals("not_co_location_host"))
			return AsceticAgreementTerm.not_co_location_host;
		if (metric.equals("vm_cpu_load"))
			return AsceticAgreementTerm.vm_cpu_load;
		if (metric.equals("availability"))
			return AsceticAgreementTerm.availability;
		if (metric.equals("reliability"))
			return AsceticAgreementTerm.reliability;
		if (metric.equals("minimum_loa"))
			return AsceticAgreementTerm.minimum_loa;
		if (metric.equals("power_usage_per_vm"))
			return AsceticAgreementTerm.power_usage_per_vm;
		return null;
	}

	public static String agreementToMetric(AsceticAgreementTerm agreement) {
		String metric = null;
		switch (agreement) {
		case vs_location:
			metric = "vs_location";
			break;
		case storage_location:
			metric = "storage_location";
			break;
		case vm_cores:
			metric = "vm_cores";
			break;
		case cpu_speed:
			metric = "cpu_speed";
			break;
		case memory:
			metric = "memory";
			break;
		case disk_size:
			metric = "disk_size";
			break;
		case reserve:
			metric = "reserve";
			break;
		case co_location_rack:
			metric = "co_location_rack";
			break;
		case not_co_location_host:
			metric = "not_co_location_host";
			break;
		case vm_cpu_load:
			metric = "vm_cpu_load";
			break;
		case availability:
			metric = "availability";
			break;
		case reliability:
			metric = "reliability";
			break;
		case minimum_loa:
			metric = "minimum_loa";
			break;
		case power_usage_per_vm:
			metric = "power_usage_per_vm";
			break;
		default:
			break;
		}
		return metric;
	}

	public static STND agreementToResource(AsceticAgreementTerm agreement) {
		STND resource = null;
		switch (agreement) {
		case vs_location:
			resource = resources.vs_location;
			break;
		case storage_location:
			resource = resources.storage_location;
			break;
		case vm_cores:
			resource = resources.vm_cores;
			break;
		case cpu_speed:
			resource = resources.cpu_speed;
			break;
		case memory:
			resource = resources.memory;
			break;
		case disk_size:
			resource=resources.disk_size;
			break;
		case reserve:
			resource = resources.reserve;
			break;
		case co_location_rack:
			resource = resources.co_location_rack;
			break;
		case not_co_location_host:
			resource = resources.not_co_location_host;
			break;
		case vm_cpu_load:
			resource = resources.vm_cpu_load;
			break;
		case availability:
			resource = resources.availability;
			break;
		case reliability:
			resource = resources.reliability;
			break;
		case minimum_loa:
			resource = resources.minimum_loa;
			break;
		case power_usage_per_vm:
			resource = resources.power_usage_per_vm;
			break;
		default:
			break;
		}
		return resource;
	}

	public static boolean isResourceTerm(String term) {

		for (int l = 0; l < resourceTerms.size(); l++) {

			if (resourceTerms.elementAt(l).equals(term.toLowerCase()))
				return true;
		}
		return false;
	}

	public static boolean isGenericTerms(String term) {
		for (int i = 0; i < genericTerms.size(); i++) {
			if (genericTerms.get(i).equalsIgnoreCase(term))
				return true;
		}
		return false;
	}

	public static void setResourceTerms() {
		resourceTerms = new Vector<String>();
		resourceTerms.add("vm_cores");
		resourceTerms.add("cpu_speed");
		resourceTerms.add("memory");
		resourceTerms.add("disk_size");
	}

	public static void setGenericTerms() {
		genericTerms = new ArrayList<String>();
		genericTerms.add("vs_location");
		genericTerms.add("storage_location");
		genericTerms.add("reserve");
		genericTerms.add("co_location_rack");
		genericTerms.add("not_co_location_host");
		genericTerms.add("reliability");
		genericTerms.add("power_usage_per_vm");
	}

	public static String getStringTerm(Guaranteed.State gs) {
		TypeConstraintExpr ce = (TypeConstraintExpr) gs.getState();
		FunctionalExpr fe = (FunctionalExpr) ce.getValue();
		String operator = fe.getOperator().getValue();
		int c = operator.indexOf('#');
		String agreementTermName = operator.substring(c + 1);
		return agreementTermName;
	}

	public static STND convertToSTND(OperatorType op) {
		STND stnd = null;
		if (op.equals(OperatorType.LESS))
			stnd = core.less_than;
		if (op.equals(OperatorType.EQUALS))
			stnd = core.equals;
		if (op.equals(OperatorType.GREATER))
			stnd = core.greater_than;
		if (op.equals(OperatorType.GREATER_EQUAL))
			stnd = core.greater_than_or_equals;
		if (op.equals(OperatorType.LESS_EQUAL))
			stnd = core.less_than_or_equals;
		if (op.equals(OperatorType.NOT_EQUALS))
			stnd = core.not_equals;
		return stnd;
	}

}
