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

/**
 * 	constant values: strings, ints, etc... not objects nor arrays of trees
 */
abstract class Value extends TreeNode {
	@Override
	public TreeNode to(String id) {
		throw new RuntimeException("Values do not have children");
	}

	@Override
	public Collection<String> getChildrenIds() {
		return null;
	}
}
