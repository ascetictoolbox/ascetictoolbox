package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static eu.ascetic.paas.applicationmanager.Dictionary.SLA_XMLNS;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
 * Model classs that represents the VariableDeclar tag in an SLA Template xml document
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "VariableDeclr", namespace = SLA_XMLNS)
public class VariableDeclr {
	@XmlElement(name = "Var", namespace = SLA_XMLNS)
	private String var;
	@XmlElement(name = "Expr", namespace = SLA_XMLNS)
	private Expr expr;

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public Expr getExpr() {
		return expr;
	}

	public void setExpr(Expr expr) {
		this.expr = expr;
	}
}