package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 * This class is the Unit test that verifies the pojo Value
 */
public class ValueTest {
	
	@Test
	public void pojoTest() {
		Value value = new Value();
		FuncExpr funcExpr = new FuncExpr();
		value.setFuncExpr(funcExpr);
		CONST constVariable = new CONST();
		value.setConstVariable(constVariable);
		
		assertEquals(funcExpr, value.getFuncExpr());
		assertEquals(constVariable, value.getConstVariable());
	}
}