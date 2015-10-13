/**
 * Copyright 2015 University of Leeds
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
package eu.ascetic.utils.paassameventinjectiontool;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * The aim of this tool is to manually inject SLA violation events in the queue
 * in order to create adaptation events.
 * @author Richard Kavanagh
 */
public class PaaSSamEventSubmitterTool extends javax.swing.JFrame {

    /**
     * Creates new form PaaSSamEventSubmitterTool
     */
    public PaaSSamEventSubmitterTool() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        south = new javax.swing.JPanel();
        buttonOnce = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        messageCount = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        arrivalRate = new javax.swing.JSpinner();
        buttonSubmitEvents = new javax.swing.JButton();
        north = new javax.swing.JPanel();
        labelApplicationId = new javax.swing.JLabel();
        appId = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        lableDeploymentId = new javax.swing.JLabel();
        deploymentId = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        labelSLAUuid = new javax.swing.JLabel();
        slauuid = new javax.swing.JTextField();
        centre = new javax.swing.JPanel();
        labelMessageQueue = new javax.swing.JLabel();
        messageQueue = new javax.swing.JTextField();
        labelAgreementTerm = new javax.swing.JLabel();
        agreementTerm = new javax.swing.JComboBox();
        labelActualValue = new javax.swing.JLabel();
        actualValue = new javax.swing.JSpinner();
        labelOperator = new javax.swing.JLabel();
        operator = new javax.swing.JComboBox();
        labelGuaranteedValue = new javax.swing.JLabel();
        guaranteedValue = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        buttonOnce.setText("Submit One Event");
        buttonOnce.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOnceActionPerformed(evt);
            }
        });
        south.add(buttonOnce);
        south.add(jSeparator3);

        jLabel1.setText("Count");
        south.add(jLabel1);

        messageCount.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10), Integer.valueOf(1), null, Integer.valueOf(1)));
        messageCount.setPreferredSize(new java.awt.Dimension(50, 30));
        south.add(messageCount);

        jLabel7.setText("Arrival Rate (s)");
        south.add(jLabel7);

        arrivalRate.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        arrivalRate.setPreferredSize(new java.awt.Dimension(50, 30));
        south.add(arrivalRate);

        buttonSubmitEvents.setText("Submit Events");
        buttonSubmitEvents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSubmitEventsActionPerformed(evt);
            }
        });
        south.add(buttonSubmitEvents);

        getContentPane().add(south, java.awt.BorderLayout.SOUTH);

        labelApplicationId.setText("Application Id:");
        north.add(labelApplicationId);

        appId.setText("davidgpTestApp");
        appId.setToolTipText("");
        appId.setPreferredSize(new java.awt.Dimension(100, 30));
        north.add(appId);
        north.add(jSeparator1);

        lableDeploymentId.setText("Deployment Id: ");
        north.add(lableDeploymentId);

        deploymentId.setText("453");
        deploymentId.setPreferredSize(new java.awt.Dimension(100, 30));
        north.add(deploymentId);
        north.add(jSeparator2);

        labelSLAUuid.setText("SLA UUID: ");
        north.add(labelSLAUuid);

        slauuid.setText("slauuid");
        slauuid.setPreferredSize(new java.awt.Dimension(100, 30));
        north.add(slauuid);

        getContentPane().add(north, java.awt.BorderLayout.NORTH);

        centre.setLayout(new java.awt.GridLayout(5, 2));

        labelMessageQueue.setText("Message Queue:");
        centre.add(labelMessageQueue);

        messageQueue.setText("paas-slam.monitoring.test");
        centre.add(messageQueue);

        labelAgreementTerm.setText("Agreement Term:");
        centre.add(labelAgreementTerm);

        agreementTerm.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "power_usage_per_app", "energy_usage_per_app" }));
        centre.add(agreementTerm);

        labelActualValue.setText("Actual Value:");
        centre.add(labelActualValue);

        actualValue.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(16.0d), Double.valueOf(1.0d), null, Double.valueOf(1.0d)));
        actualValue.setToolTipText("The actual value seen during the SLA breach.");
        centre.add(actualValue);
        actualValue.getAccessibleContext().setAccessibleDescription("");

        labelOperator.setText("Operator:");
        centre.add(labelOperator);

        operator.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "less_than_or_equals", "less_than", "equals", "greater_than", "greater_than_or_equals", " " }));
        operator.setToolTipText("The gurantee operator. i.e. \"less than \" guaranteed value X");
        centre.add(operator);

        labelGuaranteedValue.setText("Guaranteed Value:");
        centre.add(labelGuaranteedValue);

        guaranteedValue.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(10.0d), Double.valueOf(1.0d), null, Double.valueOf(1.0d)));
        guaranteedValue.setToolTipText("The value to guarantee");
        centre.add(guaranteedValue);

        getContentPane().add(centre, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonOnceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOnceActionPerformed
        try {
            SLAManagerMessageGenerator generator = new SLAManagerMessageGenerator("guest", "guest", "192.168.3.16:5673", messageQueue.getText());
            generator.setAppId(appId.getText());
            generator.setDeploymentId(deploymentId.getText());
            generator.setSlaUuid(slauuid.getText());
            generator.setArrivalInterval((int) arrivalRate.getValue());            
            generator.createAndSendViolationMessage((double) actualValue.getValue(),
                    (double) guaranteedValue.getValue(),
                    agreementTerm.getItemAt(agreementTerm.getSelectedIndex()).toString(),
                    operator.getItemAt(operator.getSelectedIndex()).toString());
        } catch (JMSException ex) {
            Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonOnceActionPerformed

    private void buttonSubmitEventsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSubmitEventsActionPerformed
        try {
            SLAManagerMessageGenerator generator = new SLAManagerMessageGenerator("guest", "guest", "192.168.3.16:5673", messageQueue.getText());
            generator.setAppId(appId.getText());
            generator.setDeploymentId(deploymentId.getText());
            generator.setSlaUuid(slauuid.getText());
            generator.setArrivalInterval((int) arrivalRate.getValue());
            for (int i = 0; i < ((int) messageCount.getValue()); i++) {
                generator.createAndSendViolationMessage((double) actualValue.getValue(),
                        (double) guaranteedValue.getValue(),
                        agreementTerm.getItemAt(agreementTerm.getSelectedIndex()).toString(),
                        operator.getItemAt(operator.getSelectedIndex()).toString());
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis((int) arrivalRate.getValue()));
        } catch (JMSException ex) {
            Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonSubmitEventsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PaaSSamEventSubmitterTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PaaSSamEventSubmitterTool().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner actualValue;
    private javax.swing.JComboBox agreementTerm;
    private javax.swing.JTextField appId;
    private javax.swing.JSpinner arrivalRate;
    private javax.swing.JToggleButton buttonOnce;
    private javax.swing.JButton buttonSubmitEvents;
    private javax.swing.JPanel centre;
    private javax.swing.JTextField deploymentId;
    private javax.swing.JSpinner guaranteedValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelActualValue;
    private javax.swing.JLabel labelAgreementTerm;
    private javax.swing.JLabel labelApplicationId;
    private javax.swing.JLabel labelGuaranteedValue;
    private javax.swing.JLabel labelMessageQueue;
    private javax.swing.JLabel labelOperator;
    private javax.swing.JLabel labelSLAUuid;
    private javax.swing.JLabel lableDeploymentId;
    private javax.swing.JSpinner messageCount;
    private javax.swing.JTextField messageQueue;
    private javax.swing.JPanel north;
    private javax.swing.JComboBox operator;
    private javax.swing.JTextField slauuid;
    private javax.swing.JPanel south;
    // End of variables declaration//GEN-END:variables
}
