/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */



package datastructure;

import utils.Constant;


/**
 * <code>CPU</code> represents CPU in resource request.
 * 
 */
public class CPU extends Resource {
    /**
     * CPU speed.
     */
    private float speed;
    /**
     * CPU architecture.
     */
    private String architecture = "";

    public CPU() {
        this.setResourceName(Constant.CPU);
    }

    /**
     * Gets speed.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets speed.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Gets architecture.
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * Sets architecture.
     */
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

}
