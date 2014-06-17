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
package eu.ascetic.vmc.api.core;

/**
 * Manageable interface (via JMX) for responding to events regarding the domain
 * and manages the actual context changes
 * 
 * @author Django Armstrong (ULeeds), Daniel Espling (UMU)
 * @version 0.0.1
 */
public interface DomainContextualizerMBean {

	/**
	 * Domain started event handler
	 */
	void vmDomainStarted();

	/**
	 * Domain stopped event handler
	 */
	void vmDomainStopped();

	/**
	 * Domain migration started event handler
	 */
	void vmDomainMigrationStarted();

	/**
	 * Domain migration completed event handler
	 */
	void vmDomainMigrationCompleted();

}