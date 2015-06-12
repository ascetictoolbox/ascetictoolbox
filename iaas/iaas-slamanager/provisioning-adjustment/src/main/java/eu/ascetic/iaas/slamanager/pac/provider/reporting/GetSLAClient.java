package eu.ascetic.iaas.slamanager.pac.provider.reporting;


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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slasoi.slamodel.core.CompoundDomainExpr;
import org.slasoi.slamodel.core.ConstraintExpr;
import org.slasoi.slamodel.core.DomainExpr;
import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.Expr;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.ValueExpr;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Customisable;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.VariableDeclr;
import org.slasoi.slamodel.sla.Guaranteed.State;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.ascetic.iaas.slamanager.pac.provider.translation.MeasurableAgreementTerm;
import eu.ascetic.iaas.slamanager.pac.provider.translation.SlaTranslator;
import eu.ascetic.iaas.slamanager.pac.provider.translation.SlaTranslatorImpl;

public class GetSLAClient {

	private SlaTranslator slaTranslator = new SlaTranslatorImpl();
	private String slaId;
	private String wsURL;
	private LinkedHashMap<String, DomainExpr> variablesAt;
	private HashMap<String, String> variablesVs;
	private List<MeasurableAgreementTerm> guarantees;
	
	private static Logger logger = Logger.getLogger(GetSLAClient.class.getName());
	
	
	public GetSLAClient(String wsURL, String slaId) {
		super();
		this.slaId = slaId;
		this.wsURL = wsURL;
		variablesAt = new LinkedHashMap<String, DomainExpr>();
		variablesVs = new HashMap<String, String>();
		guarantees = new ArrayList<MeasurableAgreementTerm>();
	}
	
	
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
		String SOAPAction =
				wsURL;
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
		
		SLA sla = null;
		try {
			sla = (xmlSLA == null) ? null : slaTranslator.parseSla(xmlSLA);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("SLA recovered and parsed"+sla);
		
		
		//Write the SOAP message formatted to the console.
		//String formattedSOAPResponse = formatXML(outputString);
		//System.out.println(formattedSOAPResponse);
		return sla;
	}

	
	
	public List<MeasurableAgreementTerm> getMeasurableTerms(SLA sla) {
		guarantees = new ArrayList<MeasurableAgreementTerm>();
		if (sla!=null) {
			AgreementTerm[] terms = sla.getAgreementTerms();
			if (terms!=null) {
				for (AgreementTerm term:terms) {
					parseVariables(term);


					if (term.getGuarantees()!=null) {
						for (Guaranteed guarantee : term.getGuarantees()) {

							if (guarantee instanceof Guaranteed.State) {

								ConstraintExpr ce = ((State) guarantee).getState();
								if (!(ce instanceof TypeConstraintExpr))
									continue;

								TypeConstraintExpr tce = (TypeConstraintExpr) ce;
								ValueExpr expr = tce.getValue();

								String ssTermName = null;
								String vsName = null;
								if (expr instanceof FunctionalExpr) {
									ssTermName = ((FunctionalExpr)expr).getOperator().getValue();
									if (ssTermName == null)
										continue;
									ValueExpr[] parameters = ((FunctionalExpr)expr).getParameters();
									if (parameters == null || parameters.length == 0)
										continue;
									vsName = parameters[0].toString();
								}

								String termName = ssTermName.substring(ssTermName.indexOf('#') + 1);



								DomainExpr de = tce.getDomain();
								if (de instanceof SimpleDomainExpr)
									parseSimpleDomainExpr(termName, (SimpleDomainExpr)de);
								if (de instanceof CompoundDomainExpr)
									parseCompoundDomainExpr(termName, (CompoundDomainExpr)de);

							} 
						}
					}

				}
			}
		}
		return guarantees;
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
	
	
	public void parseSimpleDomainExpr(String termName, SimpleDomainExpr sde) {
		String operator = sde.getComparisonOp().toString();
		ValueExpr ve = sde.getValue();
		if (ve instanceof CONST) {
			String value = ((CONST)ve).getValue();
			String unit = ((CONST)ve).getDatatype().toString();
			MeasurableAgreementTerm a = new MeasurableAgreementTerm(termName, unit, value, operator);
			System.out.println("Measurable term "+a);
			guarantees.add(a);
		} else if (ve instanceof ID) {
			DomainExpr cde = variablesAt.get(((ID)ve).getValue());
			if (cde instanceof SimpleDomainExpr) {
				parseSimpleDomainExpr( termName, (SimpleDomainExpr)cde);
			}
			if (cde instanceof CompoundDomainExpr) {
				parseCompoundDomainExpr(termName, (CompoundDomainExpr)cde);
			}
		}
	}

	
	public void parseCompoundDomainExpr(String termName, CompoundDomainExpr cde) {
		if (null == cde)
			return;
		for (DomainExpr de : cde.getSubExpressions()) {
			if (de instanceof SimpleDomainExpr)
				parseSimpleDomainExpr(termName, (SimpleDomainExpr) de);
		}
	}
	
	public void parseVariables(AgreementTerm term) {
		VariableDeclr[] vdecs = term.getVariableDeclrs();
		String ovfId = null;
		if (null == vdecs)
			return;
		for (VariableDeclr v : vdecs) {
			if (v instanceof Customisable) {
				Customisable c = (Customisable) v;
				ID var = c.getVar();
				DomainExpr expr = (DomainExpr) c.getExpr();
				variablesAt.put(var.getValue(), expr);
			} 
			else {
				Expr expr = (Expr) v.getExpr();
				if (expr instanceof FunctionalExpr) {
					FunctionalExpr fu = (FunctionalExpr) expr;
					ValueExpr ve = fu.getParameters()[0];
					ovfId = ve.toString();
					variablesVs.put(v.getVar().getValue(), ovfId);
				}
			}
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