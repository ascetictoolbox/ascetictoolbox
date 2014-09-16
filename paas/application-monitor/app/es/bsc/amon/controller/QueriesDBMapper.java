/*
 * Author: Mario Macias (Barcelona Supercomputing Center). 2014
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 */

package es.bsc.amon.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import play.Logger;
import play.libs.Json;

/**
 * Created by mmacias on 02/07/14.
 */
public class QueriesDBMapper {
    public static final QueriesDBMapper instance = new QueriesDBMapper();
    private QueriesDBMapper() {}

    public ArrayNode find(String query) {
        DBObject dbo = (DBObject)JSON.parse(query);

        return (ArrayNode)Json.parse(EventsDBMapper.getInstance().find(dbo).toString());
    }

    public ArrayNode aggregate(String query) {
        Object raw = JSON.parse(query);
        ArrayNode ret = new ArrayNode(JsonNodeFactory.instance);

        if(raw instanceof BasicDBObject) {
            ret = (ArrayNode)Json.parse(EventsDBMapper.getInstance().aggregate((BasicDBObject)raw).toString());
        } else if(raw instanceof BasicDBList) {
            ret = (ArrayNode)Json.parse(EventsDBMapper.getInstance().aggregate((BasicDBList)raw).toString());
        }

        return ret;
    }
    public static final String START = "start";
    public static final String END = "end";
    public static final String APPID = "appId";
    public static final String NODEID = "nodeId";
    public static final String INSTANCEID = "instanceId";
    public static final String OP = "op";
    public static final String DATA = "data";

    public enum Operation { sum, avg, max, min, first, last, count, array };
}
