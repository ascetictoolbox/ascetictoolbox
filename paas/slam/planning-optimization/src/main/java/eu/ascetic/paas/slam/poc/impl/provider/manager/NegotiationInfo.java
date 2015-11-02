/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
	
	
