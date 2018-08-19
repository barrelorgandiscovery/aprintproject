package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class PerfoCommander {

  public PerfoCommander() {}

  public static void main(String[] args) throws Exception {
    JFrame f = new JFrame();
    f.setSize(320, 200);
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(new JMainPanel(), BorderLayout.CENTER);
  }
}
