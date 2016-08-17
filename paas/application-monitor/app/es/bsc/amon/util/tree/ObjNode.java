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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mmacias on 11/06/14.
 */
class ObjNode extends TreeNode {
	protected Map<String,TreeNode> properties = new HashMap<String,TreeNode>();

	@Override
	public TreeNode to(String id) {
		TreeNode t = properties.get(id);
		if(t == null) throw new RuntimeException("Property " + id + " does not exist in this object.");
		return t;
	}

	@Override
	public Collection<String> getChildrenIds() {
		return properties.keySet();
	}
}
