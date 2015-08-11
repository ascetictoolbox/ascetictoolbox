package eu.ascetic.saas.applicationpackager;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.ascetic.saas.applicationpackager.utils.Utils;
import eu.ascetic.saas.applicationpackager.xml.model.ApplicationConfig;

/**
 * Hello world!
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
