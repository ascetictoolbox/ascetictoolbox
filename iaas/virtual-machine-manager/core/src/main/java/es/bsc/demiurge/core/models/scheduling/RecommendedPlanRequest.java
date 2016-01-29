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

package es.bsc.demiurge.core.models.scheduling;

import com.google.common.base.Preconditions;

/**
 * This class represents a request made by a client that asks for a recommended deployment plan.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class RecommendedPlanRequest {

    private final int timeLimitSeconds; // maximum execution time allowed for the engine that computes the plan
    private final String constructionHeuristicName; // name of the heuristic to be used by the engine
    private final LocalSearchAlgorithmOptionsSet localSearchAlgorithm; // local search algorithm to be used

    public RecommendedPlanRequest(int timeLimitSeconds, String constructionHeuristicName,
            LocalSearchAlgorithmOptionsSet localSearchAlgorithm) {
        validateConstructorParams(timeLimitSeconds);
        this.timeLimitSeconds = timeLimitSeconds;
        this.constructionHeuristicName = constructionHeuristicName;
        this.localSearchAlgorithm = localSearchAlgorithm;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public String getConstructionHeuristicName() {
        return constructionHeuristicName;
    }

    public LocalSearchAlgorithmOptionsSet getLocalSearchAlgorithm() {
        return localSearchAlgorithm;
    }

    private void validateConstructorParams(int timeLimitSeconds) {
        Preconditions.checkNotNull(timeLimitSeconds);
        Preconditions.checkArgument(timeLimitSeconds >= 0, "time limit was %s but expected non-negative",
                timeLimitSeconds);
    }

}
