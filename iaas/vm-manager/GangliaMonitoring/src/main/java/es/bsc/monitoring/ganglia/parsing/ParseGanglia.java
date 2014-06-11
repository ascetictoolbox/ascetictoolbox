package es.bsc.monitoring.ganglia.parsing;

import es.bsc.monitoring.exceptions.MonitoringException;
import es.bsc.monitoring.ganglia.configuration.GangliaSummaryXMLParser;
import es.bsc.monitoring.ganglia.configuration.GangliaXMLParser;
import es.bsc.monitoring.ganglia.infrastructure.Cluster;
import es.bsc.monitoring.ganglia.infrastructure.ClusterSummary;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 *
 */
public class ParseGanglia {

    private String gangliaCollectorIP;
    private Integer gangliaPort;
    private Integer gangliaPortQuery;

    // Class constructor
    public ParseGanglia(String gangliaCollectorIP, Integer gangliaPort, Integer gangliaPortQuery) {

        this.gangliaCollectorIP = gangliaCollectorIP;
        this.gangliaPort = gangliaPort;
        this.gangliaPortQuery = gangliaPortQuery;

    }

    public String getGangliaXml() throws MonitoringException {

        StringBuilder stringBuffer = new StringBuilder();
        try {
            Socket s = new Socket(gangliaCollectorIP, gangliaPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                stringBuffer.append(line);
                line = reader.readLine();

            }
            reader.close();

            // System.out.println("StringBuffer: " + stringBuffer);
        } catch (IOException e) {
            throw new MonitoringException("Unable to read the sample monitoring report from the file: "
                    + e.getMessage());
        }

        return stringBuffer.toString().trim();

    }

    public List<Cluster> parseGangliaXml(String buffer) throws MonitoringException {

        List<Cluster> gridConfiguration;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser parser;
        GangliaXMLParser gangliaXMLParser;
        try {

            parser = factory.newSAXParser();
            gangliaXMLParser = new GangliaXMLParser();
            parser.parse(new InputSource(new StringReader(buffer)), gangliaXMLParser);
            gridConfiguration = gangliaXMLParser.getGridConfiguration();

        } catch (ParserConfigurationException e) {
            throw new MonitoringException("Error while parsing: " + e.getMessage());
        } catch (SAXException e) {
            throw new MonitoringException("Error while parsing the XML: " + e.getMessage());
        } catch (IOException e) {
            throw new MonitoringException("I/O error: " + e.getMessage());
        }

        return gridConfiguration;
    }

    public ClusterSummary parseGangliaSummaryXml(String buffer) throws MonitoringException {

        List<ClusterSummary> gridConfiguration;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser parser;
        GangliaSummaryXMLParser gangliaXMLParser;
        try {

            parser = factory.newSAXParser();
            gangliaXMLParser = new GangliaSummaryXMLParser();
            parser.parse(new InputSource(new StringReader(buffer)), gangliaXMLParser);
            gridConfiguration = gangliaXMLParser.getGridConfiguration();

        } catch (ParserConfigurationException e) {
            throw new MonitoringException("Error while parsing: " + e.getMessage());
        } catch (SAXException e) {
            throw new MonitoringException("Error while parsing the XML: " + e.getMessage());
        } catch (IOException e) {
            throw new MonitoringException("I/O error: " + e.getMessage());
        }

        if (gridConfiguration.get(0) != null) {
            return gridConfiguration.get(0);
        } else {
            return null;
        }
    }

    public String queryGanglia(String query) throws MonitoringException {

        StringBuilder stringBuffer = new StringBuilder();
        try {
            Socket s = new Socket(gangliaCollectorIP, gangliaPortQuery);
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            out.println(query);

            String line = reader.readLine();
            while (line != null) {
                stringBuffer.append(line);
                line = reader.readLine();

            }
            out.close();
            reader.close();

            return stringBuffer.toString().trim();

        } catch (IOException e) {
            throw new MonitoringException("Socket error: "
                    + e.getMessage());
        }

    }
}
