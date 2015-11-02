/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.ioutils.caching;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This creates a least recently used cache, of a set size.
 * @author Richard
 * @param <K> the type of keys maintained by this map 
 * @param <V> the type of mapped values
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private int maxCapacity = 100;  
    
    /**
     * This creates a Least recently used cache (LRU).
     *
     * @param initialCapacity The initial capacity of this least recently used cache.
     * @param maxCapacity The maximum capacity of this least recently used cache
     * before it removes the oldest entry.
     * @param loadFactor The load factor used to decide when to expand the current
     * size of the least recently used cache.
     */
    public LRUCache(int initialCapacity, int maxCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
        this.maxCapacity = (maxCapacity >= initialCapacity ? maxCapacity : initialCapacity);
    }

    /**
     * This creates a Least recently used cache (LRU).
     *
     * @param initialCapacity The initial capacity of this least recently used cache.
     * @param maxCapacity The maximum capacity of this least recently used cache
     * before it removes the oldest entry.
     */
    public LRUCache(int initialCapacity, int maxCapacity) {
        super(initialCapacity, 0.75f, true);
        this.maxCapacity = maxCapacity;
    } 

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }

}
