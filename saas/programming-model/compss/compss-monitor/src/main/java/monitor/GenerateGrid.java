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
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.event.ColSizeEvent;

public class GenerateGrid {

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
		// header.setColspan(implCount+1);
		header.setLabel("Energy Consumption Estimation");
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
					Label cost = new Label(resource.getEnergyEstimations()[coreId][implId]);
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

}
