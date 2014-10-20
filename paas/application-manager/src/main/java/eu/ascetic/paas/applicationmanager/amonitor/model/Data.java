package eu.ascetic.paas.applicationmanager.amonitor.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail: david.garciaperez@atos.net 
 * 
 * Common methods for all the rest APIs
 * 
 * 

{ "appId" : "davidgpTestApp",
  "instanceId" : "100",
  "data" : {
         "start" : "1413535514549",
         "end" : "1413535614539",
         "power" : "10 Wh"
     }
}

 *
 */

@JsonPropertyOrder({ "start", "end", "power"})
@JsonRootName(value = "data")
public class Data {
	@JsonProperty("start")
	private String start;
	@JsonProperty("end")
	private String end;
	@JsonProperty("power")
	private String power;
	
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	
	public String getPower() {
		return power;
	}
	public void setPower(String power) {
		this.power = power;
	}
}
