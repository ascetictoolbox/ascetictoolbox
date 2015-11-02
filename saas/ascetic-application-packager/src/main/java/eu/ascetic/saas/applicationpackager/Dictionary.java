package eu.ascetic.saas.applicationpackager;

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
 * This class contains values for constraints in the project
 *
 */

public class Dictionary {

	public static String DISK_SIZE_UNIT_GBYTE =  "byte * 2^30";
	public static String DISK_SIZE_UNIT_MBYTE =  "byte * 2^20";
	public static String DISK_SIZE_UNIT_KBYTE =  "byte * 2^10";
	
	public static String GB = "GB";
	public static String MB = "MB";
	
	public static String CPU_SPEED_UNIT_GHZ =  "hertz * 2^30";
	public static String CPU_SPEED_UNIT_MHZ =  "hertz * 2^20";
	
	public static String GHZ = "GHz";
	public static String MHZ = "MHz";
	
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_TYPE_XML = "application/xml";
	public static final String USER_AGENT = "ASCETiC-Application-Packager/0.1"; 
	
}
