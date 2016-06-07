package eu.ascetic.providerregistry.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * @email david.garciaperez@atos.net 
 * 
 * Common methods for the REST API.
 * 
 */
public abstract class RestAbstract {

	protected Response buildResponse(Response.Status status, String payload) {
		ResponseBuilder builder = Response.status(status);
		builder.entity(payload);
		return builder.build();
	}
}
