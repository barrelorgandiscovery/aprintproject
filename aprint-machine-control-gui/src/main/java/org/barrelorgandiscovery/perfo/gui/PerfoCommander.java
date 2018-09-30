package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.perfo.ConfigFactory;
import org.barrelorgandiscovery.perfo.PunchProcess;

public class PerfoCommander extends JFrame implements Navigation {

  /** */
  private static final long serialVersionUID = -327201276216073491L;

  private static Logger logger = Logger.getLogger(PerfoCommander.class);

  public PerfoCommander() throws Exception {
    super();
    initComponents();
  }

  private PunchProcess punchProcess = new PunchProcess(ConfigFactory.getInstance());

  private Map<PunchScreen, IPunchMachinePanelActivate> panelList;

  private IPunchMachinePanelActivate current;

  protected void initComponents() throws Exception {
    setSize(480, 320);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout());

    JHelloPanel hello = new JHelloPanel(this);
    JSelectFiles selectfiles = new JSelectFiles(punchProcess, this);
    JPunch punch = new JPunch(punchProcess, selectfiles, this);
    JMachineMove machineMove = new JMachineMove(ConfigFactory.getInstance(), this);
    JParameter parameter = new JParameter(this);

    panelList = new HashMap<>();
    panelList.put(PunchScreen.Hello, hello);
    panelList.put(PunchScreen.SelectFiles, selectfiles);
    panelList.put(PunchScreen.Punch, punch);
    panelList.put(PunchScreen.Parameters, parameter);
    panelList.put(PunchScreen.MachineMove, machineMove);

    current = panelList.get(PunchScreen.Hello);
    assert current != null;

    getContentPane().add((JPanel) current, BorderLayout.CENTER);
  }

  public static void main(String[] args) throws Exception {

    // BasicConfigurator.configure(new LF5Appender());

	  EventQueue.invokeLater(new Runnable() {
          public void run() {
              try {
                  UIManager.setLookAndFeel(com.jgoodies.looks.plastic.Plastic3DLookAndFeel.class.getName());
                  JFrame f = new PerfoCommander();
                  f.setVisible(true);
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });
   
  }

  void changePanel(JPanel newPanel) {
    assert newPanel != null;
    getContentPane().removeAll();
    getContentPane().add(newPanel, BorderLayout.CENTER);
  }

  @Override
  public void navigateTo(IPunchMachinePanelActivate panel, PunchScreen newScreen) {
    logger.debug("change screeen to " + newScreen);
    IPunchMachinePanelActivate newP = panelList.get(newScreen);

    if (newP != null) {
      current = newP;
      changePanel((JPanel) newP);
      revalidate();
      repaint();
      newP.activate();

      repaint();
    }
  }
}
