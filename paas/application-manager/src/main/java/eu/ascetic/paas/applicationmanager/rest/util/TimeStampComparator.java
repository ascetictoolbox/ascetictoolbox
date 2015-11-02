package eu.ascetic.paas.applicationmanager.rest.util;

import java.sql.Timestamp;


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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Utility to compare dates tto be used in the rEST interface
 */
public class TimeStampComparator {

	/**
	 * Returns <code>true</code> if ts2 is still in the future with respect ts1. 
	 * @param ts1 reference date
	 * @param ts2 date that we want to see if it is in the future
	 * @return <code>true</code> if ts2 is in the future with respect ts1
	 */
	public static boolean isInTheFuture(Timestamp ts1, Timestamp ts2) {
		long ts1MilliSeconds = ts1.getTime();
		long ts2MilliSeconds = ts2.getTime();
		
		if(ts2MilliSeconds > ts1MilliSeconds) return true;
		else return false;
	}
}
