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

package es.bsc.vmplacement.domain;

/**
 * Enumeration of the options available for the local search heuristics 
 * supported by this library.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public enum LocalSearchHeuristicOption {
    
    SIZE("Size"),
    ACCEPTED_COUNT_LIMIT("Accepted Count Limit"),
    INITIAL_HARD_TEMPERATURE("Initial Hard Temperature"),
    INITIAL_SOFT_TEMPERATURE("Initial Soft Temperature");
    
    private final String name;
    
    private LocalSearchHeuristicOption(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    
}
