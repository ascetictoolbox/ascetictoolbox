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

import com.google.common.base.Preconditions;

/**
 * This class represents a request made by a client that asks for a recommended deployment plan.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class RecommendedPlanRequest {

    private final int timeLimitSeconds; // maximum execution time allowed for the engine that computes the plan
    private final String constructionHeuristicName; // name of the heuristic to be used by the engine
    private final LocalSearchAlgorithmsOptionsSet localSearchAlgorithm; // local search algorithm to be used

    public RecommendedPlanRequest(int timeLimitSeconds, String constructionHeuristicName,
            LocalSearchAlgorithmsOptionsSet localSearchAlgorithm) {
        validateConstructorParams(timeLimitSeconds, constructionHeuristicName, localSearchAlgorithm);
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

    public LocalSearchAlgorithmsOptionsSet getLocalSearchAlgorithm() {
        return localSearchAlgorithm;
    }

    private void validateConstructorParams(int timeLimitSeconds, String constructionHeuristicName,
            LocalSearchAlgorithmsOptionsSet localSearchAlgorithm) {
        Preconditions.checkNotNull(timeLimitSeconds);
        Preconditions.checkArgument(timeLimitSeconds >= 0, "Argument was %s but expected non-negative",
                timeLimitSeconds);
        Preconditions.checkNotNull(constructionHeuristicName);
        Preconditions.checkNotNull(localSearchAlgorithm);
    }

}
