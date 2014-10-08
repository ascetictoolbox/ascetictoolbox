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

import java.util.LinkedList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Core {

	private int coreId;
	private String methodName;
	private String params;
	private Implementation[] impls;

	public Core(Node core){
		coreId = Integer.parseInt(core.getAttributes()
				.getNamedItem("id").getTextContent());
		String signature = core.getAttributes().getNamedItem("signature")
				.getTextContent();
		int pos = signature.indexOf("(");
		int posfin = signature.indexOf(")");
		methodName = signature.substring(0, pos);
		params = signature.substring(pos + 1, posfin);
		NodeList nl = core.getChildNodes();
		int implCount=0;
		LinkedList<Node> implementations= new LinkedList<Node>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals("Implementation")) {
				implCount++;
				implementations.add(n);
			}
		}
		impls= new Implementation[implCount];
		for (Node impl:implementations){
			int implId = Integer.parseInt(impl.getAttributes()
					.getNamedItem("id").getTextContent());
			impls[implId]= new Implementation(impl);
		}
	}

	public LinkedList<String[]> getCoreRows(){
		LinkedList<String[]> rows = new LinkedList<String[]>();
		for (Implementation impl:impls){
			String[] row = new String[6];
			rows.add(row);
			row[0]=""+coreId;
			row[1]=impl.declaringClass;
			row[2]=methodName;
			row[3]=params;
			row[4]=""+impl.avg;
			row[5]=""+impl.count;
		}
		return rows;
	}
	
	class Implementation {
		int implId;
		String declaringClass;
		long min;
		long avg;
		long max;
		long count;

		public Implementation(Node impl) {
			implId = Integer.parseInt(impl.getAttributes()
					.getNamedItem("id").getTextContent());
			declaringClass=impl.getAttributes()
					.getNamedItem("definingClass").getTextContent();
			NodeList nl = impl.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeName().equals("MinExecutionTime")) {
					min = Long.parseLong(n.getTextContent());
				}else if (n.getNodeName().equals("MeanExecutionTime")) {
					avg = Long.parseLong(n.getTextContent());
				}else if (n.getNodeName().equals("MaxExecutionTime")) {
					max = Long.parseLong(n.getTextContent());
				}else if (n.getNodeName().equals("ExecutedCount")) {
					count = Long.parseLong(n.getTextContent());
				}
			}
		}
	}
	
	public String getCoreName(){
		return this.methodName;
	}
	public String[] getImplementations(){
		String[] impl = new String[impls.length];
		for (int implId=0;implId<impls.length;implId++){
			impl[implId]=impls[implId].declaringClass;
		}
		return impl;
	}
}
