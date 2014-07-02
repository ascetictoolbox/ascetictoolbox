package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.controller.QueriesDBMapper;
import es.bsc.amon.model.Query;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;

/**
 * Created by mmacias on 27/06/14.
 */
public class Queries extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result post() {

        String qs = request().body().asJson().toString();
        ObjectMapper om = new ObjectMapper();
        try {
            Query q = om.readValue(qs.getBytes(), Query.class);
            return ok(QueriesDBMapper.instance.find(q));
        } catch(IOException e) {
            return internalServerError(e.getMessage());
        }
    }

}
