package api;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by mmacias on 27/06/14.
 */
public class Queries extends Controller {

	@BodyParser.Of(BodyParser.Json.class)
	public static Result submitQuery() {
		return notFound();
	}
}
