package eu.ascetic.paas.applicationmanager.dao;

import java.util.List;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * Describes the generic Data Access Object 
 * It is used by Spring to do the specific DAO injections
 
 */

public interface DAO<T> {
	public boolean save(T t);
	public List<T> getAll();
	public T getById(int id);
	public boolean delete(T something);
	public boolean update(T something);
}
