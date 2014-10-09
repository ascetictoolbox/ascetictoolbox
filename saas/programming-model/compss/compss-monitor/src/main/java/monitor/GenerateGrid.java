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
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.event.ColSizeEvent;

public class GenerateGrid {

	private static RowRenderer resourceRowRenderer = new RowRenderer() {

		public void render(Row row, Object model, int index) throws Exception {
			String[] data = (String[]) model;

			// add something to row...
			Image state = new Image();
			state.setWidth("25px");
			if (data[1].compareTo("Ready") == 0) {
				state.setSrc("/images/state/green.jpg");
			} else if (data[1].compareTo("Removing") == 0) {
				state.setSrc("/images/state/yellow.jpg");
			} else if (data[1].compareTo("Waiting") == 0) {
				state.setSrc("/images/state/red.jpg");
			}
			row.appendChild(state);

			row.appendChild(new Label(data[0]));
			row.appendChild(new Label(data[2]));

			if (data[3] != null) {
				if (data[5].startsWith("0.")) {
					Float memsize = Float.parseFloat(data[3]);
					row.appendChild(new Label((memsize * 1024) + " MB"));
				} else {
					row.appendChild(new Label(data[3] + " GB"));
				}
			} else {
				row.appendChild(new Label(data[3]));
			}

			if (data[4] != null) {
				if (data[4].startsWith("0.")) {
					Float disksize = Float.parseFloat(data[4]);
					row.appendChild(new Label((disksize * 1024) + " MB"));
				} else {
					row.appendChild(new Label(data[4] + " GB"));
				}
			} else {
				row.appendChild(new Label(data[4]));
			}

			row.appendChild(new Label(data[5]));
		}
	};
	private static RowRenderer coreRowRenderer = new RowRenderer() {

		public void render(Row row, Object model, int index) throws Exception {
			String[] data = (String[]) model;

			// add something to row...
			int i = Integer.parseInt(data[0]) % 12;
			Image color = new Image();
			color.setWidth("25px");
			color.setSrc("/images/colors/" + i + ".png");
			row.appendChild(color);
			row.appendChild(new Label(data[1]));
			row.appendChild(new Label(data[2]));
			row.appendChild(new Label(data[3]));
			row.appendChild(new Label(data[4]));
			row.appendChild(new Label(data[5]));
		}
	};

	public static void computeEnergyColumns(Grid grid) {

		Core[] coreInfo = MonitoringParser.getCoreInfo();
		Map<String, Resource> resourceInfo = MonitoringParser.getResourceInfo();

		Iterator<Component> comps = grid.getChildren().iterator();
		LinkedList<Component> compslist = new LinkedList<Component>();
		while (comps.hasNext()) {
			compslist.add(comps.next());
		}
		for (Component c : compslist) {
			grid.removeChild(c);
		}

		Columns columns = new Columns();
		while (columns.getFirstChild() != null) {
			columns.removeChild(columns.getFirstChild());
		}

		int coreCount = coreInfo.length;
		int implCount = 0;
		int[] implCounts = new int[coreCount];
		for (int coreId = 0; coreId < coreCount; coreId++) {
			implCounts[coreId] = coreInfo[coreId].getImplementations().length;
			implCount += implCounts[coreId];
		}

		System.out.println("Hi ha " + coreCount + " cores");
		System.out.println("Hi ha " + implCount + " implementacions");

		Auxhead head = new Auxhead();
		Auxheader header = new Auxheader();
		header.setLabel("Energy Consumption Estimation");
		header.setColspan(1+implCount);
		header.setParent(head);
		head.setParent(grid);

		head = new Auxhead();
		header = new Auxheader();
		header.setRowspan(2);
		header.setColspan(1);
		header.setLabel("Resources");
		header.setParent(head);
		header.setWidth("100px");
		for (int i = 0; i < coreCount; i++) {
			header = new Auxheader();
			header.setColspan(implCounts[i]);
			header.setLabel(coreInfo[i].getCoreName());
			header.setParent(head);
		}
		head.setParent(grid);

		for (int coreId = 0; coreId < coreCount; coreId++) {
			for (int implId = 0; implId < implCounts[coreId]; implId++) {
				Column c = new Column();
				c.setLabel(coreInfo[coreId].getImplementations()[implId]);
				c.setParent(columns);
				c.setWidth("75");
			}
		}

		columns.setParent(grid);
		head.setParent(grid);

		Rows rows = new Rows();
		for (Resource resource : resourceInfo.values()) {
			Row row = new Row();
			Label resourceName = new Label(resource.getName());
			resourceName.setParent(row);
			row.setParent(rows);
			for (int coreId = 0; coreId < coreCount; coreId++) {
				for (int implId = 0; implId < implCounts[coreId]; implId++) {
					Label cost = new Label(
							resource.getEnergyEstimations()[coreId][implId]);
					cost.setParent(row);
				}
			}
		}
		rows.setParent(grid);
		/*
		 * Auxheader header = new Auxheader(); header.setColspan(implCount);
		 * header.setLabel("EnergyConsumption information");
		 * header.setParent(head);
		 */

	}

	public static void updateWorkersGrid(Grid grid) {
		grid.setModel(new ListModelList(MonitoringParser.getWorkersDataArray()));
		grid.setRowRenderer(resourceRowRenderer);
	}

	public static void updateCoresGrid(Grid grid) {

		grid.setModel(new ListModelList(MonitoringParser.getCoresDataArray()));
		grid.setRowRenderer(coreRowRenderer);
	}
}
