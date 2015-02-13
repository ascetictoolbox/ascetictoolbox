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
 * Enumeration of the supported construction heuristics.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public enum ConstructionHeuristic {

    FIRST_FIT("firstFit"), FIRST_FIT_DECREASING("firstFitDecreasing"), BEST_FIT("bestFit"),
            BEST_FIT_DECREASING("bestFitDecreasing");

    private final String name;

    private ConstructionHeuristic(String name) {
        this.name = name;
    }

}
