package eu.ascetic.paas.slam.pac.impl.provider.reporting;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.vocab.sla;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.ascetic.paas.slam.pac.impl.provider.translation.SlaTranslator;
import eu.ascetic.paas.slam.pac.impl.provider.translation.SlaTranslatorImpl;
public class GetSLAClient {
	private String slaId;
	private String wsURL;
	
	public GetSLAClient(String wsURL, String slaId) {
		super();
		this.slaId = slaId;
		this.wsURL = wsURL;
	}

	private SlaTranslator slaTranslator = new SlaTranslatorImpl();

	private static Logger logger = Logger.getLogger(GetSLAClient.class.getName());
	
	public SLA getSLA() throws MalformedURLException,
	IOException {

		//Code to make a webservice HTTP request
		String responseString = "";
		String outputString = "";
		URL url = new URL(wsURL);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection)connection;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		//testing purposes, remove...
		String slaId = "cbf1f8e2-4d90-46f4-be60-7a198f4def39";
		
		String xmlInput =
				" <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:rep=\"http://reportingWS.businessManager.slasoi.org\">"+
						"<soapenv:Header/>"+
						"<soapenv:Body>"+
						"<rep:getSLA>"+
						"<slaId>"+slaId+"</slaId>"+
						"</rep:getSLA>"+
						"</soapenv:Body>"+
						"</soapenv:Envelope>";

		byte[] buffer = new byte[xmlInput.length()];
		buffer = xmlInput.getBytes();
		bout.write(buffer);
		byte[] b = bout.toByteArray();
		
		/*
		 * TODO
		 * retrieve url from configuration file
		 */
		String SOAPAction =
				"http://10.4.0.16:8080/services/BusinessManager_Reporting?wsdl";
		// Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length",
				String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", SOAPAction);
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		OutputStream out = httpConn.getOutputStream();
		//Write the content of the request to the outputstream of the HTTP Connection.
		out.write(b);
		out.close();
		//Ready with sending the request.

		//Read the response.
		InputStreamReader isr =
				new InputStreamReader(httpConn.getInputStream());
		BufferedReader in = new BufferedReader(isr);

		//Write the SOAP message response to a String.
		while ((responseString = in.readLine()) != null) {
			outputString = outputString + responseString;
		}
		//System.out.println("OutputString : "+outputString);
		//Parse the String output to a org.w3c.dom.Document and be able to reach every node with the org.w3c.dom API.
		Document document = parseXmlFile(outputString);
		NodeList nodeLst = document.getElementsByTagName("ns:getSLAResponse");
		String xmlSLA = nodeLst.item(0).getTextContent();
		
		String xmlSLAtest = "<slam:SLA xmlns:slam=\"http://www.slaatsoi.eu/slamodel\"><slam:Text/><slam:Properties><slam:Entry><slam:Key>ProviderUUid</slam:Key><slam:Value>1</slam:Value></slam:Entry></slam:Properties><slam:UUID>cbf1f8e2-4d90-46f4-be60-7a198f4def39</slam:UUID><slam:ModelVersion>sla_at_soi_sla_model_v1.0</slam:ModelVersion><slam:EffectiveFrom>2015-05-13T11:57:46.705Z</slam:EffectiveFrom><slam:EffectiveUntil>2017-05-13T11:57:46.705Z</slam:EffectiveUntil><slam:TemplateId>ASCETiC-SLaTemplate-Example-01</slam:TemplateId><slam:AgreedAt>2015-05-13T11:57:46.705Z</slam:AgreedAt><slam:Party><slam:Text/><slam:Properties><slam:Entry><slam:Key>http://www.slaatsoi.org/slamodel#gslam_epr</slam:Key><slam:Value>http://10.4.0.16:8080/services/asceticNegotiation?wsdl</slam:Value></slam:Entry></slam:Properties><slam:ID>AsceticProvider</slam:ID><slam:Role>http://www.slaatsoi.org/slamodel#provider</slam:Role></slam:Party><slam:Party><slam:Text/><slam:Properties><slam:Entry><slam:Key>http://www.slaatsoi.org/slamodel#gslam_epr</slam:Key><slam:Value>http://10.4.0.16:8080/services/asceticNegotiation?wsdl</slam:Value></slam:Entry></slam:Properties><slam:ID>333</slam:ID><slam:Role>http://www.slaatsoi.org/slamodel#customer</slam:Role></slam:Party><slam:InterfaceDeclr><slam:Text>Interface to specific OVF item</slam:Text><slam:Properties><slam:Entry><slam:Key>SLA-ProvidersList</slam:Key><slam:Value>{\"SLA-ProvidersList\":[{\"sla-id\":\"a34206b0-3200-4992-b5ac-e6fc5d735876\",\"provider-uuid\":\"1\"}]}</slam:Value></slam:Entry><slam:Entry><slam:Key>OVF_URL</slam:Key><slam:Value>ascetic-ovf-example.ovf</slam:Value></slam:Entry></slam:Properties><slam:ID>OVF-Item-ubu1</slam:ID><slam:ProviderRef>AsceticProvider</slam:ProviderRef><slam:Endpoint><slam:Text/><slam:Properties><slam:Entry><slam:Key>OVF_VirtualSystem_ID</slam:Key><slam:Value>ubu1</slam:Value></slam:Entry></slam:Properties><slam:ID>ubu1-VM-Type</slam:ID><slam:Location>VM-Manager ID</slam:Location><slam:Protocol>http://www.slaatsoi.org/slamodel#HTTP</slam:Protocol></slam:Endpoint><slam:Interface><slam:InterfaceResourceType><slam:Text/><slam:Properties/><slam:Name>OVFAppliance</slam:Name></slam:InterfaceResourceType></slam:Interface></slam:InterfaceDeclr><slam:AgreementTerm><slam:Text/><slam:Properties/><slam:ID>ubu1_Guarantees</slam:ID><slam:VariableDeclr><slam:Text/><slam:Properties/><slam:Var>VM_of_type_ubu1</slam:Var><slam:Expr><slam:ValueExpr><slam:FuncExpr><slam:Text/><slam:Properties/><slam:Operator>http://www.slaatsoi.org/coremodel#subset_of</slam:Operator><slam:Parameter><slam:ID>OVF-Item-ubu1</slam:ID></slam:Parameter></slam:FuncExpr></slam:ValueExpr></slam:Expr></slam:VariableDeclr><slam:Guaranteed><slam:Text/><slam:Properties/><slam:State><slam:ID>ubu1-vm_cores</slam:ID><slam:Priority xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/><slam:Constraint><slam:TypeConstraintExpr><slam:Value><slam:FuncExpr><slam:Text/><slam:Properties/><slam:Operator>http://www.slaatsoi.org/resources#vm_cores</slam:Operator><slam:Parameter><slam:ID>VM_of_type_ubu1</slam:ID></slam:Parameter></slam:FuncExpr></slam:Value><slam:Domain><slam:SimpleDomainExpr><slam:ComparisonOp>http://www.slaatsoi.org/coremodel#equals</slam:ComparisonOp><slam:Value><slam:CONST><slam:Value>1.0</slam:Value><slam:Datatype>http://www.w3.org/2001/XMLSchema#integer</slam:Datatype></slam:CONST></slam:Value></slam:SimpleDomainExpr></slam:Domain></slam:TypeConstraintExpr></slam:Constraint></slam:State></slam:Guaranteed><slam:Guaranteed><slam:Text/><slam:Properties/><slam:State><slam:ID>ubu1-disk_size</slam:ID><slam:Priority xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/><slam:Constraint><slam:TypeConstraintExpr><slam:Value><slam:FuncExpr><slam:Text/><slam:Properties/><slam:Operator>http://www.slaatsoi.org/resources#disk_size</slam:Operator><slam:Parameter><slam:ID>VM_of_type_ubu1</slam:ID></slam:Parameter></slam:FuncExpr></slam:Value><slam:Domain><slam:SimpleDomainExpr><slam:ComparisonOp>http://www.slaatsoi.org/coremodel#equals</slam:ComparisonOp><slam:Value><slam:CONST><slam:Value>51200.0</slam:Value><slam:Datatype>http://www.slaatsoi.org/coremodel/units#MB</slam:Datatype></slam:CONST></slam:Value></slam:SimpleDomainExpr></slam:Domain></slam:TypeConstraintExpr></slam:Constraint></slam:State></slam:Guaranteed><slam:Guaranteed><slam:Text/><slam:Properties/><slam:State><slam:ID>ubu1-memory</slam:ID><slam:Priority xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/><slam:Constraint><slam:TypeConstraintExpr><slam:Value><slam:FuncExpr><slam:Text/><slam:Properties/><slam:Operator>http://www.slaatsoi.org/resources#memory</slam:Operator><slam:Parameter><slam:ID>VM_of_type_ubu1</slam:ID></slam:Parameter></slam:FuncExpr></slam:Value><slam:Domain><slam:SimpleDomainExpr><slam:ComparisonOp>http://www.slaatsoi.org/coremodel#equals</slam:ComparisonOp><slam:Value><slam:CONST><slam:Value>256.0</slam:Value><slam:Datatype>http://www.slaatsoi.org/coremodel/units#MB</slam:Datatype></slam:CONST></slam:Value></slam:SimpleDomainExpr></slam:Domain></slam:TypeConstraintExpr></slam:Constraint></slam:State></slam:Guaranteed><slam:Guaranteed><slam:Text/><slam:Properties/><slam:State><slam:ID>Power_Usage_for_ubu1</slam:ID><slam:Priority xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/><slam:Constraint><slam:TypeConstraintExpr><slam:Value><slam:FuncExpr><slam:Text/><slam:Properties/><slam:Operator>http://www.slaatsoi.org/resources#power_usage_per_vm</slam:Operator><slam:Parameter><slam:ID>VM_of_type_ubu1</slam:ID></slam:Parameter></slam:FuncExpr></slam:Value><slam:Domain><slam:SimpleDomainExpr><slam:ComparisonOp>http://www.slaatsoi.org/coremodel#less_than_or_equals</slam:ComparisonOp><slam:Value><slam:CONST><slam:Value>10</slam:Value><slam:Datatype>http://www.slaatsoi.org/coremodel/units#W</slam:Datatype></slam:CONST></slam:Value></slam:SimpleDomainExpr></slam:Domain></slam:TypeConstraintExpr></slam:Constraint></slam:State></slam:Guaranteed></slam:AgreementTerm><slam:AgreementTerm><slam:Text/><slam:Properties/><slam:ID>Infrastructure_Price_Of_ubu1</slam:ID><slam:Guaranteed><slam:Text/><slam:Properties/><slam:Action><slam:ID>Price_Of_VirtualSystem_ubu1</slam:ID><slam:ActorRef>http://www.slaatsoi.org/slamodel#provider</slam:ActorRef><slam:Policy>http://www.slaatsoi.org/slamodel#mandatory</slam:Policy><slam:Precondition><slam:Text/><slam:Properties/><slam:Operator>http://www.slaatsoi.org/coremodel#invocation</slam:Operator><slam:Parameter><slam:ValueExpr><slam:ID>ubu1</slam:ID></slam:ValueExpr></slam:Parameter></slam:Precondition><slam:Postcondition><slam:Text/><slam:Properties/><slam:ProductOfferingPrice><slam:ID>Product_Offering_Price_Of_ubu1</slam:ID><slam:Name/><slam:Description/><slam:BillingFrequency>http://www.slaatsoi.org/business#http://www.slaatsoi.org/business#per_month</slam:BillingFrequency><slam:ValidFrom>2015-05-13T12:16:58.210Z</slam:ValidFrom><slam:ValidUntil>2016-05-13T12:16:58.211Z</slam:ValidUntil><slam:ComponentProdOfferingPrice><slam:ID>Price_OF_ubu1</slam:ID><slam:PriceType>http://www.slaatsoi.org/business#http://www.slaatsoi.org/business#per_hour</slam:PriceType><slam:Price><slam:Value>24.338490278911742</slam:Value><slam:Datatype>http://www.slaatsoi.org/coremodel/units#http://www.slaatsoi.org/coremodel/units#EUR</slam:Datatype></slam:Price><slam:Quantity><slam:Value>1</slam:Value><slam:Datatype>http://www.slaatsoi.org/coremodel/units#vm</slam:Datatype></slam:Quantity></slam:ComponentProdOfferingPrice></slam:ProductOfferingPrice></slam:Postcondition></slam:Action></slam:Guaranteed></slam:AgreementTerm></slam:SLA>";
		SLA sla = null;
		try {
			sla = (xmlSLA == null) ? null : slaTranslator.parseSla(xmlSLAtest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("XML SLA: "+xmlSLA);
		System.out.println("SLA recovered and parsed"+sla);
		
		
		//Write the SOAP message formatted to the console.
		//String formattedSOAPResponse = formatXML(outputString);
		//System.out.println(formattedSOAPResponse);
		return sla;
	}


	//format the XML in your String
	public String formatXML(String unformattedXml) {
		try {
			Document document = parseXmlFile(unformattedXml);
			OutputFormat format = new OutputFormat(document);
			format.setIndenting(true);
			format.setIndent(3);
			format.setOmitXMLDeclaration(true);
			Writer out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);
			return out.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Document parseXmlFile(String in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(in));
			return db.parse(is);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		GetSLAClient gsc = new GetSLAClient("http://10.4.0.16:8080/services/BusinessManager_Reporting?wsdl",null);
		try {
			gsc.getSLA();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}