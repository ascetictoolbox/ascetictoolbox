package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.bsc.amon.controller.QueriesDBMapper;
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

        return ok(QueriesDBMapper.instance.aggregate(qs));
    }

}
