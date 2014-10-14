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


    private static final String OP_SUM = "sum";
    private static final String OP_AVG = "avg";
    private static final String OP_COUNT = "count";
    private static final String OP_HIST = "hist"; //histogram

	private static final long DEFAULT_RESOLUTION = 10000;
	private static final long REMEMBER_TIME = 24*3600*1000; // Only 'remembers' apps from the last 24 hours

}
