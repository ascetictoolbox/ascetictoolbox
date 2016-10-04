package eu.ascetic.asceticarchitecture.iaas.zabbixApi.collector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary;



public class CurrentCollectorManager {
	
	private final Gson gson = new GsonBuilder().create();
	private ZabbixClient client = new ZabbixClient();

	
	public CurrentCollectorManager(){
		
	}
	
	
	public String getJsonCurrentHvPower(String hostName){
		String jsonItemStr = "";
//		Item itemCpu = client.getStringFormatItemByKeyFromHost(Dictionary.ZABBIX_ITEMKEY_HV_POWER, hostName);
//		Item itemMemory = client.getStringFormatItemByKeyFromHost(Dictionary.ZABBIX_ITEMKEY_HV_MEMORY, hostName);	
//		Item itemDisk = client.getDiskUsageItemByKeyFromHV(hostName);				
//		Item itemNetworkBytesTransmitted = client.getStringFormatItemByKeyFromHost(Dictionary.ZABBIX_ITEMKEY_HV_NETWORK_BYTES_TRANSMITTED, hostName);
//		Item itemNetworkBytesReceived = client.getStringFormatItemByKeyFromHost(Dictionary.ZABBIX_ITEMKEY_HV_NETWORK_BYTES_RECEIVED, hostName);
//		
//		items.add(itemCpu);
//		items.add(itemMemory);
//		items.add(itemDisk);
//		items.add(itemNetworkBytesTransmitted);
//		items.add(itemNetworkBytesReceived);
//				
//		JsonItem jsonItem;
//		if (items != null){
//			jsonItem = new JsonItem(client.getHostByName(hostName).getHost());
//			
//			jsonItem.setValues(items);
//			jsonItemStr = gson.toJson(jsonItem);
//		}
		return jsonItemStr;		
	}
	
	
	public String getJsonCurrentVmWorkload(String hostName){
		String jsonItemStr = "";
//		Vector<Item> items = new Vector<Item>();
//		Item itemCpu = client.getStringFormatItemByKeyFromHost(Dictionary.ZABBIX_ITEMKEY_VM_CPU, hostName);
//		Item itemMemory = client.getStringFormatItemByKeyFromHost(Dictionary.ZABBIX_ITEMKEY_VM_MEMORY, hostName);
//		Item itemDisk = client.getDiskUsageItemByKeyFromVM(hostName);
//		Item itemNetworkBytesTransmitted = client.getStringFormatItemByKeyFromHost(Dictionary.ZABBIX_ITEMKEY_VM_NETWORK_BYTES_TRANSMITTED, hostName);
//		Item itemNetworkBytesReceived = client.getStringFormatItemByKeyFromHost(Dictionary.ZABBIX_ITEMKEY_VM_NETWORK_BYTES_RECEIVED, hostName);
//		
//		items.add(itemCpu);
//		items.add(itemMemory);
//		items.add(itemDisk);
//		items.add(itemNetworkBytesTransmitted);
//		items.add(itemNetworkBytesReceived);
//				
//		JsonItem jsonItem;
//		if (items != null){
//			jsonItem = new JsonItem(client.getHostByName(hostName).getHost());
//			jsonItem.setValues(items);
//			jsonItemStr = gson.toJson(jsonItem);
//		}
		return jsonItemStr;		
	}
	
}
