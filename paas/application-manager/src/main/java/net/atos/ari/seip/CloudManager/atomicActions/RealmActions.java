package net.atos.ari.seip.CloudManager.atomicActions;

import java.util.List;

import org.apache.log4j.Logger;
/* import org.deltacloud.client.DeltaCloudClient;
import org.deltacloud.client.DeltaCloudClientException;
import org.deltacloud.client.Realm; */

public class RealmActions {

	Logger log = Logger.getLogger(this.getClass().getName());

/*	DeltaCloudClient dcc;

	public RealmActions(DeltaCloudClient dcc) {
		super();
		this.dcc = dcc;
	}
	
	public Realm listRealm(String realmId){
		try {
			return dcc.listRealms(realmId);
		} catch (DeltaCloudClientException e) {
			log.error("unable to list realm: "+realmId);
		}
		return null;
	}
	
	public List<Realm> listRealms(){
		try {
			return dcc.listRealms();
		} catch (DeltaCloudClientException e) {
			log.error("unable to list realms");
		}
		return null;
	} */
}
