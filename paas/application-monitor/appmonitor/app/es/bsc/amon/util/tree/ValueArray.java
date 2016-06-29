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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Assuming all the values are of the same class
 */
class ValueArray<T extends Value> extends Value {
	protected List<T> values = new ArrayList<T>();

	@Override
	public Object getValue() {
		if(values.size()==0) return null;

		Object[] arr = (Object[])Array.newInstance(values.get(0).getClass(),values.size());
		for(int i = 0 ; i < values.size() ; i++) {
			arr[i] = values.get(i);
		}
		return arr;
	}

}
