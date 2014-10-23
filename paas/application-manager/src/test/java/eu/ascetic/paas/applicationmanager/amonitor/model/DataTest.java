package eu.ascetic.paas.applicationmanager.amonitor.model;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

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
 * email: david.garciaperez@atos.net
 * 
 * Test the JSON Pojo Data Class	
 */
public class DataTest {

	@Test
	public void pojo() {
		Data data = new Data();
		data.setStart("start");
		data.setEnd("end");
		data.setPower("power");
		
		assertEquals("start", data.getStart());
		assertEquals("end", data.getEnd());
		assertEquals("power", data.getPower());
	}
	
	@Test
	public void testingTimestamps() {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String dateInString1 = "17-Oct-2014";
		String dateInString2 = "18-Oct-2014";
	 
		try {
	 
			Date date1 = formatter.parse(dateInString1);
			System.out.println(date1 + " "  + date1.getTime());
			System.out.println(formatter.format(date1));
			Date date2 = formatter.parse(dateInString2);
			System.out.println(date2 + " "  + date2.getTime());
			System.out.println(formatter.format(date2));
	 
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
