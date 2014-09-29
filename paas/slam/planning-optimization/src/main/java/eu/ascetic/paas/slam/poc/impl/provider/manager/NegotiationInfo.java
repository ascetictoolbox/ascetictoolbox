/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */

package eu.ascetic.paas.slam.poc.impl.provider.manager;

public class NegotiationInfo {

		private String negotiationId;
		private String endPoint;
		private String slaTemplate;

		
		public NegotiationInfo() {
		}
		
		public String getNegotiationId() {
			return negotiationId;
		}
		
		public void setNegotiationId(String negotiationId) {
			this.negotiationId = negotiationId;
		}
		
		public String getEndPoint() {
			return endPoint;
		}
		
		public void setEndPoint(String endPoint) {
			this.endPoint = endPoint;
		}

		public String getSlaTemplate() {
			return slaTemplate;
		}

		public void setSlaTemplate(String slaTemplate) {
			this.slaTemplate = slaTemplate;
		}
		
}
	
	
