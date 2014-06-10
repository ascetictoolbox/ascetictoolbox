package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import es.bsc.amon.DBManager;

import java.util.*;

/**
 * Created by mmacias on 08/06/14.
 */
public class Apps {
    private static Apps instance;

    public static Apps getInstance() {
        if(instance == null) {
            instance = new Apps();
        }
        return instance;
    }

    public ArrayNode getAllApps(long start, long end) {
        ArrayNode apps = new ArrayNode(JsonNodeFactory.instance);

        DBObject query = (DBObject) JSON.parse("{$orderby : {timestamp : -1}, $query : { $and : [ { timestamp : { $gte : " + start
                + " }}, { timestamp : {$lte : " + end + "}} ] } }");

        ArrayNode ret = DBManager.instance.find(Events.COLL_NAME, query);

        Map<String,Set<String>> appsInfo = new HashMap<>();

        Iterator<JsonNode> iter = ret.iterator();
        while(iter.hasNext()) {
            JsonNode event = iter.next();
            String appName = event.get(Events.APPID).asText();
            String nodeName = event.get(Events.NODEID).asText();
            Set<String> appSet = appsInfo.get(appName);
            if (appSet == null) {
                appSet = new TreeSet<>();
                appsInfo.put(appName, appSet);
            }
            appSet.add(nodeName);
        }

        ArrayNode all = new ArrayNode(JsonNodeFactory.instance);
        for(Map.Entry<String,Set<String>> entry : appsInfo.entrySet()) {
            ObjectNode app = new ObjectNode(JsonNodeFactory.instance);
            ArrayNode nodes = new ArrayNode(JsonNodeFactory.instance);
            for(String n : entry.getValue()) {
                nodes.add(n);
            }
            app.put(entry.getKey(), nodes);
            all.add(app);
        }

        return all;
    }
}
