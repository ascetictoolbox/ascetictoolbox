package eu.ascetic.saas.applicationpackager.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class exposes many useful methods to other project classes
 *
 */

public class Utils {

	/**
	 * Read file.
	 *
	 * @param path the path
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String readFile(String path) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String everything;
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        everything = sb.toString();
	    } finally {
	        br.close();
	    }
	    return everything;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceSpecialCharacters(String str){
		String s = str;
		str = str.replaceAll(">", "&gt;");
		str = str.replaceAll("<", "&lt;");
		return str;
	}
	
	public static String replaceAmpersand(String str){
//		System.out.println("**********************************************************");
//		System.out.println(str.replaceAll("&amp;", "&"));
//		System.out.println("**********************************************************");
		return str.replaceAll("&amp;", "&");
	}
	
	public static String replaceArrow(String str){
//		System.out.println("**********************************************************");
//		System.out.println(str.replaceAll("&amp;", "&"));
//		System.out.println("**********************************************************");
		return str.replaceAll("=>", "=&gt;");
	}
	
	public static int getIntValue(double d){
		Double number = new Double(d);
		return number.intValue();		
	}
	


}
