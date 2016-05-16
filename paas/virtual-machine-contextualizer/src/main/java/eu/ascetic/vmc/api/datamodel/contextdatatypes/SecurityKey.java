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

import java.util.Arrays;

/**
 * Class for storing security key attributes and keyData.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.2
 */
public class SecurityKey {

    private String name;
    private byte[] keyData;

    /**
     * Creates a new instance of a security key.
     * 
     * @param name
     *            The key name
     * @param initialKeyData
     *            The data representing the key
     */
    public SecurityKey(String name, byte[] initialKeyData) {
        this.name = name;
        if (initialKeyData == null) {
            this.keyData = new byte[0];
        } else {
            this.keyData = Arrays.copyOf(initialKeyData, initialKeyData.length);
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the keyData
     */
    public byte[] getKeyData() {
        return keyData.clone();
    }

    /**
     * @param keyData
     *            the keyData to set
     */
    public void setKeyData(byte[] newKeyData) {
        if (newKeyData == null) {
            this.keyData = new byte[0];
        } else {
            this.keyData = Arrays.copyOf(newKeyData, newKeyData.length);
        }
    }
}
