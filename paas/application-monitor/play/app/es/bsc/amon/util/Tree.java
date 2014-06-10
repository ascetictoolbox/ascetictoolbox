package es.bsc.amon.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import java.util.*;

/**
 * Created by mmacias on 09/06/14.
 */
public class Tree {
	public static abstract class Node {
	}
	public static class ObjNode extends Node {
		protected Map<String,Node> properties = new HashMap<>();
	}
	// an array of trees
	public static class NodeArray extends Node {
		protected List<Node> elements = new ArrayList<>();
	}
	// constant values: strings, ints, etc... not objects nor arrays of trees
	public static abstract class Value extends Node {

	}
	public static class StringValue extends Value {
		protected String value;
	}
	// not a json array, just stores values to later operate with them
	public static class ValueArray extends Value {
		protected List<Node> values = new ArrayList<>();
	}
	public static class NumberValue extends Value {
		protected Number value;
	}

	public static Node fromJson(JsonNode json) {
		Node n=null;
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
	public static JsonNode toJson(Node n) {
		JsonNode json = null;
		if(n instanceof ObjNode) {
			json = new ObjectNode(JsonNodeFactory.instance);
			for(Map.Entry<String,Node> e : ((ObjNode)n).properties.entrySet()) {
				((ObjectNode)json).put(e.getKey(),toJson(e.getValue()));
			}
		} else if(n instanceof NodeArray) {
			json = new ArrayNode(JsonNodeFactory.instance);
			for(Node ne : ((NodeArray)n).elements) {
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
