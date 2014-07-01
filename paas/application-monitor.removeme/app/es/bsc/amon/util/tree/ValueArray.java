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
