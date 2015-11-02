package eu.ascetic.paas.applicationmanager.rest.util;

import static org.junit.Assert.assertEquals;

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
 * email david.garciaperez@atos.net
 * 
 * Test the formated date...
 */
public class DateUtilTest {

	@Test
	public void getDateStringLogStandardFormatTest() throws Exception {
		String oldstring = "2011-01-18 00:00:00.0";
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(oldstring);
		
		String output = DateUtil.getDateStringLogStandardFormat(date);
		
		assertEquals("18/01/2011:00:00:00 +0100", output);
	}
}
