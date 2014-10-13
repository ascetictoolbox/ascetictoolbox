package es.bsc.amon.gui;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.*;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;
import es.bsc.amon.controller.EventsDBMapper;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Json;

/**
 * Created by mmacias on 13/10/14.
 */
public class GuiMetricsDBMapper {
    private static final String COLL_NAME = "guiMetrics";
    private DBCollection collection = null;

    public static GuiMetricsDBMapper instance = null;
    public static GuiMetricsDBMapper getInstance() {
        if(instance == null) {
            instance = new GuiMetricsDBMapper();
        }
        return instance;
    }

    private GuiMetricsDBMapper() {
        // default table size to 64 MB
        Logger.info("Creating collection '" + COLL_NAME + "'...");
        DB database = DBManager.instance.getDatabase();

        try {
            collection = database.createCollection(COLL_NAME,new BasicDBObject());
        } catch(CommandFailureException cfe) {
            if("collection already exists".equalsIgnoreCase(cfe.getCommandResult().getErrorMessage())) {
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
