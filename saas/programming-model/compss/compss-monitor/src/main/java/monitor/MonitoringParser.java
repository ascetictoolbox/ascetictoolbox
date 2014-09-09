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
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MonitoringParser {
	
	private static List<String[]> WorkersDataArray;
	private static List<String[]> CoresDataArray;
	
	public static void parse(){
		String monitorLocation;

		if (System.getenv("IT_MONITOR") == null){
			monitorLocation = System.getProperty("user.home")+"/monitor.xml";
		}else{
			monitorLocation = System.getenv("IT_MONITOR")+"/monitor.xml";
		}
		
		while (true) {
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				docFactory.setNamespaceAware(true);
				Document resourcesDoc = docFactory.newDocumentBuilder().parse(monitorLocation);
				NodeList nl = resourcesDoc.getChildNodes();
				Node COMPSs=null;
				for (int i = 0; i < nl.getLength(); i++) {
					if (nl.item(i).getNodeName().equals("COMPSsState")){
						COMPSs = nl.item(i);
						break;
					}
					
				}
				WorkersDataArray =new ArrayList<String[]>();
				
				if (COMPSs==null){
					//NO COMPSs item --> empty
					return;
				}
				nl=COMPSs.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					if (n.getNodeName().equals("ResourceInfo")) {
						WorkersDataArray = parseResourceInfoNode(n);
					}
					if (n.getNodeName().equals("CoresInfo")) {
						CoresDataArray = parseCoresInfoNode(n);
					}
				}
				return;
			} catch (Exception e) {
				//e.printStackTrace();
				try{
					Thread.sleep(1000);
				}catch(Exception e2){
					
				}
			}
		}
	}







	private static List<String[]> parseResourceInfoNode(Node resourceInfo) {
		List<String[]> datas = new ArrayList<String[]>();
		NodeList nl = resourceInfo.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals("Resource")) {
				datas.add(parseResourceNode(n));
			}
		}
		return datas;
	}

	private static String[] parseResourceNode(Node resource) {
		String[] data = new String[7];
		try {
			data[0] = resource.getAttributes().getNamedItem("id")
					.getTextContent();
			NodeList nl = resource.getChildNodes();
			int slots = 0;
			int plens = 0;
			StringBuilder tasks = new StringBuilder();
			String status = "Ready";
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeName().equals("Slot")) {
					slots++;
					String content = n.getTextContent();
					if (content.compareTo("") != 0) {
						if (plens > 0) {
							tasks.append(", ");
						}
						tasks.append(content);
						plens++;
					}
				} else if (n.getNodeName().equals("Status")) {
					status = n.getTextContent();
				} else if (n.getNodeName().equals("CPU")) {
					data[4]=n.getTextContent();
				} else if (n.getNodeName().equals("Memory")) {
					data[5]=n.getTextContent();
				} else if (n.getNodeName().equals("Disk")) {
					data[6]=n.getTextContent();
				}
			}
			data[1] = tasks.toString();
			if (slots > 0) {
				data[2] = (plens * 100) / slots + "%";
			} else {
				data[2] = "-";
			}
			data[3] = status;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return data;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private static List<String[]> parseCoresInfoNode(Node coresInfo) {
		List<String[]> datas = new ArrayList<String[]>();
		NodeList nl = coresInfo.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals("Core")) {
				datas.add(parseCoreNode(n));
			}
		}
		return datas;
	}
	
	private static String[] parseCoreNode(Node cores) {
		String[] data = new String[5];
		try {
			data[0] = cores.getAttributes().getNamedItem("id").getTextContent();
			String signature= cores.getAttributes().getNamedItem("signature").getTextContent();
			int pos=signature.indexOf("(");
			int posfin=signature.indexOf(")");
			data[1] = signature.substring(0, pos);
			data[2]=signature.substring(pos+1, posfin);
			NodeList nl = cores.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeName().equals("MeanExecutionTime")) {
					data[3] = n.getTextContent();
				} else if (n.getNodeName().equals("ExecutedCount")) {
					data[4] = n.getTextContent();
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return data;
	}
	
	
	public static List<String[]> getWorkersDataArray() {
		return WorkersDataArray;
	}
	public static List<String[]> getCoresDataArray() {
		return CoresDataArray;
	}

}
