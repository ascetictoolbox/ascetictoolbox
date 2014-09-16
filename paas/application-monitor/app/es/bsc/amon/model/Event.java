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

package es.bsc.amon.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;

/**
 * Created by mmacias on 27/06/14.
 */
public class Event {
	public ObjectNode _id;
	public long timestamp;
	public Long endtime;
	public String appId;
	public String nodeId;
	public String instanceId;
	public ObjectNode data;

	public String getId() {
		return _id.get("$oid").asText();
	}
}
