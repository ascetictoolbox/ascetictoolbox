/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.monitoring.ganglia;

import es.bsc.demiurge.core.monitoring.exceptions.MonitoringException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 *
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 *
 */
class ParseGanglia {

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
