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
package eu.ascetic.utils.ovf.api.enums;

import eu.ascetic.utils.ovf.api.ProductProperty;

/**
 * Enumeration containing predefined constants of virtual resource types used in
 * {@link ProductProperty}.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public enum ProductPropertyType {

	/**
	 * Enumeration containing all possible product property primitive types.
	 */
	// @formatter:off
	UINT8("uint8"), SINT8("sint8"), UINT16("uint16"), SINT16("sint16"), 
	UINT32("uint32"), SINT32("sint32"), UINT64("uint64"), SINT64("sint64"), 
	STRING("string"), BOOLEAN("boolean"), REAL32("real32"), REAL64("real64");
	// @formatter:on

	/**
	 * Stores the String representation of this ProductPropertyType.
	 */
	private String type;

	/**
	 * Default constructor.
	 * 
	 * @param type
	 *            The String representation of the ProductPropertyType
	 */
	private ProductPropertyType(String type) {
		this.type = type;
	}

	/**
	 * Returns the string representation of this ProductPropertyType.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Finds the ProductPropertyType object for a given string representation.
	 * 
	 * @param type
	 *            The Product Property type to find represented by this String
	 * @return The ProductPropertyType object
	 */
	public static ProductPropertyType findByType(String type) {
		if (type != null) {
			for (ProductPropertyType productPropertyType : ProductPropertyType
					.values()) {
				if (productPropertyType.getType().equals(type)) {
					return productPropertyType;
				}
			}
		}
		throw new IllegalArgumentException(
				"There is no property type with getType '" + type
						+ "' specified.");
	}
}
