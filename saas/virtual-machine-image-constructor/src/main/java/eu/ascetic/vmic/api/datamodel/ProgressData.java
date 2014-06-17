/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.vmic.api.datamodel;

import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * A class to store the progress data of a service. Contains statics for ID.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.1
 */
public class ProgressData {

	public static final double COMPLETED_PERCENTAGE = 100.0;

	/**
	 * The current percentage completion
	 */
	private Double currentPercentageCompletion;

	/**
	 * Used to store an altered version of the ovfDefinition for passing image URI's
	 * back through the API.
	 */
	private OvfDefinition ovfDefinition = null;

	/**
	 * Defines when the VMIC has completed constructing an image.
	 */
	private boolean complete = false;

	/**
	 * Defines if the VMIC has encountered an unrecoverable exception.
	 */
	private boolean error = false;

	/**
	 * Exception causing VMIC to error out.
	 */
	private Exception exception = null;
	
	/**
	 * Constructor with default progress values.
	 */
	public ProgressData() {
		currentPercentageCompletion = 0.0;
	}

	/**
	 * @return the currentPercentageCompletion
	 */
	public Double getCurrentPercentageCompletion() {
		return currentPercentageCompletion;
	}

	/**
	 * @param currentPercentageCompletion
	 *            the currentPercentageCompletion to set
	 */
	public void setCurrentPercentageCompletion(
			Double currentPercentageCompletion) {
		this.currentPercentageCompletion = currentPercentageCompletion;
	}

	/**
	 * The ovfDefinition altered by the VMIC during the generation process.
	 * 
	 * @return the ovfDefinition.
	 */
	public OvfDefinition getOvfDefinition() {
		return ovfDefinition;
	}

	/**
	 * @param ovfDefinition
	 *            the ovfDefinition to set.
	 */
	public void setOvfDefinition(OvfDefinition ovfDefinition) {
		this.ovfDefinition = ovfDefinition;
	}

	/**
	 * Defines if the VMC has completed contextualization.
	 * 
	 * @return the complete
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @param complete
	 *            the complete to set
	 */
	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	/**
	 * @return the error
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(boolean error) {
		this.error = error;
	}

	/**
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @param exception
	 *            the exception to set
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}
}
