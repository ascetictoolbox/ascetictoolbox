package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;
import play.Logger;

import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Created by mmacias on 07/06/14.
 */
public class Events {
    public static final String TIMESTAMP = "timestamp";
    public static final String APPID = "appId";
    public static final String NODEID = "nodeId";
    public static final String DATA = "data";


    public static final String COLL_NAME = "events";

    private static Events instance = null;

    private DBCollection colEvents = null;

    private Events() {
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
        indexInfo.put(Events.TIMESTAMP, -1); // 1 for ascending, -1 for descending
        indexInfo.put(Events.APPID, 1);
        indexInfo.put(Events.NODEID,1);

        colEvents.createIndex(indexInfo);
    }

    public static Events getInstance() {
        if(instance == null) {
            instance = new Events();
        }
        return instance;
    }


    // cuando quiera encontrar métricas ordenándolas por timestamp:
    // {$orderby : {timestamp : -1}, $query : { $and : [ { timestamp : { $gte : 1402162324197 }}, { timestamp : {$lte : 1402162469099}} ] } }
    // el -1 ordena de mayor a menor y un 1 ordenaría de menor a mayor
    public void storeEvent(long timestamp, String appId, String nodeId, JsonNode body) {
        ObjectNode on = new ObjectNode(JsonNodeFactory.instance);
        on.put(Events.TIMESTAMP, timestamp);
        on.put(Events.APPID, appId);
        on.put(Events.NODEID, nodeId);

        if(body != null) {
            on.put(Events.DATA, body);
        }
        colEvents.save((DBObject) JSON.parse(on.toString()));
    }


}
