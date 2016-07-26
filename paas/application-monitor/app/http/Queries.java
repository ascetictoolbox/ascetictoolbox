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

package http;


import es.bsc.amon.controller.QueriesDBMapper;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class Queries extends Controller {

    @BodyParser.Of(BodyParser.AnyContent.class)
    public static Result post() {
        String contentType = request().getHeader("Content-Type");
        if("application/json".equals(contentType)) {
            return ok(QueriesDBMapper.INSTANCE.aggregate(request().body().asJson()));
        } else if("text/plain".equals(contentType)) {
            return ok(QueriesDBMapper.INSTANCE.aggregate(request().body().asText()));
        } else {
            return badRequest("Unknown mime type. Supported types are:\n" +
                    "\tapplication/json for MongoDB JSON-like aggregation query\n" +
                    "\ttext/plain for MongoAL aggregation language (http://github.com/mariomac/MongoAL)");

        }
    }

}
