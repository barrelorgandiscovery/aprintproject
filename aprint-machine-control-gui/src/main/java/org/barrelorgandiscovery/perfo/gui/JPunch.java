package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.perfo.PunchListener;
import org.barrelorgandiscovery.perfo.PunchProcess;

import com.jeta.forms.components.panel.FormPanel;

public class JPunch extends JPanel implements IPunchMachinePanelActivate {

  private static Logger logger = Logger.getLogger(JPunch.class);
  private PunchProcess punchProcess;
  private JSelectFiles selectFilesComponent;
  private Navigation navigation;

  public JPunch(PunchProcess punchProcess, JSelectFiles selectFilesComponent, Navigation navigation)
      throws Exception {
    super();

    this.punchProcess = punchProcess;
    this.selectFilesComponent = selectFilesComponent;
    this.navigation = navigation;
    initComponents();
  }

  private JLabel lblFilePos;
  private JLabel lblpunchlabel;
  private JLabel lblmachineState;
  private AbstractButton cancelButton;
  private AbstractButton btnpausetoggle;

  protected void initComponents() throws Exception {

    FormPanel p = new FormPanel(getClass().getResourceAsStream("punch.jfrm"));

    setLayout(new BorderLayout());
    add(p, BorderLayout.CENTER);

    lblFilePos = p.getLabel("filepos");
    lblpunchlabel = p.getLabel("punchlabel");
    lblmachineState = p.getLabel("machinestate");

    cancelButton = p.getButton("cancel");
    defineCancelButton();
    cancelButton.addActionListener(
        (e) -> {
          punchProcess.stop();
          navigation.navigateTo(this, PunchScreen.Hello);
        });

    btnpausetoggle = p.getButton("pausetoggle");
    btnpausetoggle.setIcon(new ImageIcon(getClass().getResource("player_pause.png")));
    btnpausetoggle.setText("Pause");
    btnpausetoggle.addActionListener(
        (e) -> {
          logger.debug("pause");
        });
  }

  private void defineCancelButton() {
    cancelButton.setText("Cancel");
    cancelButton.setIcon(new ImageIcon(getClass().getResource("cancel.png")));
  }

  private void setInformMessage(String message) {
    lblpunchlabel.setText(message);
    repaint();
  }

  private void setMachineStatus(String message) {
    lblmachineState.setText(message);
    repaint();
  }

  @Override
  public void activate() {
    // checking files
    setInformMessage("Verify punch files");
    defineCancelButton();
    try {
      punchProcess.startPunch(
          new PunchListener() {

            @Override
            public void startFile(File punchFile) {
              setInformMessage("Punching " + punchFile.getName());
            }

            @Override
            public void message(String message) {
              setMachineStatus(message);
            }

            @Override
            public void initializing(String status) {
              setMachineStatus(status);
            }

            @Override
            public void informCurrentPunchPosition(
                File currentFile, int currentPunchIndex, int totalPunchNumber) {
              
            	lblFilePos.setText(
                  "" + currentPunchIndex + "/" + totalPunchNumber + " " + currentFile.getName());
              repaint();
            }

            @Override
            public void finishedFile(File file) {}

            @Override
            public void allFilesFinished() {
              defineDoneButton();
            }
          },
          selectFilesComponent.getSelectedFiles());
    } catch (Exception ex) {
      logger.error("error in punch " + ex.getMessage(), ex);
    }
  }

  private void defineDoneButton() {
    cancelButton.setText("Done");
    cancelButton.setIcon(new ImageIcon(getClass().getResource("button_ok.png")));
  }
}
