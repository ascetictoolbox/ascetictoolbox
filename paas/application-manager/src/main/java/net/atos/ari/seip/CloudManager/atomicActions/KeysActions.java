package net.atos.ari.seip.CloudManager.atomicActions;

import java.util.List;

import org.apache.log4j.Logger;
import org.deltacloud.client.DeltaCloudClient;
import org.deltacloud.client.DeltaCloudClientException;
import org.deltacloud.client.Key;

public class KeysActions {

	Logger log = Logger.getLogger(this.getClass().getName());

	DeltaCloudClient dcc;

	public KeysActions(DeltaCloudClient dcc) {
		super();
		this.dcc = dcc;
	}
	
	public Key createKey(String keyname){
		try {
			return dcc.createKey(keyname);
		} catch (DeltaCloudClientException e) {
			log.error("unable to create key");
		}
		return null;
	}
	
	public Key listKey(String keyId){
		try {
			return dcc.listKey(keyId);
		} catch (DeltaCloudClientException e) {
			log.error("unable to list key: "+keyId);
		}
		return null;
	}
	
	public List<Key> listKeys(){
		try {
			return dcc.listKeys();
		} catch (DeltaCloudClientException e) {
			log.error("unable to list keys");
		}
		return null;
	}
	
}
