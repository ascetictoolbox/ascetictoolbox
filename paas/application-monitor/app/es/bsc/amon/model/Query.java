package es.bsc.amon.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by mmacias on 27/06/14.
 */
public class Query {
	public Long start;
	public Long end;
	public String appId;
	public String nodeId;
	public String instanceId;
	public Operation op;
	public ObjectNode query;

	public enum Operation { sum, avg, max, min, first, last, count, array };
}
