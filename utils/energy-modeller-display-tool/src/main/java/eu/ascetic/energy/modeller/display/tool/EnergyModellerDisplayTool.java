/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.energy.modeller.display.tool;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jfree.data.time.TimeSeries;

/**
 * This displays the energy usage of VMs and their hosts
 * @author Richard Kavanagh
 */
public class EnergyModellerDisplayTool extends JFrame {

    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 1733713840007288665L;
    private static EnergyModellerDisplayTool displayTool;
    private final HostDataSource dataSource = new ZabbixDirectDbDataSourceAdaptor();
    private final DataCollector collector = new DataCollector(dataSource, new DefaultDatabaseConnector(), true);
    /**
     *
     */
    private final JTabbedPane tabbedPane;
    /**
     *
     */
    private final List<JPanel> panels = new ArrayList<>();

    /**
     *
     */
    private final ConcurrentHashMap<String, TimeSeries> data = new ConcurrentHashMap<>();

    /**
     *
     * @throws IOException If the meter is not available.
     */
    public EnergyModellerDisplayTool() throws IOException {
        this.tabbedPane = new JTabbedPane();
        this.addContent();
        Thread thread = new Thread(collector);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * @throws IOException If the meter is not available.
     */
    protected final void addContent() throws IOException {

        collector.registerListener(new DataAvailableListener() {
            @Override
            public void processDataAvailable(final HashMap<String, TimeSeries> dataset) {
                data.putAll(dataset);
            }
        });

        String path = System.getProperty("export.file.path");

        if (path != null && !path.isEmpty()) {
            final FileOutputStream fos = new FileOutputStream(new File(path));

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        TimeSeriesLineChart graph;
        List<Host> hosts = dataSource.getHostList();
        Collections.sort(hosts);
        ArrayList<String> hostsStr = new ArrayList<>();
        for (Host host : hosts) {
            hostsStr.add(host.getHostName());
        }
        graph = new TimeSeriesLineChart(data, hostsStr);
        for (Host host : hosts) {
            graph.addHost(host.getHostName());
        }
        this.addPanel(graph);

        for (Host host : hosts) {
            graph = new TimeSeriesLineChart(data, host.getHostName());
            this.addPanel(graph);
        }

        this.tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        getContentPane().add(this.tabbedPane);

        for (JPanel panel : this.getPanels()) {
            panel.setVisible(true);
            this.tabbedPane.add(panel, panel.toString());
        }
        setPreferredSize(new Dimension(1024, 768));

        this.pack();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        setVisible(true);
    }

    /**
     * Add a panel to this {@link JFrame}.
     *
     * @param panel The panel to be added. Might not be <code>null</code>.
     */
    public void addPanel(JPanel panel) {
        this.panels.add(panel);
    }

    /**
     * @return A read-only {@link List} with the panels.
     */
    public List<JPanel> getPanels() {
        return Collections.unmodifiableList(panels);
    }

    /**
     *
     * @param args The reference to the arguments.
     * @throws IOException If the power meter is not connected.
     */
    public static void main(final String[] args) throws IOException {
        displayTool = new EnergyModellerDisplayTool();
        HashSet<String> argsSet = new HashSet<>();
        argsSet.addAll(Arrays.asList(args));
        if (argsSet.contains("Ignore-Idle-Energy")) {
            displayTool.collector.setConsiderIdleEnergy(false);
        } 

    }
}
