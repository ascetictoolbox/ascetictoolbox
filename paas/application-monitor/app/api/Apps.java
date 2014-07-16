package api;

import es.bsc.amon.controller.AppsDBMapper;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.text.ParseException;

/**
 * Created by mmacias on 12/06/14.
 */
public class Apps extends Controller {
	/**
	 *
	 * @param start start time in UTC milliseconds. If null, start time will be 0. If negative, will be now - start.
	 * @param end end time in UTC milliseconds. If null, end time will be now
	 * @return
	 */
	public static Result listApps(Long start, Long end) throws ParseException {
        long now = System.currentTimeMillis();
		if(start < 0L) {
			start = now + start;
		}
		if(end <= 0L) {
			end = now;
		}

		return Results.ok(AppsDBMapper.getInstance().getAllApps(start, end).toString());
	}
}
