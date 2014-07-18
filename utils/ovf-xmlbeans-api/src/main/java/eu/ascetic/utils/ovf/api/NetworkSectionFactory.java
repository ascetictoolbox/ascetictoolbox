/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanNetworkSectionType;

/**
 * Provides factory methods for creating instances of {@link NetworkSection}.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class NetworkSectionFactory {

    /**
     * Creates a new empty instance of {@link NetworkSection} with null internal
     * object references.
     * 
     * @return The new NetworkSection instance
     */
    public NetworkSection newInstance() {
        return new NetworkSection(
                XmlBeanNetworkSectionType.Factory.newInstance());
    }
}
