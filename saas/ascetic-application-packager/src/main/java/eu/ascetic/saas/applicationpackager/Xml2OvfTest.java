package eu.ascetic.saas.applicationpackager;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.ascetic.saas.applicationpackager.ovf.OVFUtils;
import eu.ascetic.saas.applicationpackager.utils.Utils;
import eu.ascetic.saas.applicationpackager.utils.Xml2OvfTranslator;
import eu.ascetic.saas.applicationpackager.xml.model.ApplicationConfig;
import eu.ascetic.saas.applicationpackager.xml.model.CpuSpeed;
import eu.ascetic.saas.applicationpackager.xml.model.Node;
import eu.ascetic.saas.applicationpackager.xml.model.SoftwareInstall;
import eu.ascetic.saas.applicationpackager.xml.model.StorageResource;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OperatingSystem;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.References;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.enums.DiskFormatType;
import eu.ascetic.utils.ovf.api.enums.OperatingSystemType;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;
import eu.ascetic.utils.ovf.api.enums.ResourceType;

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
 * This class is a test of translation from XML to OVF code
 *
 */

public class Xml2OvfTest {

	public static void main(String args[]){
		
		Xml2OvfTranslator xml2ovf = new Xml2OvfTranslator(
				"C:\\data\\projects\\ARI\\it\\ASCETiC\\svn\\trunk\\saas\\ascetic-application-packager"
				+ "\\src\\main\\resources\\atc-single-definitivo-feb2016.xml");
		
		String ovfCode = xml2ovf.translate();
		System.out.println(ovfCode);
		
//		generateOvf(appCfg);
	}

	
	private static ApplicationConfig getXml() {
    	String xmlFileTxt = "";
    	try {
    		xmlFileTxt = Utils.readFile("C:/tests/app-packager/xmlToTest_sept2015.xml");
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
    	
    	return appCfg;
	}
}
