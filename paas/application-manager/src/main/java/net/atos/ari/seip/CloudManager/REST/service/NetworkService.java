package net.atos.ari.seip.CloudManager.REST.service;

import javax.ws.rs.PathParam;

public class NetworkService {

	
	
	public String CreateNetwork(){
		return "network created";
	}
	
	public String GetNetworkInfo(@PathParam("networkID") String networkID){
		return "network info";
	}
	
	public String DestroyNetwork(@PathParam("networkID") String networkID){
		return "network destroyed";
	}
}
