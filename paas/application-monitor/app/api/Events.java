package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.controller.EventsDBMapper;
import play.Logger;
import play.mvc.*;

import java.util.*;

public class Events extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result post() {

        ObjectNode body = (ObjectNode)request().body().asJson();

        return ok(EventsDBMapper.getInstance().storeEvent(body).toString());
    }

	@BodyParser.Of(BodyParser.Json.class)
	public static Result update() {
		return notFound("TO DO"); //by the moment
	}

	public static Result get(String id) {
		String s = EventsDBMapper.getInstance().getString(id);
		if(s == null) {
			return notFound("Event with _id " + id + " does not exist");
		} else {
			return ok(s);
		}
	}

	public static Result finish(String id) {
		try {
			ObjectNode o = EventsDBMapper.getInstance().markAsFinished(id);
			return ok(o.toString());
		} catch(Exception e) {
			Logger.error(e.getMessage(),e);
		}
		return notFound("Didn't find event with _id " + id);
	}
}
