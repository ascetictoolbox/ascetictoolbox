package eu.ascetic.paas.applicationmanager.rest.util;

import java.text.SimpleDateFormat;
import java.util.Date;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Utils to format dates... 
 *
 */
public class DateUtil {

	/**
	 * Formats a data using standard common log format
	 * @param date to be converted to standard date format
	 * @return the formated string
	 */
	public static String getDateStringLogStandardFormat(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss Z");
        return dateFormat.format(date);
	}
}
