package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.DBManager;
import models.Apps;
import models.Events;
import play.mvc.*;

import java.text.ParseException;
import java.util.*;

public class EventsController extends Controller {

    /**
     *
     * @param start start time in UTC milliseconds. If null, start time will be 0
     * @param end end time in UTC milliseconds. If null, end time will be now
     * @return
     */
    public static Result listApps(Long start, Long end) throws ParseException {
        if(start < 0L) {
            start = 0L;
        }
        if(end < 0L) {
            end = System.currentTimeMillis();
        }

        return ok(Apps.getInstance().getAllApps(start,end).toString());
    }

    public static Result getLastEventForApp(String appId) {

        return ok("TODO");

    }

    public static Result getLastEvent(String appId, String nodeId) {
        return ok("TODO");
    }


    @BodyParser.Of(BodyParser.Json.class)
    public static Result postEvent(String appId, String nodeId) {

        long timestamp = Calendar.getInstance().getTimeInMillis();
        JsonNode body = request().body().asJson();

        Events.getInstance().storeEvent(timestamp,appId,nodeId,body);

        return ok();
    }

}
