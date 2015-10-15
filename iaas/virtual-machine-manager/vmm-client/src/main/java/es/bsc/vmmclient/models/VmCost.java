package es.bsc.vmmclient.models;

import com.google.common.base.MoreObjects;

/**
 * Created by mmacias on 15/10/15.
 */
public class VmCost {
	private String vmId;
	private double cost;

	public VmCost(String vmId, double cost) {
		this.vmId = vmId;
		this.cost = cost;
	}

	public String getVmId() {
		return vmId;
	}

	public double getCost() {
		return cost;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("vmId",vmId)
				.add("cost",cost)
				.toString();
	}
}
