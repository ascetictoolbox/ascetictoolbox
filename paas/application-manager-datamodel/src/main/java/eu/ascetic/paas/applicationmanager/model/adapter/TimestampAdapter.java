package eu.ascetic.paas.applicationmanager.model.adapter;

import java.sql.Timestamp;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

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
 * Helps JAXB to convert between Timestamps objects to XML
 *
 */
public class TimestampAdapter extends XmlAdapter<Date, Timestamp>  {

	public Date marshal(Timestamp v) {
		return new Date(v.getTime());
	}

	public Timestamp unmarshal(Date v) {
		return new Timestamp(v.getTime());
	}
}
