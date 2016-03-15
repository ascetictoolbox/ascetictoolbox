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

package es.bsc.demiurge.ws.rest.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by mmacias on 15/10/15.
 */
// todo: parece que en las ultimas versiones aquí hay algun problema con java reflections
public class ErrorHandler extends WebApplicationException {
	private Response.Status status;
	public ErrorHandler(Throwable cause, Response.Status status) {
		super(cause, status);
		this.status = status;
	}

	@Override
	public Response getResponse() {
		StringBuilder sb = new StringBuilder("Error deploying VMs: ");
		Throwable th = getCause();
		while(th != null) {
			sb.append("\n\tCaused by ").append(th.getClass().getName()).append(": ").append(th.getMessage());
			th = th.getCause();
		}
		return Response.status(status)
				.entity(sb.toString()).type(MediaType.TEXT_PLAIN_TYPE).build();
	}
}