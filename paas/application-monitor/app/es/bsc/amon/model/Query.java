package es.bsc.amon.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by mmacias on 27/06/14.
 */
public class Query {
	public long start;
	public long end;
	public String appId;
	public String nodeId;
	public String instanceId;
	public Operation op;
	public JsonNode query;

	public enum Operation { sum, avg, max, min, first, last, count, array };
}
