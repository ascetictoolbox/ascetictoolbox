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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zkoss.zhtml.Head;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.event.ColSizeEvent;

public class GraphManager {

	private static String monitorLocation;

	static {
		if (System.getenv("IT_MONITOR") == null) {
			monitorLocation = System.getProperty("user.home") + "/monitor.dot";
		} else {
			monitorLocation = System.getenv("IT_MONITOR") + "/monitor.dot";
		}
		monitorLocation = "/tmp/monitor.dot";
	}

	public static Process generateGraph() throws IOException{
		String[] cmd = {
				"/bin/sh",
				"-c",
				"dot -Tjpg " + monitorLocation + " > "
						+ System.getProperty("catalina.base")
						+ "/webapps/compss-monitor/images/graph.jpg" };
		return  Runtime.getRuntime().exec(cmd);
	}

	public static void updateGraph(Process p, Image graph) throws InterruptedException{
		p.waitFor();
		graph.setSrc("/images/graph.jpg");
		graph.setStyle("max-width:100%;");
		graph.invalidate();
	}

}
