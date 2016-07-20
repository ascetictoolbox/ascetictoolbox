package eu.ascetic.saas.experimentmanager.wslayer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import eu.ascetic.saas.experimentmanager.wslayer.exception.ResponseParsingException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSException;

public class XMLHandler implements Handler {

	public String getSingle(InputStream result, String query) throws ResponseParsingException, WSException {
		Document root = parse(result);
			
		try {
			XPath xPath =  XPathFactory.newInstance().newXPath();
			return xPath.compile(query).evaluate(root);
		} catch (Exception e) {
			throw new ResponseParsingException(e);
		} 
	}

	private Document parse(InputStream result) throws WSException {
		Document root = null;
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			root = db.parse(result);
		} catch (Exception e) {
			throw new WSException(-1,"Bad response",e);
		}
		return root;
	}

	public List<String> getList(InputStream result, String query) throws ResponseParsingException, WSException {
		Document root = parse(result);
		try {
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList resp = (NodeList) xPath.compile(query).evaluate(root, XPathConstants.NODESET); 
			return toStringList(resp);
		} catch (Exception e) {
			throw new ResponseParsingException(e);
		} 
	}

	public static List<String> toStringList(NodeList nl){
		List<String> arr= new ArrayList<String>();
		int len = nl.getLength();
		for (int i=0;i<len;++i){
			arr.add(nl.item(i).getTextContent());
		}
		return arr;
	}

	public String getAccepted() {
		return "application/xml";
	}
	
}
