package eu.ascetic.paas.applicationmanager.util;


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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail: david.garciaperez@atos.net 
 * 
 * Simple class to help to implement equals methods
 *
 */
public final class EqualsUtil {

	  static public boolean areEqual(boolean aThis, boolean aThat){
	    //System.out.println("boolean");
	    return aThis == aThat;
	  }

	  static public boolean areEqual(char aThis, char aThat){
	    //System.out.println("char");
	    return aThis == aThat;
	  }

	  static public boolean areEqual(long aThis, long aThat){
	    /*
	    * Implementation Note
	    * Note that byte, short, and int are handled by this method, through
	    * implicit conversion.
	    */
	    //System.out.println("long");
	    return aThis == aThat;
	  }

	  static public boolean areEqual(float aThis, float aThat){
	    //System.out.println("float");
	    return Float.floatToIntBits(aThis) == Float.floatToIntBits(aThat);
	  }

	  static public boolean areEqual(double aThis, double aThat){
	    //System.out.println("double");
	    return Double.doubleToLongBits(aThis) == Double.doubleToLongBits(aThat);
	  }

	  /**
	  * Possibly-null object field.
	  *
	  * Includes type-safe enumerations and collections, but does not include
	  * arrays. See class comment.
	  */
	  static public boolean areEqual(Object aThis, Object aThat){
	    //System.out.println("Object");
	    return aThis == null ? aThat == null : aThis.equals(aThat);
	  }
	}
