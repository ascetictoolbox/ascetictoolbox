package eu.ascetic.saas.applicationpackager;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.ascetic.saas.applicationpackager.utils.Utils;
import eu.ascetic.saas.applicationpackager.xml.model.ApplicationConfig;
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
 * This class is a test of marshall/unmarshall operation with XML code
 *
 */
public class XmlAppTest 
{
    public static void main( String[] args )
    {
    	String xmlFileTxt = "";
    	try {
    		xmlFileTxt = Utils.readFile("C:/tests/app-packager/xmlToTest.txt");
    	} catch (IOException e1) {
    		// TODO Auto-generated catch block
    		e1.printStackTrace();
    	}

    	ApplicationConfig appCfg = null;
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationConfig.class);
    		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    		appCfg = (ApplicationConfig) jaxbUnmarshaller.unmarshal(new StringReader(xmlFileTxt));
    		System.out.println(xmlFileTxt);
    	} catch (JAXBException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }
}
