package org.barrelorgandiscovery.extensionsng.scanner;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensionsng.scanner.tools.VersionTools;
import org.barrelorgandiscovery.extensionsng.scanner.wizard.JMergeImageWizardPanel;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.WelcomeExtensionExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseExtension;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

/**
 * APrint NG extension
 *
 * @author pfreydiere
 */
public class ScannerExtension extends BaseExtension {

  private static Logger logger = Logger.getLogger(ScannerExtension.class);

  public ScannerExtension() throws Exception {
    super();
    this.defaultAboutAuthor = "Patrice Freydiere";
    this.defaultAboutVersion = VersionTools.getVersion();
  }

  @Override
  public String getName() {
    return "Scanner Extension";
  }

  @Override
  protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints) throws Exception {
    super.setupExtensionPoint(initExtensionPoints);

    WelcomeExtensionExtensionPoint wbe =
        new WelcomeExtensionExtensionPoint() {

          public void addComponentInPanel(JPanel p) {
            p.add(createNewProjectButton());
          }
        };

    initExtensionPoints.add(createExtensionPoint(WelcomeExtensionExtensionPoint.class, wbe));
  }

  private JButton createNewProjectButton() {
    JButton btn = new JButton("Merge Scan Images ...");
    btn.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            try {
              newProject();
            } catch (Throwable t) {
              logger.error("error creating new project :" + t.getMessage(), t);
              BugReporter.sendBugReport();
              JMessageBox.showError(ScannerExtension.this.application.getOwnerForDialog(), t);
            }
          }
        });
    return btn;
  }

  private void newProject() throws Exception {

    // ask for the folder + instrument

    APrintNGInternalFrame frame = new APrintNGInternalFrame(this.extensionPreferences);
    frame.getContentPane().setLayout(new BorderLayout());
    frame
        .getContentPane()
        .add(new JMergeImageWizardPanel(this.extensionPreferences), BorderLayout.CENTER);
    frame.setVisible(true);
  }
}
