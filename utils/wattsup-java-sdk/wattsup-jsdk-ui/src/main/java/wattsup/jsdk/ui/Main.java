/**
 * Copyright (C) 2013 Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package wattsup.jsdk.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.event.WattsUpDataAvailableEvent;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.listener.WattsUpDataAvailableListener;
import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.meter.WattsUp;
import wattsup.jsdk.ui.chart.line.WattsLineChart;

public class Main extends JFrame {

    /**
     * Serial code version
     * <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 1733713840007288665L;
    /**
     *
     */
    private final JTabbedPane tabbedPane_;
    /**
     *
     */
    private final List<JPanel> panels_ = new ArrayList<JPanel>();
    /**
     *
     */
    private final WattsUp meter_;
    /**
     *
     */
    private final List<WattsUpPacket> data_ = new ArrayList<>();

    /**
     *
     * @param config The configuration for the meter.
     * @throws IOException If the meter is not available.
     */
    public Main(WattsUpConfig config) throws IOException {
        meter_ = new WattsUp(config);
        this.tabbedPane_ = new JTabbedPane();
        this.addContent();
    }

    /**
     * @throws IOException If the meter is not available.
     */
    protected void addContent() throws IOException {

        meter_.registerListener(new WattsUpDataAvailableListener() {
            @Override
            public void processDataAvailable(final WattsUpDataAvailableEvent event) {
                data_.clear();
                data_.addAll(Arrays.asList(event.getValue()));
            }
        });

        String path = System.getProperty("export.file.path");

        if (path != null && !path.isEmpty()) {
            final FileOutputStream fos = new FileOutputStream(new File(path));
//            meter_.registerListener(new ExportCsvListener(fos));

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        fos.close();
                    } catch (IOException ignore) {
                        ignore.printStackTrace();
                    }
                }
            });
        }

        meter_.registerListener(new WattsUpDisconnectListener() {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event) {
                System.exit(0);
            }
        });

        this.addPanel(new WattsLineChart(data_));

        this.tabbedPane_.setTabPlacement(JTabbedPane.BOTTOM);
        getContentPane().add(this.tabbedPane_);

        for (JPanel panel : this.getPanels()) {
            panel.setVisible(true);
            this.tabbedPane_.add(panel, panel.getClass().getSimpleName());
        }
        setPreferredSize(new Dimension(1024, 768));

        this.pack();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        meter_.connect();
        meter_.setLoggingModeSerial(1);

        setVisible(true);
    }

    /**
     * Add a panel to this {@link JFrame}.
     *
     * @param panel The panel to be added. Might not be <code>null</code>.
     */
    public void addPanel(JPanel panel) {
        this.panels_.add(panel);
    }

    /**
     * @return A read-only {@link List} with the panels.
     */
    public List<JPanel> getPanels() {
        return Collections.unmodifiableList(panels_);
    }

    /**
     * Creates an {@link WattsUp} to monitor the power consumption.
     *
     * @param args The reference to the arguments.
     * @throws IOException If the power meter is not connected.
     */
    public static void main(final String[] args) throws IOException {
        String comPort = "COM9";
        if (args.length != 0) {
            comPort = args[0];
//            throw new IllegalArgumentException("The path to the serial port is required!");
        }
        new Main(new WattsUpConfig().withPort(comPort).withExternalLoggingInterval(5).scheduleDuration(-1l));
//        new Main(new WattsUpConfig().withPort(comPort).scheduleDuration(Integer.valueOf(System.getProperty("measure.duration", "0"))));
    }
}
