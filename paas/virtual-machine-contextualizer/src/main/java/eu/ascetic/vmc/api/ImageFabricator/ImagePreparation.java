/**
 *  Copyright 2013 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.vmc.api.ImageFabricator;

import eu.ascetic.vmc.api.DataModel.Service;

/**
 * TODO: Description here...
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.1
 */
public class ImagePreparation {

	private Service service;
	
	/**
	 * TODO
	 */
	public ImagePreparation () {
		service = new Service(null);
	}

	/**
	 * FIXME: Not currently used
	 * 
	 * @return the service
	 */
	public Service getService() {
		return service;
	}

	/**
	 * FIXME: Not currently used
	 * 
	 * @param service the service to set
	 */
	public void setService(Service service) {
		this.service = service;
	}
}
