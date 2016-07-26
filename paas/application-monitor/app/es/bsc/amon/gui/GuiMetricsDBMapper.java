package es.bsc.amon.gui;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.*;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Json;

/**
 * Created by mmacias on 13/10/14.
 */
public enum GuiMetricsDBMapper {
	INSTANCE;
    private static final String COLL_NAME = "guiMetrics";
    private DBCollection collection = null;

    private GuiMetricsDBMapper() {
        // default table size to 64 MB
        Logger.info("Creating collection '" + COLL_NAME + "'...");
        DB database = DBManager.INSTANCE.getDatabase();

        try {
            collection = database.createCollection(COLL_NAME,new BasicDBObject());
        } catch(MongoException cfe) {
            if(cfe.getCode() == DBManager.COLLECTION_ALREADY_EXISTS) {
                Logger.info("Collection '"+ COLL_NAME +"' already exists. Continuing normally...");
            }
            collection = database.getCollection(COLL_NAME);
        }

    }

    // Returns the ID of the inserted item
    public String addPanel(String panel) {
        BasicDBObject dbo = (BasicDBObject) JSON.parse(panel);
        collection.insert(dbo);

        return dbo.getObjectId("_id").toString();
    }

    public ArrayNode getPanels() {
        BasicDBList dbl = new BasicDBList();
        DBCursor dbc = collection.find();
        while(dbc.hasNext()) {
            dbl.add(dbc.next());
        }
        return (ArrayNode)Json.parse(dbl.toString());
    }

    public void deletePanel(String id) {
        BasicDBObject dbo = new BasicDBObject();
        dbo.append("_id", new ObjectId(id));
        collection.remove(dbo);
    }

}
