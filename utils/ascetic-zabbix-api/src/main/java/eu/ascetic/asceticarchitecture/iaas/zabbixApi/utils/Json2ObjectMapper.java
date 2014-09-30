package eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils;

import java.util.ArrayList;
import java.util.HashMap;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HostGroup;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Template;

/**
*
* 
* Copyright 2014 ATOS SPAIN S.A. 
* 
* Licensed under the Apache License, Version 2.0 (the License);
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
* @author: David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
* @email david.rojoa@atos.net 
* 
* Java representation of Json2Object mapper 
* 
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

	/**
	 * Gets the template.
	 *
	 * @param hmJsonTemplate the hm json template
	 * @return the template
	 */
	public static Template getTemplate(HashMap<String, String> hmJsonTemplate) {
		Template template = new Template();
		template.setTemplateId(hmJsonTemplate.get(Dictionary.TEMPLATE_TEMPLATEID));
		template.setHost(hmJsonTemplate.get(Dictionary.TEMPLATE_HOST));
		template.setName(hmJsonTemplate.get(Dictionary.TEMPLATE_NAME));
		return template;
	}

}
