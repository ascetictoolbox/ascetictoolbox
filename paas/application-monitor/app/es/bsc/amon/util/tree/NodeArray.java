package es.bsc.amon.util.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An array of treeNodes
 */
class NodeArray extends TreeNode {
	protected List<TreeNode> elements = new ArrayList<TreeNode>();

	@Override
	public TreeNode to(String id) {
		try {
			int index = Integer.parseInt(id);
			return elements.get(index);
		} catch(NumberFormatException e) {
			throw new RuntimeException("Accessing a NodeArrayElement requires a number as index");
		} catch(IndexOutOfBoundsException e) {
			throw new RuntimeException("Index " + id + " is outside the range [0,"+elements.size()+"]");
		}
	}

	@Override
	public Collection<String> getChildrenIds() {
		List<String> ls = new ArrayList<String>(elements.size());
		for(int i = 0 ; i < elements.size() ; i++) {
			ls.add(i,String.valueOf(i));
		}
		return ls;
	}
}
