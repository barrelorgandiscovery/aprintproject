package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParameters;

public class JMainPanel extends JPanel {

  private JLabel connectedStatus = new JLabel();

  public JMainPanel() throws Exception {
	  initComponents();
  }

  protected void initComponents() throws Exception {
    setLayout(new BorderLayout());

    add(connectedStatus, BorderLayout.CENTER);
    
    GRBLMachine g = new GRBLMachine();
    GRBLMachineParameters p = new GRBLMachineParameters();
    p.setComPort("/dev/ttyUSB0");
    MachineControl m = g.open(p);

    m.setMachineControlListener(
        new MachineControlListener() {

          @Override
          public void statusChanged(MachineStatus status) {}

          @Override
          public void error(String message) {
            // TODO Auto-generated method stub

          }

          @Override
          public void currentMachinePosition(
              String status, double wx, double wy, double mx, double my) {
            try {
              SwingUtilities.invokeAndWait(
                  new Runnable() {
                    public void run() {

                      connectedStatus.setText(status);
                    };
                  });

            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });
  }
}
