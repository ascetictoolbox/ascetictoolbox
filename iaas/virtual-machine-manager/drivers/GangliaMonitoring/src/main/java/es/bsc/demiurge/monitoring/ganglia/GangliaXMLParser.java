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

/**
 *
 * @author mcanuto
 */
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * SAX parser to create a grid configuration from a XML stream.
 * XML schema corresponds to a GMetad XML output.
 * 
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 */
class GangliaXMLParser extends DefaultHandler implements GangliaMetKeys {

    private List<Cluster> grid;
    private Cluster currentCluster;
    private List<Host> currentClusterHosts;
    private Host currentHost;
    private List<Metric> currentHostMetrics;
    private Metric currentMetric;
    private HashMap<String, String> extraData;

    /**
     * {@inheritDoc}
     * @param name
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (name.equals(METRIC)) {
            this.currentMetric.setExtraData(extraData);
            this.currentHostMetrics.add(currentMetric);

        } else if (name.equals(HOST)) {
            this.currentHost.setMetrics(currentHostMetrics);
            this.currentClusterHosts.add(currentHost);

        } else if (name.equals(CLUSTER)) {
            this.currentCluster.setHosts(currentClusterHosts);
            this.grid.add(currentCluster);
        }
    }

    /**
     * {@inheritDoc}
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startDocument() throws SAXException {
        this.grid = new ArrayList<Cluster>();
    }

    /**
     * {@inheritDoc}
     * @param name
     * @param atts
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        if (name.equals(METRIC)) {
            this.currentMetric = new Metric(atts.getValue(NAME), atts.getValue(VAL), atts.getValue(TYPE),
                    atts.getValue(UNITS), atts.getValue(TN), atts.getValue(TMAX), atts.getValue(DMAX),
                    atts.getValue(SLOPE), atts.getValue(SOURCE));
            this.extraData = new HashMap<String, String>();

        } else if (name.equals(EXTRA_ELEMENT)) {
            this.extraData.put(atts.getValue(NAME), atts.getValue(VAL));

        } else if (name.equals(HOST)) {
            this.currentHost = new Host(atts.getValue(NAME), atts.getValue(IP), atts.getValue(REPORTED),
                    atts.getValue(TN), atts.getValue(TMAX), atts.getValue(DMAX), atts.getValue(LOCATION),
                    atts.getValue(GMOND_STARTED));
            this.currentHostMetrics = new ArrayList<Metric>();

        } else if (name.equals(CLUSTER)) {
            this.currentCluster = new Cluster(atts.getValue(NAME), atts.getValue(LOCALTIME), atts.getValue(OWNER),
                    atts.getValue(LATLONG), atts.getValue(URL));
            this.currentClusterHosts = new ArrayList<Host>();
        }       
    }

    /**
     * Get the configuration after parsing the XML Stream.
     *
     * @return a grid configuration
     */
    public List<Cluster> getGridConfiguration() {
        return this.grid;
    }
}
