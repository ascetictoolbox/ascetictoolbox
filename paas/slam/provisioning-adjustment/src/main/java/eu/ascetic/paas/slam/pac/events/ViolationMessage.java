/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.paas.slam.pac.events;

import java.util.Calendar;

import org.slasoi.gslam.pac.events.Message;

public class ViolationMessage implements Message {

	private Calendar time;
	private String appId;
	private String deploymentId;
	private Value value;
	private Alert alert;

	public class Alert {

		private String type;
		private String slaUUID;
		private String slaAgreementTerm;
		private SlaGuaranteedState slaGuaranteedState;
		private SlaGuaranteedAction slaGuaranteedAction;
//		private Provider provider;

		public class SlaGuaranteedState {

			private String guaranteedId;
			private String operator;
			private Double guaranteedValue;

			public String getGuaranteedId() {
				return guaranteedId;
			}

			public void setGuaranteedId(String guaranteedId) {
				this.guaranteedId = guaranteedId;
			}

			public String getOperator() {
				return operator;
			}

			public void setOperator(String operator) {
				this.operator = operator;
			}

			public Double getGuaranteedValue() {
				return guaranteedValue;
			}

			public void setGuaranteedValue(Double guaranteedValue) {
				this.guaranteedValue = guaranteedValue;
			}

		}

//		public class Provider {
//
//			private String providerUUID;
//			private String slaUUID;
//
//			public String getProviderUUID() {
//				return providerUUID;
//			}
//
//			public void setProviderUUID(String providerUUID) {
//				this.providerUUID = providerUUID;
//			}
//
//			public String getSlaUUID() {
//				return slaUUID;
//			}
//
//			public void setSlaUUID(String slaUUID) {
//				this.slaUUID = slaUUID;
//			}
//
//		}

		public class SlaGuaranteedAction {

			private String guaranteedId;
			private String customValue;

			public String getGuaranteedId() {
				return guaranteedId;
			}

			public void setGuaranteedId(String guaranteedId) {
				this.guaranteedId = guaranteedId;
			}

			public String getCustomValue() {
				return customValue;
			}

			public void setCustomValue(String customValue) {
				this.customValue = customValue;
			}

		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getSlaUUID() {
			return slaUUID;
		}

		public void setSlaUUID(String slaUUID) {
			this.slaUUID = slaUUID;
		}

		public String getSlaAgreementTerm() {
			return slaAgreementTerm;
		}

		public void setSlaAgreementTerm(String slaAgreementTerm) {
			this.slaAgreementTerm = slaAgreementTerm;
		}

		public SlaGuaranteedState getSlaGuaranteedState() {
			return slaGuaranteedState;
		}

		public void setSlaGuaranteedState(SlaGuaranteedState slaGuaranteedState) {
			this.slaGuaranteedState = slaGuaranteedState;
		}

		public SlaGuaranteedAction getSlaGuaranteedAction() {
			return slaGuaranteedAction;
		}

		public void setSlaGuaranteedAction(SlaGuaranteedAction slaGuaranteedAction) {
			this.slaGuaranteedAction = slaGuaranteedAction;
		}

//		public Provider getProvider() {
//			return provider;
//		}
//
//		public void setProvider(Provider provider) {
//			this.provider = provider;
//		}

	}

	public ViolationMessage(Calendar time, String appId, String deploymentId) {
		this.time = time;
		this.appId = appId;
		this.deploymentId = deploymentId;
	}

	/**
	 * Print message parameters
	 * 
	 * @return Message parameters
	 */
	public final String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("\nTime: " + time);
		sb.append("\nappId: " + appId);
		sb.append("\ndeploymentId: " + deploymentId);
		sb.append("\nvalue: " + value.getTextValue());
		sb.append("\nvalue id: " + value.getAttributeValue());
		sb.append("\nAlert type: " + alert.type);
		sb.append("\nAlert SLA UUID: " + alert.slaUUID);
		sb.append("\nAlert slaAgreementTerm: " + alert.slaAgreementTerm);
//		if (alert.provider != null) {
//			sb.append("\nAlert providerUUID: " + alert.provider.providerUUID);
//			sb.append("\nAlert providerSlaUUID: " + alert.provider.slaUUID);
//		}
		sb.append("\nAlert GuaranteeId: " + alert.slaGuaranteedState.guaranteedId);
		sb.append("\nAlert Operator: " + alert.slaGuaranteedState.operator);
		sb.append("\nAlert GuaranteeValue: " + alert.slaGuaranteedState.guaranteedValue);

		return sb.toString();
	}



	public void setTime(Calendar time) {
		this.time = time;
	}


	public Calendar getTime() {
		return time;
	}


	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public Alert getAlert() {
		return alert;
	}

	public void setAlert(Alert alert) {
		this.alert = alert;
	}

}
