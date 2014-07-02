package es.bsc.amon.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Json;

import javax.persistence.Basic;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created by mmacias on 07/06/14.
 */
public class EventsDBMapper {
    public static final String TIMESTAMP = "timestamp";
	public static final String ENDTIME = "endtime";
    public static final String APPID = "appId";
    public static final String NODEID = "nodeId";
    public static final String INSTANCEID = "instanceId";
    public static final String DATA = "data";
	public static final String _ID = "_id";
	public static final String _ID_OID = "$oid";


    public static final String COLL_NAME = "events";

    private static EventsDBMapper instance = null;

    private DBCollection colEvents = null;

    private EventsDBMapper() {
        // default table size to 64 MB
        Logger.info("Creating collection '"+ COLL_NAME +"'...");
        Properties config = DBManager.instance.getConfig();
        DB database = DBManager.instance.getDatabase();

        try {


            int collectionSize = Integer.parseInt(config.getProperty("collection.size"));
            colEvents = database.createCollection(COLL_NAME,
                    new BasicDBObject("capped", true) //enable round robin database
                            .append("size", collectionSize));
        } catch(CommandFailureException cfe) {
            if("collection already exists".equalsIgnoreCase(cfe.getCommandResult().getErrorMessage())) {
                Logger.info("Collection '"+ COLL_NAME +"' already exists. Continuing normally...");
            }
            colEvents = database.getCollection(COLL_NAME);
        }

        // compound index events by timestamp, appId and nodeId
        BasicDBObject indexInfo = new BasicDBObject();
        indexInfo.put(EventsDBMapper.TIMESTAMP, -1); // 1 for ascending, -1 for descending
        indexInfo.put(EventsDBMapper.APPID, 1);
        indexInfo.put(EventsDBMapper.NODEID,1);

        colEvents.createIndex(indexInfo);
    }

    public static EventsDBMapper getInstance() {
        if(instance == null) {
            instance = new EventsDBMapper();
        }
        return instance;
    }


    // cuando quiera encontrar métricas ordenándolas por timestamp:
    // {$orderby : {timestamp : -1}, $query : { $and : [ { timestamp : { $gte : 1402162324197 }}, { timestamp : {$lte : 1402162469099}} ] } }
    // el -1 ordena de mayor a menor y un 1 ordenaría de menor a mayor
    public ObjectNode storeEvent(ObjectNode event) {
	    long timestamp = Calendar.getInstance().getTimeInMillis();

        event.put(EventsDBMapper.TIMESTAMP, timestamp);

		DBObject dbo = (DBObject) JSON.parse(event.toString());
	    colEvents.save(dbo);

	    // return stored id and timestamp
	    ObjectNode on = new ObjectNode(JsonNodeFactory.instance);
		on.put(_ID, dbo.get(_ID).toString());
	    on.put(TIMESTAMP,timestamp);

	    return on;
    }

	public JsonNode getLastEvent(String appId, String nodeId) {
		DBObject query = (DBObject) JSON.parse("{$orderby : {timestamp : -1}, $query : { $and : [ { appId : \"" + appId
				+ "\" }, { nodeId : \"" + nodeId + "\"} ] } }");
		return Json.parse(DBManager.instance.findOne(COLL_NAME,query).toString());
	}

	public ObjectNode get(String id) {
		try {
			String s = getString(id);
			if( s != null ) {
				return (ObjectNode) new ObjectMapper().readTree(s);
			}
		} catch(Exception e) {
			Logger.error(e.getMessage(),e);
		}
		return null;
	}

	public String getString(String id) {
		BasicDBObjectBuilder b = BasicDBObjectBuilder.start();
		b.add(_ID,new ObjectId(id));

		DBCursor cur = colEvents.find(b.get());
		if(cur.hasNext()) {
			return cur.next().toString();
		}
		return null;
	}

	public ObjectNode markAsFinished(String id) {
		BasicDBObjectBuilder q = BasicDBObjectBuilder.start();
		q.add(_ID,new ObjectId(id));
		long timestamp = Calendar.getInstance().getTimeInMillis();

		BasicDBObjectBuilder m = BasicDBObjectBuilder.start();
		m.add("$set", BasicDBObjectBuilder.start(ENDTIME,timestamp).get());

		colEvents.update(q.get(),m.get(),false,false);

		ObjectNode on = new ObjectNode(JsonNodeFactory.instance);
		on.put(_ID,id);
		on.put(ENDTIME,timestamp);

		return on;
	}

    public void remove(DBObject dbo) {
        colEvents.remove(dbo);
    }

    public void remove(String id) {
        BasicDBObjectBuilder q = BasicDBObjectBuilder.start().add(_ID,new ObjectId(id));
        colEvents.findAndRemove(q.get());
    }

    public BasicDBList find(DBObject query) {
        BasicDBList dbl = new BasicDBList();
        DBCursor dbc = colEvents.find(query);
        while(dbc.hasNext()) {
            dbl.add(dbc.next());
        }
        return dbl;
    }



}
