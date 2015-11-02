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

package eu.ascetic.paas.slam.poc.impl.provider.selection;

import java.util.Comparator;

import org.slasoi.slamodel.sla.SLATemplate;

public class WeightedSlat {

	private SLATemplate slat;
	private double weight;
		
	public WeightedSlat(SLATemplate slat, double weight) {
			this.slat = slat;
			this.weight = weight;
		}

	
	public static class WeightComparator implements Comparator<WeightedSlat> {
	    public int compare(WeightedSlat e1, WeightedSlat e2) {
//	        return (e1.weight > e2.weight) ? 0 : 1;
	    	if (e1.weight > e2.weight) return 1;
	    	else if (e1.weight < e2.weight) return -1;
	    	else return 0;
	    }
	}


	public SLATemplate getSlat() {
		return slat;
	}


	public double getWeight() {
		return weight;
	}


	
}
