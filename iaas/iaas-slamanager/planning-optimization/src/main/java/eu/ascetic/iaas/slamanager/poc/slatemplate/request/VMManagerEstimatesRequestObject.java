/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.iaas.slamanager.poc.slatemplate.request;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.ascetic.iaas.slamanager.poc.enums.AsceticAgreementTerm;
import eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.ResourceGuarantee;
import eu.ascetic.iaas.slamanager.poc.utils.AsceticUnits;

public class VMManagerEstimatesRequestObject {

	private JSONObject requestJson;

	private AsceticSlaTemplate asceticslaTemplate = null;

	public VMManagerEstimatesRequestObject(Builder builder) {
		this.asceticslaTemplate = builder._asceticSlaTemplate;
		this.requestJson = builder._requestJson;
	}

	public JSONObject getRequestJson() {
		return requestJson;
	}

	public AsceticSlaTemplate getAsceticSlaTemplate() {
		return asceticslaTemplate;
	}

	public static class Builder {

		private AsceticSlaTemplate _asceticSlaTemplate = null;

		private JSONObject _requestJson = null;

		public Builder setAsceticSlatemplate(AsceticSlaTemplate ast) {
			this._asceticSlaTemplate = ast;
			return this;
		}

		private Builder init() {
			this._requestJson = new JSONObject();
			try {
				this._requestJson.put("vms", new JSONArray());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return this;
		}

		public VMManagerEstimatesRequestObject build() {
			for (VirtualSystem vs : _asceticSlaTemplate.getVirtualSystems()) {
				addVM(vs);
			}
			return new VMManagerEstimatesRequestObject(this);
		}

		public void addVM(VirtualSystem vs) {
			JSONObject vm = new JSONObject();
			try {
				vm.put("id", vs.getOvfId());
				for (ResourceGuarantee rg : vs.getResourceGuarantees()) {
					if (rg.getDefault() != -1) {
						if (rg.getAgreementTerm().equals(AsceticAgreementTerm.cpu_speed))
							vm.put("cpuFreq", rg.getDefault());
						if (rg.getAgreementTerm().equals(AsceticAgreementTerm.memory))
							vm.put("ramMb", rg.getDefault());
						if (rg.getAgreementTerm().equals(AsceticAgreementTerm.vm_cores))
							vm.put("vcpus", rg.getDefault());
						if (rg.getAgreementTerm().equals(AsceticAgreementTerm.disk_size))
							vm.put("diskGb", rg.getDefault()/1024); // convert to GB
					}
				}
				_requestJson.accumulate("vms", vm);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		public Builder() {
			init();
		}
	}

}
