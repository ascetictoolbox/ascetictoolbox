package eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils;

import java.util.HashMap;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
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
}
