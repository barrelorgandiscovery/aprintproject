package org.barrelorgandiscovery.perfo.gui.old;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;

public class JMainPanel extends JPanel {

  private JLabel connectedStatus = new JLabel();

  public JMainPanel() throws Exception {
    initComponents();
  }

  protected void initComponents() throws Exception {
    setLayout(new BorderLayout());

    add(connectedStatus, BorderLayout.SOUTH);

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

                      connectedStatus.setText(status + "-" + "(" + wx + "," + wy + ")");
                    };
                  });

            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });

    JButton btn = new JButton("move");
    add(btn, BorderLayout.EAST);
    btn.addActionListener(
        (e) -> {
          try {
            m.sendCommand(
                new DisplacementCommand(Math.random() * 100 + 50, Math.random() * 100 + 50));
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        });
    
  }
}