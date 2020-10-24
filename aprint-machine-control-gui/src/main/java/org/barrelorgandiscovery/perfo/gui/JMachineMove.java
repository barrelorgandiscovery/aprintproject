package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControl;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineControlListener;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.MachineStatus;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLPunchMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLPunchMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.perfo.Config;
import org.barrelorgandiscovery.perfo.ConfigFactory;

import com.jeta.forms.components.panel.FormPanel;

import jssc.SerialPortList;

/** @author pfreydiere */
public class JMachineMove extends JPanel implements IPunchMachinePanelActivate {

  private static Logger logger = Logger.getLogger(JMachineMove.class);

  private Config currentConfig;
  private HashMap<JToggleButton, Double> toggleStepButtons;
  private HashMap<AbstractButton, Supplier<DisplacementCommand>> buttonActions;
  private JToggleButton defaulttoggle;

  // consigne
  private double currentX;
  private double currentY;

  private Navigation navigation;

  public JMachineMove(Config config, Navigation navigation) throws Exception {
    this.currentConfig = config;
    this.navigation = navigation;
    initComponents();
  }

  protected void initComponents() throws Exception {

    FormPanel fp = new FormPanel(getClass().getResourceAsStream("movemachine.jfrm"));
    backbtn = fp.getButton("back");
    backbtn.setText("Back");
    backbtn.setIcon(new ImageIcon(getClass().getResource("back.png")));
    backbtn.addActionListener(
        (e) -> {
          back();
        });

    homingbtn = fp.getButton("homingbtn");
    homingbtn.setText("Homing");
    homingbtn.setIcon(new ImageIcon(getClass().getResource("gohome.png")));
    homingbtn.addActionListener(
        (e) -> {
          try {
            homing();
          } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
          }
        });

    buttonActions = new HashMap<>();

    AbstractButton upbtn = fp.getButton("up");
    upbtn.setText("");
    upbtn.setIcon(new ImageIcon(getClass().getResource("1uparrow.png")));
    buttonActions.put(
        upbtn,
        () -> {
          return new DisplacementCommand(currentX - getStepToggleValue(), currentY);
        });
    upbtn.addActionListener((e) -> moveFromButton(e));

    AbstractButton downbtn = fp.getButton("down");
    downbtn.setText("");
    downbtn.setIcon(new ImageIcon(getClass().getResource("1downarrow.png")));
    buttonActions.put(
        downbtn,
        () -> {
          return new DisplacementCommand(currentX + getStepToggleValue(), currentY);
        });
    downbtn.addActionListener((e) -> moveFromButton(e));

    AbstractButton leftbtn = fp.getButton("left");
    leftbtn.setText("");
    leftbtn.setIcon(new ImageIcon(getClass().getResource("1leftarrow.png")));
    buttonActions.put(
        leftbtn,
        () -> {
          return new DisplacementCommand(currentX, currentY - getStepToggleValue());
        });
    leftbtn.addActionListener((e) -> moveFromButton(e));

    AbstractButton rightbtn = fp.getButton("right");
    rightbtn.setText("");
    rightbtn.setIcon(new ImageIcon(getClass().getResource("1rightarrow.png")));
    buttonActions.put(
        rightbtn,
        () -> {
          return new DisplacementCommand(currentX, currentY + getStepToggleValue());
        });
    rightbtn.addActionListener((e) -> moveFromButton(e));

    toggleStepButtons = new HashMap<>();

    addStepToggleButton((JToggleButton) fp.getButton("01move"), 0.1d);
    defaulttoggle = (JToggleButton) fp.getButton("1move");
    addStepToggleButton(defaulttoggle, 1.0d);
    addStepToggleButton((JToggleButton) fp.getButton("10move"), 10.0d);

    defaulttoggle.setSelected(true);
    /// add to layout

    poslabel = fp.getLabel("poslbl");

    setLayout(new BorderLayout());
    add(fp, BorderLayout.CENTER);

    checkState();
  }

  private void addStepToggleButton(JToggleButton tb, double step) {
    assert tb != null;
    toggleStepButtons.put(tb, step);
    tb.setText("" + step);
    tb.setSelected(false);
    tb.addActionListener(
        (e) -> {
          toggleStepButtons
              .entrySet()
              .forEach(
                  (t) -> {
                    t.getKey().setSelected(false);
                  });
          tb.setSelected(true);
          defaulttoggle = tb;
        });
  }

  ///////////////////////////////////////////////
  // machine device state

  MachineControl machineControl = null;

  private AbstractButton homingbtn;

  private AbstractButton backbtn;

  private JLabel poslabel;

  public void activate() {

    if (isActivate()) {
      deactivate();
    }

    GRBLPunchMachine machine = new GRBLPunchMachine();
    GRBLPunchMachineParameters params = new GRBLPunchMachineParameters();
    logger.debug("existing com :");
    logger.debug(Arrays.asList(SerialPortList.getPortNames()));
    params.setComPort(currentConfig.usbPort);

    poslabel.setText("Opening Machine ...");

    try {
      MachineControl open = machine.open(params);
      open.setMachineControlListener(
          new MachineControlListener() {

            @Override
            public void statusChanged(MachineStatus status) {
              try {
                SwingUtilities.invokeLater(
                    () -> {
                      logger.debug("Machine Status ..." + status);
                    });

              } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
              }
            }

            @Override
            public void error(String message) {
              try {
                SwingUtilities.invokeLater(
                    () -> {
                      poslabel.setText("Error ..." + message);
                    });

              } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
              }
            }

            @Override
            public void currentMachinePosition(
                String status, double mx, double my) {
              try {
                SwingUtilities.invokeLater(
                    () -> {
                      poslabel.setText("" + status + ":" + mx + ";" + my);
                      currentX = my;
                      currentY = mx;
                    });

              } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
              }
            }
            
            @Override
            public void rawElementReceived(String commandReceived) {
            	
            }
            
            @Override
            public void rawElementSent(String commandSent) {
            	
            }
            @Override
            public void informationReceived(String commands) {
            	
            }
          });

      this.machineControl = open;

      poslabel.setText("Wait for Initializing .. ");

      repaint();

      Thread.sleep(4000);

      // send homing

      poslabel.setText("initializing done ... ");

      repaint();

    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  public void deactivate() {
    try {
      machineControl.close();
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    machineControl = null;
    poslabel.setText("Machine disconnected ...");
  }

  private boolean isActivate() {
    return machineControl != null;
  }

  private double getStepToggleValue() {
    return toggleStepButtons.get(defaulttoggle);
  }

  private void moveFromButton(ActionEvent e) {
    Object s = e.getSource();
    if (s == null || !(s instanceof AbstractButton)) {
      logger.debug("no source to apply button");
      return;
    }
    Supplier<DisplacementCommand> f = buttonActions.get((AbstractButton) s);
    if (f != null) {
      DisplacementCommand d = f.get();
      if (d != null) {
        try {
          logger.debug("send command " + d);
          machineControl.sendCommand(d);
        } catch (Exception ex) {
          logger.error(ex.getMessage(), ex);
        }
      }
    }
  }

  private void back() {
    deactivate();
    checkState();
    navigation.navigateTo(this, PunchScreen.Parameters);
  }

  private void homing() throws Exception {
    currentX = 0.0;
    currentY = 0.0;
    activate();

    if (machineControl != null) {
      HomingCommand homing = new HomingCommand();
      machineControl.sendCommand(homing);
      machineControl.flushCommands();
    }

    checkState();
  }

  private void setEnable(Component c, boolean enableState) {

    logger.debug("set enabled :" + c + " " + enableState);
    if (c == null) {
      return;
    }
    if (c == homingbtn || c == backbtn) {
      return;
    }

    if (c instanceof Container) {
      Container p = (Container) c;
      Component[] comp = p.getComponents();
      for (Component sub : comp) {
        setEnable(sub, enableState);
      }
    }
    c.setEnabled(enableState);
  }

  private void checkState() {
    setEnable(this, isActivate());
  }

  /**
   * Test procedure
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure(new LF5Appender());
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(480, 320);
    f.getContentPane().setLayout(new BorderLayout());

    Navigation n =
        new Navigation() {

          @Override
          public void navigateTo(IPunchMachinePanelActivate punchPanel, PunchScreen newScreen) {
            

          }
        };

    f.getContentPane().add(new JMachineMove(ConfigFactory.getInstance(), n), BorderLayout.CENTER);
    f.setVisible(true);
  }
}
