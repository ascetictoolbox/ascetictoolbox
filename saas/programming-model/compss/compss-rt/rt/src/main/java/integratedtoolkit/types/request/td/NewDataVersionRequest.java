/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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

package integratedtoolkit.types.request.td;

import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.data.Location;

/**
 * The NewDataVersionRequest represents a notification about the creation of a
 * new data version.
 */
public class NewDataVersionRequest extends TDRequest {

    /** Previous version Data Id */
    private DataInstanceId lastDID;
    /**  name of the data containing file */
    private String fileName;
    /**  location of the data containing file */
    private Location location;

    /** 
     * Constructs a NewDataVersionRequest
     * 
     * @param lastDID  Previous version Data Id
     * @param fileName  Name of the data containing file
     * @param location  Location of the data containing file
     */
    public NewDataVersionRequest(DataInstanceId lastDID, String fileName,
            Location location) {
        super(TDRequestType.NEW_DATA_VERSION);
        this.lastDID = lastDID;
        this.fileName = fileName;
        this.location = location;
    }

    /** 
     * Returns the previous version Data Id
     * 
     * @result the previous version Data Id
     * 
     */
    public DataInstanceId getLastDID() {
        return lastDID;
    }

    /** 
     * Sets the previous version Data Id
     * 
     * @param lastDID the previous version Data Id
     * 
     */
    public void setLastDID(DataInstanceId lastDID) {
        this.lastDID = lastDID;
    }

    /** 
     * Returns the name of the data containing file
     * 
     * @result the name of the data containing file
     * 
     */
    public String getFileName() {
        return fileName;
    }

    /** 
     * Sets the name of the data containing file
     * 
     * @param fileName the name of the data containing file
     * 
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** 
     * Returns the location of the data containing file
     * 
     * @result the location of the data containing file
     * 
     */
    public Location getLocation() {
        return location;
    }

    /** 
     * Sets the location of the data containing file
     * 
     * @param location the location of the data containing file
     * 
     */
    public void setLocation(Location location) {
        this.location = location;
    }
}
