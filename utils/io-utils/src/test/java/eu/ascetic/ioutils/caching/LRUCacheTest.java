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

import eu.ascetic.ioutils.caching.LRUCache;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Richard
 */
public class LRUCacheTest extends TestCase {
    
    public LRUCacheTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of removeEldestEntry method, of class LRUCache.
     */
    public void testRemoveEldestEntry() {
        System.out.println("removeEldestEntry");
        LRUCache<String, String> instance = new LRUCache<>(4, 4);
        instance.put("1", "test1");
        instance.put("2", "test2");
        instance.put("3", "test3");
        instance.put("4", "test4");
        instance.put("5", "test5");
        assert(instance.size() == 4);
        assert (instance.get("1") == null);
        assert (instance.get("2") != null);
        assert (instance.get("3") != null);
        assert (instance.get("4") != null);
        assert (instance.get("5") != null);
        for (Map.Entry<String,String> string : instance.entrySet()) {
            System.out.println(string);
        }
    }
    
}
