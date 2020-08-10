package org.barrelorgandiscovery.extensionsng.perfo.ng;

import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensionsng.perfo.ng.tools.VersionTools;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.InitNGExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameExtensionPoints;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseExtension;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;

/**
 * Extension de perforation associée à une machine outils
 *
 * @author pfreydiere
 */
public class PerfoExtensionMachine extends BaseExtension
    implements IExtension, VirtualBookFrameExtensionPoints,
    InitNGExtensionPoint {

  private static Logger logger = Logger.getLogger(PerfoExtensionMachine.class);

  public PerfoExtensionMachine() throws Exception {
    super();
    this.defaultAboutAuthor = "Patrice Freydiere / Freddy Meyer / Jean Pierre Rosset";
    this.defaultAboutVersion = VersionTools.getVersion();
  }
  
  @Override
  protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints) throws Exception {
	  super.setupExtensionPoint(initExtensionPoints);
	  initExtensionPoints.add(createExtensionPoint(VirtualBookFrameExtensionPoints.class));
  }

  public String getName() {
    return "GCode Perfo Extension";
  }

  public IExtension newExtension() {
    try {
      return new PerfoExtensionMachineVirtualBook();
    } catch (Exception ex) {
      logger.error("error initialize the perfo extension :" + ex.getMessage(), ex);
      BugReporter.sendBugReport();
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }
}
