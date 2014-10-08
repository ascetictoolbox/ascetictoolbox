/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MonitoringParser {

	private static Core[] coreInfo = new Core[0];
	private static Map<String, Resource> resourceInfo = new HashMap<String, Resource>();

	public static void parse() {

		System.out.println("Parsing document...");

		String monitorLocation;

		if (System.getenv("IT_MONITOR") == null) {
			monitorLocation = System.getProperty("user.home") + "/monitor.xml";
		} else {
			monitorLocation = System.getenv("IT_MONITOR") + "/monitor.xml";
		}
		monitorLocation = "/tmp/monitor.xml";
		while (true) {
			try {
				System.out.println("Parsing document");
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				docFactory.setNamespaceAware(true);
				Document resourcesDoc = docFactory.newDocumentBuilder().parse(
						monitorLocation);
				NodeList nl = resourcesDoc.getChildNodes();
				Node COMPSs = null;
				for (int i = 0; i < nl.getLength(); i++) {
					if (nl.item(i).getNodeName().equals("COMPSsState")) {
						COMPSs = nl.item(i);
						break;
					}

				}
				if (COMPSs == null) {
					// NO COMPSs item --> empty
					return;
				}
				nl = COMPSs.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					if (n.getNodeName().compareTo("ResourceInfo") == 0) {
						System.out.println("ResourceInfo");
						parseResourceInfoNode(n);
					}
					if (n.getNodeName().compareTo("CoresInfo") == 0) {
						System.out.println("CoresInfo");
						parseCoresInfoNode(n);
					}
				}
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void parseResourceInfoNode(Node resourcesInfo) {
		try {
			resourceInfo.clear();
			NodeList nl = resourcesInfo.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeName().equals("Resource")) {
					String resourceName = n.getAttributes().getNamedItem("id")
							.getTextContent();
					Resource r = new Resource(n);
					resourceInfo.put(resourceName, r);
				}
			}
		} catch (Exception e) {
			System.out.println("ResourceInfo not parsed properly");
		}
	}


	private static void parseCoresInfoNode(Node coresInfo) {
		try {
			NodeList nl = coresInfo.getChildNodes();
			LinkedList<Node> cores = new LinkedList<Node>();
			int coreCount = 0;
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeName().equals("Core")) {
					cores.add(n);
					coreCount++;
				}
			}
			coreInfo = new Core[coreCount];
			for (Node core : cores) {
				int coreId = Integer.parseInt(core.getAttributes()
						.getNamedItem("id").getTextContent());
				coreInfo[coreId] = new Core(core);
			}
		} catch (Exception e) {
			System.out.println("CoreInfo not parsed properly");
		}
	}

	
	
	
	public static List<String[]> getWorkersDataArray() {
		List<String[]> values = new LinkedList<String[]>();
		for (Resource r:resourceInfo.values()){
			values.add(r.getResourceRow());
		}
		return values;
	}

	public static List<String[]> getCoresDataArray() {
		LinkedList<String[]> values = new LinkedList<String[]>();
		for (Core core : coreInfo) {
			values.addAll(core.getCoreRows());
		}
		return values;
	}

	
	public static Core[] getCoreInfo(){
		return coreInfo;
	}
	
	public static Map<String, Resource> getResourceInfo(){
		return resourceInfo;
	}
}
