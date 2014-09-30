package eu.ascetic.paas.applicationmanager;

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
 * @author David Garcia Perez. David Rojo, Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net david.rojoa@atos.net
 */

public class Dictionary {
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String USER_AGENT = "ASCETiC-Application-Manager/0.1";
	
	// SLA
	public static final String SLA_XMLNS = "http://www.slaatsoi.eu/slamodel";
	
	public static String DISK_SIZE_UNIT_GBYTE =  "byte * 2^30";
	public static String DISK_SIZE_UNIT_MBYTE =  "byte * 2^20";
	public static String DISK_SIZE_UNIT_KBYTE =  "byte * 2^10";
}
