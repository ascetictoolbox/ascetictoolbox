/**
 *  Copyright 2013 University of Leeds
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
package eu.ascetic.vmc.api.datamodel.contextdatatypes;

/**
 * Class for storing the attributes of service end point for use within a VM.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.2
 */
public class EndPoint {

    private String id;
    private String uri;
    private String type;
    private String subtype;
    private String interval;

    /**
     * Default constructor.
     * 
     * @param uri
     *            The URI that the endpoint points to.
     */
    public EndPoint(String id, String uri, String type, String subtype, String interval) {
        this.id = id;
        this.uri = uri;
        this.type = type;
        this.setSubtype(subtype);
        this.interval = interval;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the subtype
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * @param subtype the subtype to set
     */
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    /**
     * @return the interval
     */
    public String getInterval() {
        return interval;
    }

    /**
     * @param interval
     *            the interval to set
     */
    public void setInterval(String interval) {
        this.interval = interval;
    }

}
