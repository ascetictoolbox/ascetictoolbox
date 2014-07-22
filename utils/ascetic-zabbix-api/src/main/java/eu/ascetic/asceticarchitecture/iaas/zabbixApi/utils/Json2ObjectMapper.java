package eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils;

import java.util.ArrayList;
import java.util.HashMap;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HostGroup;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;

/**
 * The Class Json2ObjectMapper.
 * 
 * @author David Rojo Antona - ATOS
 */
public class Json2ObjectMapper {

	/**
	 * Gets the host.
	 *
	 * @param hmJsonHost the hm json host
	 * @return the host
	 */
	public static Host getHost(HashMap<String,String> hmJsonHost){
		Host host = new Host();
		host.setHostid(hmJsonHost.get(Dictionary.HOST_HOSTID));
		host.setHost(hmJsonHost.get(Dictionary.HOST_HOST));
		host.setAvailable(hmJsonHost.get(Dictionary.HOST_AVAILABLE));
		return host;
	}
	
	/**
	 * Gets the item.
	 *
	 * @param hmJsonItem the hm json item
	 * @return the item
	 */
	public static Item getItem(HashMap<String,String> hmJsonItem){
		Item item = new Item(hmJsonItem.get(Dictionary.ITEM_NAME));
		item.setDelay(hmJsonItem.get(Dictionary.ITEM_DELAY));
		item.setHistory(hmJsonItem.get(Dictionary.ITEM_HISTORY));
		item.setHostid(hmJsonItem.get(Dictionary.ITEM_HOSTID));
		item.setItemid(hmJsonItem.get(Dictionary.ITEM_ITEMID));
		item.setKey(hmJsonItem.get(Dictionary.ITEM_KEY));
		item.setLastClock(Long.parseLong(hmJsonItem.get(Dictionary.ITEM_LASTCLOCK)));
		item.setLastValue(hmJsonItem.get(Dictionary.ITEM_LASTVALUE));
		item.setTrends(hmJsonItem.get(Dictionary.ITEM_TRENDS));
		return item;
	}
	
	
	/**
	 * Gets the history item.
	 *
	 * @param hmJsonHistoryItem the hm json history item
	 * @return the history item
	 */
	public static HistoryItem getHistoryItem(HashMap<String,Object> hmJsonHistoryItem){
		HistoryItem historyItem = new HistoryItem();
		historyItem.setItemid((String) hmJsonHistoryItem.get(Dictionary.HISTORY_ITEM_ITEMID));
		historyItem.setClock(Long.parseLong((String) hmJsonHistoryItem.get(Dictionary.HISTORY_ITEM_CLOCK)));
		historyItem.setNanoseconds((String) hmJsonHistoryItem.get(Dictionary.HISTORY_ITEM_NANOSECONDS));
		historyItem.setValue((String) hmJsonHistoryItem.get(Dictionary.HISTORY_ITEM_VALUE));
		
		ArrayList<HashMap<String, String>> hosts = (ArrayList<HashMap<String, String>>) hmJsonHistoryItem.get(Dictionary.HISTORY_ITEM_HOSTS);
		String hostId = hosts.get(0).get(Dictionary.HISTORY_ITEM_HOSTID);
		historyItem.setHostid(hostId);
		
		return historyItem;		
	}
	
	
	/**
	 * Gets the host group.
	 *
	 * @param hmJsonHostGroup the hm json host group
	 * @return the host group
	 */
	public static HostGroup getHostGroup(HashMap<String,String> hmJsonHostGroup){
		HostGroup hostGroup = new HostGroup();
		hostGroup.setGroupId(hmJsonHostGroup.get(Dictionary.HOSTGROUP_GROUPID));
		hostGroup.setName(hmJsonHostGroup.get(Dictionary.HOSTGROUP_NAME));
		return hostGroup;
	}

}
