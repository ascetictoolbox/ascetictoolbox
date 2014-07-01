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
