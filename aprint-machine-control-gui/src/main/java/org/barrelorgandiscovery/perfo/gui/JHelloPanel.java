package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.forms.components.panel.FormPanel;

public class JHelloPanel extends JPanel implements IPunchMachinePanelActivate {

  /** */
  private static final long serialVersionUID = -6532092867267382249L;

  private Navigation navigation;
  
  public JHelloPanel(Navigation navigation) throws Exception {
    super();
    this.navigation = navigation;
    initComponents();
  }

  protected void initComponents() throws Exception {

    FormPanel p = new FormPanel(getClass().getResourceAsStream("hello.jfrm"));
    
    JLabel label = p.getLabel("lblaprintmachine");
    label.setText("APrint Commander - 10.2018");
    label.setIcon(new ImageIcon(getClass().getResource("pda_black.png")));
    
    AbstractButton fileselectbtn = p.getButton("fileselect");
    fileselectbtn.setText("Select Files");
    fileselectbtn.setIcon(new ImageIcon(getClass().getResource("fileopen.png")));
    fileselectbtn.addActionListener( (e) -> {
    	navigation.navigateTo(JHelloPanel.this, PunchScreen.SelectFiles);
    });
    
    AbstractButton parameterbtn = p.getButton("parameters");
    parameterbtn.setText("Parameters");
    parameterbtn.setIcon(new ImageIcon(getClass().getResource("kgpg_edit.png")));
    parameterbtn.addActionListener( (e) -> {
    	navigation.navigateTo(JHelloPanel.this, PunchScreen.Parameters);
    });
    
    setLayout(new BorderLayout());
    add(p, BorderLayout.CENTER);
  }

  @Override
  public void activate() {}
  
}
