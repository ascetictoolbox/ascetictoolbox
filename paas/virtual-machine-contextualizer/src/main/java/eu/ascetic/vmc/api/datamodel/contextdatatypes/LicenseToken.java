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
package eu.ascetic.vmc.api.DataModel.ContextDataTypes;

import java.util.Arrays;

/**
 * Class for storing the attributes of a license token.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.3
 */
public class LicenseToken {

	private int id;
	private byte[] token;

	/**
	 * Default constructor.
	 * 
	 * @param token
	 *            String representation of the a license token.
	 */
	public LicenseToken(int id, byte[] initialToken) {
		this.id = id;
		if (initialToken == null) {
			this.token = new byte[0];
		} else {
			this.token = Arrays.copyOf(initialToken, initialToken.length);
		}	
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the token
	 */
	public byte[] getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public void setToken(byte[] newToken) {
		if (newToken == null) {
			this.token = new byte[0];
		} else {
			this.token = Arrays.copyOf(newToken, newToken.length);
		}
	}
}
