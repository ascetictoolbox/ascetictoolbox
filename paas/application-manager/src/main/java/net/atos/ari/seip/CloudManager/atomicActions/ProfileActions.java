package net.atos.ari.seip.CloudManager.atomicActions;

import java.util.List;

import org.apache.log4j.Logger;
import org.deltacloud.client.DeltaCloudClient;
import org.deltacloud.client.DeltaCloudClientException;
import org.deltacloud.client.HardwareProfile;

public class ProfileActions {

	Logger log = Logger.getLogger(this.getClass().getName());

	DeltaCloudClient dcc;

	public ProfileActions(DeltaCloudClient dcc) {
		super();
		this.dcc = dcc;
	}
	
	public HardwareProfile listProfile(String profileId){
		try {
			return dcc.listProfile(profileId);
		} catch (DeltaCloudClientException e) {
			log.error("unable to list profile: "+profileId);
		}
		return null;
	}
	
	
	public List<HardwareProfile> listProfiles(){
		try {
			return dcc.listProfiles();
		} catch (DeltaCloudClientException e) {
			log.error("unable to list profiles");
		}
		return null;
	}
	
}
