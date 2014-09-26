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

package es.bsc.vmmanagercore.model.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class LocalSearchAlgorithm {

    private final String name;
    private final List<String> options;

    public LocalSearchAlgorithm(String name, List<String> options) {
        this.name = name;
        this.options = new ArrayList<>(options);
    }

    public String getName() {
        return name;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }
    
}
