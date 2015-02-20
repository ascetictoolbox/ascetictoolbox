/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package es.bsc.vmplacement.domain;

/**
 * Enumeration of the construction heuristics supported by this library.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public enum ConstructionHeuristic {

    FIRST_FIT("First Fit"), 
    FIRST_FIT_DECREASING("First Fit Decreasing"), 
    BEST_FIT("Best Fit"),
    BEST_FIT_DECREASING("Best Fit Decreasing");

    private final String name;

    private ConstructionHeuristic(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    
}
