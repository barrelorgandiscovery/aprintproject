package org.barrelorgandiscovery.extensionsng.scanner.wizardscan;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.io.File;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.barrelorgandiscovery.extensionsng.scanner.wizard.JMergeImagesStep;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JChooseFolderStep;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JMergeImageWizardPanel;
import org.barrelorgandiscovery.gui.wizard.Wizard;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;

public class JScanWizard extends JPanel {

  private IPrefsStorage ps;

  private Wizard wizard;

  public JScanWizard(IPrefsStorage ps) throws Exception {
    this.ps = ps;
    initComponents();
  }

  protected void initComponents() throws Exception {

    JChooseFolderStep s = new JChooseFolderStep(ps);
    JScanParameterStep p = new JScanParameterStep(s, ps);

    wizard = new Wizard(Arrays.asList(s, p), null);

    setLayout(new BorderLayout());
    add(wizard, BorderLayout.CENTER);

    wizard.toFirst();
  }

  public static void main(String[] args) throws Exception {

    BasicConfigurator.configure(new LF5Appender());

    FilePrefsStorage p = new FilePrefsStorage(new File("c:\\temp\\wizardscan.properties"));
    p.load();

    JFrame f = new JFrame();
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(new JScanWizard(p), BorderLayout.CENTER);

    f.setSize(800, 600);
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
