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
import org.zkoss.zul.Rows;
import org.zkoss.zul.event.ColSizeEvent;

public class GenerateGrid {

	public static void computeEnergyColumns(Grid grid,
			Map<String, int[][]> values) {

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
		int coreCount = 0;
		int implCount = 0;
		int[] implCounts = new int[0];
		for (int[][] entry : values.values()) {
			coreCount = entry.length;
			implCounts = new int[coreCount];
			for (int j = 0; j < coreCount; j++) {
				implCounts[j] = entry[j].length;
				implCount += implCounts[j];
			}
			break;
		}

		System.out.println("Hi ha "+coreCount+" cores");
		System.out.println("Hi ha "+implCount+" implementacions");
		
		Auxhead head = new Auxhead();
		Auxheader header = new Auxheader();
		//header.setColspan(implCount+1);
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
			header.setLabel("Core" + i);
			header.setParent(head);
		}
		head.setParent(grid);

		for (int coreId=0;coreId<coreCount;coreId++){
			for (int implId=0;implId<implCounts[coreId];implId++){
				Column c = new Column();
				c.setLabel("Implementation"+implId);
				c.setParent(columns);
				c.setWidth("75");
			}			
		}
		
		columns.setParent(grid);
		head.setParent(grid);
		
		Rows rows= new Rows();
		for (java.util.Map.Entry<String, int[][]> entry: values.entrySet()){
			Row row= new Row();
			Label resource= new Label (entry.getKey());
			resource.setParent(row);
			row.setParent(rows);
			for (int coreId=0;coreId<coreCount;coreId++){
				for (int implId=0;implId<implCounts[coreId];implId++){
					Label cost = new Label(""+entry.getValue()[coreId][implId]);
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
