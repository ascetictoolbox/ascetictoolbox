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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Resource {

	private String name;
	private String tasks;
	private String status;
	private String cpu;
	private String memory;
	private String disk;
	private String[][] energyEstimation;

	public Resource(Node resource) {
		System.out.println("Parsing Resource");
		name = resource.getAttributes().getNamedItem("id").getTextContent();
		int slots = 0;
		int plens = 0;
		status = "Ready";
		NodeList nl = resource.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals("CPU")) {
				cpu = n.getTextContent();
			} else if (n.getNodeName().equals("Memory")) {
				memory = n.getTextContent();
			} else if (n.getNodeName().equals("Disk")) {
				disk = n.getTextContent();
			} else if (n.getNodeName().equals("Tasks")) {
				tasks = n.getTextContent();
			} else if (n.getNodeName().equals("EnergyEstimation")) {
				parseEnergyEstimations(n);
			}
		}
		System.out.println("Resource "+name+ " running tasks ["+tasks+"] is "+status+" and has "+cpu+" cores, "+memory+" GB of Memory and "+disk+"of disk");
		
	}

	private void parseEnergyEstimations(Node energyEstimations) {
		int coreCount = Integer.parseInt(energyEstimations.getAttributes()
				.getNamedItem("cores").getTextContent());
		energyEstimation = new String[coreCount][];
		NodeList coreList = energyEstimations.getChildNodes();
		for (int i = 0; i < coreList.getLength(); i++) {
			Node core = coreList.item(i);
			if (core.getNodeName().compareTo("Core") == 0) {
				int coreId = Integer.parseInt(core.getAttributes()
						.getNamedItem("id").getTextContent());
				int implCount = Integer.parseInt(core.getAttributes()
						.getNamedItem("implementations").getTextContent());
				energyEstimation[coreId] = new String[implCount];
				NodeList implList = core.getChildNodes();
				for (int j = 0; j < implList.getLength(); j++) {
					Node impl = implList.item(j);
					if (impl.getNodeName().equals("Implementation")) {
						int implId = Integer.parseInt(impl.getAttributes()
								.getNamedItem("id").getTextContent());
						energyEstimation[coreId][implId] = impl
								.getTextContent();
					}
				}
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public String[][] getEnergyEstimations() {
		return this.energyEstimation;
	}

	public String[] getResourceRow() {
		String[] row = new String[6];
		row[0] = name;
		row[1] = status;
		row[2] = cpu;
		row[3] = memory;
		row[4] = disk;
		row[5] = tasks;
		return row;
	}

}
