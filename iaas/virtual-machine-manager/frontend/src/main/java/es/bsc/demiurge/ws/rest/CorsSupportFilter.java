/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.ws.rest;


/**
 * CORS (cross-origin resource sharing) support filter. I could not connect to the REST service from an
 * external application without applying this CORS filter. For more information about CORS check
 * http://enable-cors.org/
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 * @deprecated
 */
@Deprecated
public class CorsSupportFilter { //implements ContainerResponseFilter {

//    @Override
//    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
//    /*
//
//
//        resp.getHttpHeaders().putSingle("Access-Control-Allow-Origin", "*");
//
//        resp.getHttpHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//
//        if (null != req.getHeaderValue("Access-Control-Request-Headers")) {
//            resp.getHttpHeaders().putSingle("Access-Control-Allow-Headers", req.getHeaderValue("Access-Control-Request-Headers"));
//        }
//        return resp;
//        */
//    }


}