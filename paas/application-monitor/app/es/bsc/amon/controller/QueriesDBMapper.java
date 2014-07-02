package es.bsc.amon.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.BasicDBObjectBuilder;
import es.bsc.amon.model.Query;
import play.libs.Json;

/**
 * Created by mmacias on 02/07/14.
 */
public class QueriesDBMapper {
    public static final QueriesDBMapper instance = new QueriesDBMapper();
    private QueriesDBMapper() {}

    public ArrayNode find(Query query) {
        BasicDBObjectBuilder dbo = BasicDBObjectBuilder.start();
        if(query.appId != null) {
            dbo.add(EventsDBMapper.APPID,query.appId);
        }
        if(query.nodeId != null) {
            dbo.add(EventsDBMapper.NODEID,query.nodeId);
        }
        if(query.instanceId != null) {
            dbo.add(EventsDBMapper.INSTANCEID,query.instanceId);
        }
        if(query.start != null && query.end != null) {
            dbo.append(EventsDBMapper.TIMESTAMP,BasicDBObjectBuilder.start("$gt",query.start-1)
                                                .add("$lt",query.end+1).get());
        } else if(query.start != null) {
            dbo.append(EventsDBMapper.TIMESTAMP,BasicDBObjectBuilder.start("$gt",query.start-1).get());
        } else if(query.end != null) {
            dbo.append(EventsDBMapper.TIMESTAMP,BasicDBObjectBuilder.start("$lt",query.end-1).get());
        }


        return (ArrayNode)Json.parse(EventsDBMapper.getInstance().find(dbo.get()).toString());
    }
}
