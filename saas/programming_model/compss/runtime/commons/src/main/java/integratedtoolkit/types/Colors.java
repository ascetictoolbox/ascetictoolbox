/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.types;

import integratedtoolkit.types.exceptions.NonInstantiableException;

public class Colors {

    public static final String BLACK = "#000000";
    public static final String DARK_BLUE = "#0000ff";
    public static final String LIGHT_GREEN = "#00ff00";
    public static final String LIGHT_BLUE = "#00ffff";
    public static final String VIOLET = "#6600ff";
    public static final String DARK_RED = "#990000";
    public static final String PURPLE = "#9900ff";
    public static final String BROWN = "#996600";
    public static final String DARK_GREEN = "#999900";
    public static final String GREY = "#c0c0c0";
    public static final String RED = "#ff0000";
    public static final String PINK = "#ff00ff";
    public static final String YELLOW = "#ffff00";
    public static final String WHITE = "#ffffff";

    private Colors() {
        throw new NonInstantiableException("Colors should not be instantiated");
    }

}
