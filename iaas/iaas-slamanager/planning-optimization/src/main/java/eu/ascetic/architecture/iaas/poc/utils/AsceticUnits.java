/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.architecture.iaas.poc.utils;

import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.vocab.units;
import org.slasoi.slamodel.vocab.xsd;

import eu.ascetic.architecture.iaas.poc.enums.CoreUnit;
import eu.ascetic.architecture.iaas.poc.enums.FrequencyUnits;
import eu.ascetic.architecture.iaas.poc.enums.MemoryUnits;
import eu.ascetic.architecture.iaas.poc.exceptions.NotSupportedUnitException;

public class AsceticUnits {

	public static final FrequencyUnits DEFAULT_FREQUENCY_UNIT = FrequencyUnits.MHz;

	public static final MemoryUnits DEFAULT_MEMORY_UNIT = MemoryUnits.MB;

	public static final CoreUnit DEFAULT_CORE_UNIT = CoreUnit.integer;

	public static final double FACTOR_OF_MAGNITUDE_FREQUENCY = 1024;

	public static final double FACTOR_OF_MAGNITUDE_MEMORY = 1024;

	public static double normalizeUnit(double value, Unit unit) throws NotSupportedUnitException {
		double normValue = -1;
		if (unit instanceof FrequencyUnits)
			normValue = normalizeFrequencyUnit((FrequencyUnits) unit, value);
		else if (unit instanceof MemoryUnits)
			normValue = normalizeMemoryUnit((MemoryUnits) unit, value);
		else if (unit instanceof CoreUnit)
			normValue = value;
		if (normValue != -1)
			return normValue;
		else
			throw new NotSupportedUnitException("Problem during the normalization of units!");
	}

	private static double normalizeFrequencyUnit(FrequencyUnits frequencyUnit, double frequencyValue) throws NotSupportedUnitException {
		return frequencyValue * factorOfMagnitudeFrquency(frequencyUnit, DEFAULT_FREQUENCY_UNIT);
	}

	private static double normalizeMemoryUnit(MemoryUnits memoryUnit, double memoryValue) throws NotSupportedUnitException {
		return memoryValue * factorOfMagnitudeMemory(memoryUnit, DEFAULT_MEMORY_UNIT);
	}

	private static double factorOfMagnitudeFrquency(FrequencyUnits source, FrequencyUnits dest) {
		return Math.pow(FACTOR_OF_MAGNITUDE_FREQUENCY, source.ordinal() - dest.ordinal());
	}

	private static double factorOfMagnitudeMemory(MemoryUnits source, MemoryUnits dest) {
		return Math.pow(FACTOR_OF_MAGNITUDE_MEMORY, source.ordinal() - dest.ordinal());
	}

	public static FrequencyUnits convertToFreqEnum(String unit) throws NotSupportedUnitException {
		if (unit.equals("KHz"))
			return FrequencyUnits.KHz;
		else if (unit.equals("MHz"))
			return FrequencyUnits.MHz;
		else if (unit.equals("GHz"))
			return FrequencyUnits.GHz;
		else
			throw new NotSupportedUnitException("Frequency unit not recognized. Allowable units are: KHz, MHz, GHz");
	}

	public static MemoryUnits convertToMemEnum(String unit) throws NotSupportedUnitException {
		if (unit.equals("KB"))
			return MemoryUnits.KB;
		else if (unit.equals("MB"))
			return MemoryUnits.MB;
		else if (unit.equals("GB"))
			return MemoryUnits.GB;
		else
			throw new NotSupportedUnitException("Memory unit not recognized. Allowable units are: KB, MB, GB");
	}

	public static Unit convertToEnum(String unit) throws NotSupportedUnitException {
		if (unit.equals("KHz"))
			return FrequencyUnits.KHz;
		else if (unit.equals("MHz"))
			return FrequencyUnits.MHz;
		else if (unit.equals("GHz"))
			return FrequencyUnits.GHz;
		else if (unit.equals("KB"))
			return MemoryUnits.KB;
		else if (unit.equals("MB"))
			return MemoryUnits.MB;
		else if (unit.equals("GB"))
			return MemoryUnits.GB;
		else if (unit.equals("xsd:integer"))
			return CoreUnit.integer;
		else
			throw new NotSupportedUnitException("Unit not recognized. Allowable units are: KHz, MHz, GHz, KB, MB, GB, integer");
	}

	public static STND convertToSTND(String unit) throws NotSupportedUnitException {
		if (unit.equals("KHz"))
			return units.KHz;
		else if (unit.equals("MHz"))
			return units.MHz;
		else if (unit.equals("GHz"))
			return units.GHz;
		else if (unit.equals("KB"))
			return units.KB;
		else if (unit.equals("MB"))
			return units.MB;
		else if (unit.equals("GB"))
			return units.GB;
		else if (unit.equals("integer"))
			return xsd.integer;
		else if (unit.equals("xsd:integer"))
			return xsd.integer;
		else if (unit.equals(xsd.$string))
			return xsd.string;
		else if (unit.equals("xsd:string"))
			return xsd.string;
		else if (unit.equals("xsd:boolean"))
			return xsd._boolean;
		else
			throw new NotSupportedUnitException("Unit not recognized. Allowable units are: KHz, MHz, GHz, KB, MB, GB, integer,string");
	}
}
