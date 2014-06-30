package es.bsc.amon.util.tree;

/**
 * Created by mmacias on 11/06/14.
 */
class NumberValue extends Value {
	protected Number value;
	@Override
	public Object getValue() {
		return value;
	}
}
