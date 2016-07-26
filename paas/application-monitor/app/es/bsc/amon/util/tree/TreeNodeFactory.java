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

package es.bsc.amon.util.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import es.bsc.amon.util.tree.TreeNode;
import play.libs.Json;

import java.util.*;

/**
 * Created by mmacias on 09/06/14.
 */
public class TreeNodeFactory {

	public static TreeNode fromJson(String json) {
		JsonNode jn = Json.parse(json);
		return fromJson(jn);
	}
	public static TreeNode fromJson(JsonNode json) {
		TreeNode n=null;
		if(json.isArray()) {
			n = new NodeArray();
			Iterator<JsonNode> it = json.iterator();
			while(it.hasNext()) {
				((NodeArray)n).elements.add(fromJson(it.next()));
			}
		} else if(json.isNumber()) {
			n = new NumberValue();
			((NumberValue)n).value = json.numberValue();
		} else if(json.isTextual()) {
			n = new StringValue();
			((StringValue)n).value = json.asText();
		} else if(json.isObject()) {
			n = new ObjNode();
			Iterator<Map.Entry<String,JsonNode>> it = json.fields();
			while(it.hasNext()) {
				Map.Entry<String,JsonNode> e = it.next();
				((ObjNode)n).properties.put(e.getKey(),fromJson(e.getValue()));
			}
		} else throw new RuntimeException("You should not reach this");
		return n;
	}
	public static JsonNode toJson(TreeNode n) {
		JsonNode json = null;
		if(n instanceof ObjNode) {
			json = new ObjectNode(JsonNodeFactory.instance);
			for(Map.Entry<String,TreeNode> e : ((ObjNode)n).properties.entrySet()) {
				((ObjectNode)json).put(e.getKey(),toJson(e.getValue()));
			}
		} else if(n instanceof NodeArray) {
			json = new ArrayNode(JsonNodeFactory.instance);
			for(TreeNode ne : ((NodeArray)n).elements) {
				((ArrayNode)json).add(toJson(ne));
			}
		} else if(n instanceof StringValue) {
			json = new TextNode(((StringValue)n).value);
		} else if(n instanceof NumberValue) {
			Number val = ((NumberValue)n).value;
			if(val instanceof Byte
					|| val instanceof Short
					|| val instanceof Integer
					|| val instanceof Long) {
				json = new LongNode(val.longValue());
			} else {
				json = new DoubleNode(val.doubleValue());
			}
		}
		else throw new RuntimeException("You should not reach this");
		return json;
	}
}
