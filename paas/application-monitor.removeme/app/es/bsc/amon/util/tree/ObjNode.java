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
