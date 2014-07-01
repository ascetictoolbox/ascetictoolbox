package api;

import play.mvc.*;

/**
 * Created by mmacias on 12/06/14.
 */
public class Misc extends Controller {
	public static Result redirectTo(String url) {
		return redirect(url);
	}
}
