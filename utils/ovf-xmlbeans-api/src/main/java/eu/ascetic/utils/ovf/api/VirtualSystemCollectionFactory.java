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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemCollectionType;

/**
 * Provides factory methods for creating instances of
 * {@link VirtualSystemCollection}.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class VirtualSystemCollectionFactory {

    /**
     * Creates a new empty instance of {@link VirtualSystemCollection} with null
     * internal object references.
     * 
     * @return The new VirtualSystemCollection instance
     */
    public VirtualSystemCollection newInstance() {
        return new VirtualSystemCollection(
                XmlBeanVirtualSystemCollectionType.Factory.newInstance());
    }
}
