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
