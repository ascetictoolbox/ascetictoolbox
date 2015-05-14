/*
 * Author: Mario Macias (Barcelona Supercomputing Center). 2014
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 */

package es.bsc.amon.util.tree;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * Created by mmacias on 11/06/14.
 */
public abstract class TreeNode {

	public void append(TreeNode o) {
		if(!getClass().equals(o.getClass()))
			throw new RuntimeException("Cannot append class " + o.getClass().getName() +" to class " + getClass().getName());
	}

	/**
	 * Navigates to a children element. E.g. an object with a property "prop":val will return the TreeNode object cointaining "val"
 	 * @param id
	 * @return
	 */
	public abstract TreeNode to(String id);

	/**
	 * Returns the identifiers of the children elements (which will be used to navigate with the method 'to').
	 * @return
	 */
	public abstract Collection<String> getChildrenIds();

	/**
	 * Gets the INSTANCE of the object, but if the object is a subtype of Value, returns the value itself (Number, String, array)...
	 * @return
	 */
	public Object getValue() {
		return this;
	}

}
