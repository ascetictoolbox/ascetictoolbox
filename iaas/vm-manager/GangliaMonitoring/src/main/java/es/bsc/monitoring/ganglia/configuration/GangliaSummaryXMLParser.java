
package es.bsc.monitoring.ganglia.configuration;


import es.bsc.monitoring.ganglia.infrastructure.ClusterSummary;
import es.bsc.monitoring.ganglia.infrastructure.HostsSummary;
import es.bsc.monitoring.ganglia.infrastructure.Metric;
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
public class GangliaSummaryXMLParser extends DefaultHandler implements GangliaMetKeys {

    private List<ClusterSummary> grid;
    private ClusterSummary currentCluster;
    private List<HostsSummary> currentClusterHosts;
    private HostsSummary currentHost;
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
        if (name.equals(METRICS)) {
            this.currentMetric.setExtraData(extraData);
            this.currentHostMetrics.add(currentMetric);

        }  else if (name.equals(CLUSTER)) {
            this.currentCluster.setHosts(currentHost);
            this.currentCluster.setMetrics(currentHostMetrics);
            this.grid.add(currentCluster);
        }
    }

    /**
     * {@inheritDoc}
     * @throws org.xml.sax.SAXException
     */
    
    @Override
    public void startDocument() throws SAXException {
        this.grid = new ArrayList<ClusterSummary>();
    }

    /**
     * {@inheritDoc}
     * @param name
     * @param atts
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        if (name.equals(METRICS)) {
            this.currentMetric = new Metric(atts.getValue(NAME), atts.getValue(VAL), atts.getValue(TYPE),
                    atts.getValue(UNITS), atts.getValue(TN), atts.getValue(TMAX), atts.getValue(DMAX),
                    atts.getValue(SLOPE), atts.getValue(SOURCE));
            this.extraData = new HashMap<String, String>();

        } else if (name.equals(EXTRA_ELEMENT)) {
            this.extraData.put(atts.getValue(NAME), atts.getValue(VAL));

        } else if (name.equals(HOSTS)) {
            this.currentHost = new HostsSummary(atts.getValue(UP), atts.getValue(DOWN), atts.getValue(SOURCE_HOST));
            

        } else if (name.equals(CLUSTER)) {
            
            this.currentCluster = new ClusterSummary(atts.getValue(NAME), atts.getValue(LOCALTIME), atts.getValue(OWNER),
                    atts.getValue(LATLONG), atts.getValue(URL));
            this.currentHostMetrics = new ArrayList<Metric>();
            
        }       
    }

    /**
     * Get the configuration after parsing the XML Stream.
     *
     * @return a grid configuration
     */
    public List<ClusterSummary> getGridConfiguration() {
        return this.grid;
    }
}
