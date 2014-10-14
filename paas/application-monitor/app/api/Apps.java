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
package api;

import es.bsc.amon.controller.AppsDBMapper;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.text.ParseException;

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

		return Results.ok(AppsDBMapper.getInstance().getAllApps(start, end,false).toString());
	}

    public static Result listInstances(Long start, Long end) throws ParseException {
        long now = System.currentTimeMillis();
        if(start < 0L) {
            start = now + start;
        }
        if(end <= 0L) {
            end = now;
        }

        return Results.ok(AppsDBMapper.getInstance().getAllApps(start, end,true).toString());
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result onAppFinished() {
        try {
            String appInstance = request().body().asJson().toString();
            AppsDBMapper.getInstance().addAppInstance(appInstance);
            return ok();
        } catch(AppsDBMapper.AppException e) {
            e.printStackTrace();
            return badRequest(e.getMessage());
        }
    }

    public static Result listFinishedInstances(Long start, Long end, Integer limit) {
        long now = System.currentTimeMillis();
        if(start < 0L) {
            start = now + start;
        }
        if(end <= 0L) {
            end = now;
        }
        if(limit <= 0) {
            limit = null;
        }
        return ok(AppsDBMapper.getInstance().getFinishedAppInstances(start, end, limit));
    }

}
