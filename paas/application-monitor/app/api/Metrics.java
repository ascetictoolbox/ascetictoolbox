/*
 * Author: Mario Macias (Barcelona Supercomputing Center). 2014
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 */


package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.controller.AppsDBMapper;
import es.bsc.amon.controller.EventsDBMapper;
import es.bsc.amon.util.tree.TreeNode;
import es.bsc.amon.util.tree.TreeNodeFactory;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Collection;
import java.util.Iterator;

public class Metrics extends Controller {
	/**
	 *
	 * @param oper Consolidation operation: sum, avg, count, hist (histogram - TODO)
	 * @param appId
	 * @param nodeId
	 * @param start default: 0
	 * @param end default: now
	 * @param resolution default: DEFAULT_RESOLUTION
	 * @return
	 */
    public static Result getAllMetricsHistory(String oper, String appId, String nodeId, Long start, Long end, Long resolution) {
        if(start < 0L) start = 0L;
        if(end < 0L) end = System.currentTimeMillis();
	    if(resolution < 0) resolution = DEFAULT_RESOLUTION;



        StringBuilder stringBuilder = new StringBuilder("oper: ").append(oper)
                .append("\nappId: ").append(appId)
                .append("\nnodeId: ").append(nodeId)
                .append("\nstart: ").append(start)
                .append("\nend: ").append(end).append("\n");
        return ok(stringBuilder.toString());

    }

	public static Result getNavigationPath() {
		try {
			long now = System.currentTimeMillis();
			// TODO: ArrayNode allApps = Apps.getInstance().getAllApps(now - REMEMBER_TIME, now);
			ObjectNode allApps = AppsDBMapper.getInstance().getAllApps(0, now, false);
			StringBuilder sb = new StringBuilder("[");
			Iterator<String> it = allApps.fieldNames();
			while(it.hasNext()) {
				sb.append("\"").append(it.next()).append("\"");
				if(it.hasNext()) {
					sb.append(",");
				}
			}
			sb.append("]");
			return ok(sb.toString());
		} catch(Throwable e) {
			Logger.warn("For " + request().uri() + " --> " +e.getMessage());
			return notFound(e.getMessage());
		}
	}
	public static Result getNavigationPathA(String appId) {
		try {
			// TODO: add remember time
			ObjectNode allApps = AppsDBMapper.getInstance().getAllApps(0,System.currentTimeMillis(), false);
			return ok(allApps.get(appId).toString());
		} catch(Throwable e) {
			Logger.warn("For " + request().uri() + " --> " +e.getMessage());
			return notFound(e.getMessage());
		}
	}
	public static Result getNavigationPathAN(String appId, String nodeId) {
		try {
			JsonNode event = EventsDBMapper.getInstance().getLastEvent(appId, nodeId);
			if(event == null) throw new RuntimeException("No metrics for app/node found: " + appId+"/"+nodeId);
			JsonNode data = event.get(EventsDBMapper.DATA);
			if(data == null) return ok("[]");
			TreeNode tn = TreeNodeFactory.fromJson(data);
			ArrayNode an = new ArrayNode(JsonNodeFactory.instance);
			StringBuilder sb = new StringBuilder("[");
			Collection<String> ids = tn.getChildrenIds();
			int size = ids.size();
			for(String id : ids) {
				sb.append("\"").append(id).append("\"");
				size--;
				if(size > 0) {
					sb.append(",");
				}
			}
			sb.append("]");
			return ok(sb.toString());
		} catch(Throwable t) {
			Logger.warn("For " + request().uri() + " --> " + t.getMessage());
			return notFound(t.getMessage());
		}
	}
	public static Result getNavigationPathANP(String appId, String nodeId, String path) {
		if (path == null) return getNavigationPathAN(appId,nodeId);
		JsonNode event = EventsDBMapper.getInstance().getLastEvent(appId, nodeId);
		if(event == null) throw new RuntimeException("No metrics for app/node found: " + appId+"/"+nodeId);
		JsonNode data = event.get(EventsDBMapper.DATA);
		if(data == null) return ok("[]");

		TreeNode tn = TreeNodeFactory.fromJson(data);
		String[] splittedPath = path.split("/");
		for(int i = 0 ; i < splittedPath.length ; i++) {
			while(i < splittedPath.length && (splittedPath[i] == null || splittedPath[i].trim().equals(""))) {
				i++;
			}
			tn = tn.to(splittedPath[i]);
		}

		StringBuilder sb = new StringBuilder("[");
		Collection<String> ids = tn.getChildrenIds();
		if(ids == null) return ok("[]");

		int size = ids.size();
		for(String id : ids) {
			sb.append("\"").append(id).append("\"");
			size--;
			if(size > 0) {
				sb.append(",");
			}
		}
		sb.append("]");
		return ok(sb.toString());
	}

    private static final String OP_SUM = "sum";
    private static final String OP_AVG = "avg";
    private static final String OP_COUNT = "count";
    private static final String OP_HIST = "hist"; //histogram

	private static final long DEFAULT_RESOLUTION = 10000;
	private static final long REMEMBER_TIME = 24*3600*1000; // Only 'remembers' apps from the last 24 hours

}
