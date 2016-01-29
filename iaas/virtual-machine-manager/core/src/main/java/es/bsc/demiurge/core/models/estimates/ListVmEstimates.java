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

package es.bsc.demiurge.core.models.estimates;

import java.util.List;

/**
 * List of VM estimates.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
// Note: this class was only useful to make easier the conversion from JSON using Gson. TODO: remove
public class ListVmEstimates {

    private List<VmEstimate> estimates;

    public ListVmEstimates(List<VmEstimate> estimates) {
        this.estimates = estimates;
    }

    public String toJSON() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for(VmEstimate e : estimates) {
            if(first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(e.toJSON());
        }
        sb.append("]");
        return sb.toString();
    }

}
