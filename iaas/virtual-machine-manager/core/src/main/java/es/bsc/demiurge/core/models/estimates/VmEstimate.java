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

import java.util.HashMap;
import java.util.Map;

/**
 * VM power and price estimates.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmEstimate {

    private String id;
    private Map<String,Double> estimates = new HashMap<>();

    public VmEstimate(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addEstimate(String name, double value) {
        estimates.put(name, value);
    }
    public double getEstimate(String name) {
        return estimates.get(name);
    }

    public String toJSON() {
        StringBuilder sb = new StringBuilder("{\"id\":\"").append(id).append("\"");
        for(Map.Entry<String,Double> e : estimates.entrySet()) {
            sb.append(",\"").append(e.getKey()).append("\":").append(e.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

}
