package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.punchio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CommandVisitor;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.punch.x2016.Punch;
import org.barrelorgandiscovery.punch.x2016.PunchDisplacement;
import org.barrelorgandiscovery.punch.x2016.PunchplanDocument;
import org.barrelorgandiscovery.tools.StreamsTools;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.tools.streamstorage.ZipStreamMarshaller;
import org.barrelorgandiscovery.tools.streamstorage.ZipStreamStorage;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO;
import org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult;

public class PunchIO {

  private static Logger logger = Logger.getLogger(PunchIO.class);

  public PunchIO() {}

  public static PunchplanDocument convert(PunchPlan p) throws Exception {

    logger.debug("convert punchplan " + p);

    PunchplanDocument doc = PunchplanDocument.Factory.newInstance();
    final org.barrelorgandiscovery.punch.x2016.PunchPlan punchplan = doc.addNewPunchplan();

    CommandVisitor v =
        new CommandVisitor() {

          @Override
          public void visit(int index, HomingCommand command) throws Exception {}

          @Override
          public void visit(int index, DisplacementCommand displacementCommand) throws Exception {

            PunchDisplacement d = PunchDisplacement.Factory.newInstance();
            d.setX(displacementCommand.getX());
            d.setY(displacementCommand.getY());
            punchplan.addNewPunchcommand().set(d);
          }

          @Override
          public void visit(int index, PunchCommand punchCommand) throws Exception {
            Punch punch = Punch.Factory.newInstance();
            punch.setX(punchCommand.getX());
            punch.setY(punchCommand.getY());
            punchplan.addNewPunchcommand().set(punch);
          }
        };

    v.visit(p);

    return doc;
  }

  public static PunchPlan fromPunchPlan(
      org.barrelorgandiscovery.punch.x2016.PunchPlan xmlPunch) throws Exception {
    logger.debug("from punch plan " + xmlPunch);
    PunchPlan p = new PunchPlan();
    org.barrelorgandiscovery.punch.x2016.PunchCommand[] pcs = xmlPunch.getPunchcommandArray();
    for (org.barrelorgandiscovery.punch.x2016.PunchCommand pc : pcs) {
      logger.debug("element " + pc);
      if (pc instanceof Punch) {
        Punch punch = (Punch) pc;
        p.getCommandsByRef().add(new PunchCommand(punch.getX(), punch.getY()));
      } else if (pc instanceof PunchDisplacement) {
        PunchDisplacement pdisplacement = (PunchDisplacement) pc;
        p.getCommandsByRef().add(new PunchCommand(pdisplacement.getX(), pdisplacement.getY()));

      } else {
        throw new Exception("unknown element " + pc);
      }
    }

    return p;
  }

  /**
   * Export the punchfile bag
   *
   * @param punchFile
   */
  public static void exportToPunchFile(PunchFileBag punchFile, File outFile) throws Exception {
    assert punchFile != null;

    File createTempFile =
        File.createTempFile("storagefoldertemp", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
    createTempFile.delete();
    createTempFile.mkdirs();
    try {

      // save the book
      FolderStreamStorage fss = new FolderStreamStorage(createTempFile);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      VirtualBookXmlIO.write(baos, punchFile.getVirtualBook(), "");
      fss.saveStream("virtualbook", "book", new ByteArrayInputStream(baos.toByteArray()));

      // save the punch file
      PunchplanDocument c = convert(punchFile.getPunchplan());
      XmlOptions options = new XmlOptions();
      options.setSavePrettyPrint();

      baos = new ByteArrayOutputStream();
      c.save(baos, options);
      fss.saveStream("punchplan", "punch", new ByteArrayInputStream(baos.toByteArray()));

      FileOutputStream sof = new FileOutputStream(outFile);
      try {
        // zip the elements
        ZipStreamMarshaller zsm = new ZipStreamMarshaller();
        zsm.pack(fss, sof);

      } finally {
        sof.close();
      }

    } finally {
      StreamsTools.recurseDelete(createTempFile);
    }
  }

  /**
   * read content from file
   *
   * @param punchFile
   * @return
   * @throws Exception
   */
  public static PunchFileBag readPunchFile(File punchFile) throws Exception {

    FileInputStream fis = new FileInputStream(punchFile);
    ZipStreamStorage sz = new ZipStreamStorage(fis);
    try {

      //open stream
      InputStream s = sz.openStream("virtualbook.book");
      VirtualBookResult vbr = VirtualBookXmlIO.read(s);

      InputStream pp = sz.openStream("punchplan.punch");
      PunchplanDocument d = PunchplanDocument.Factory.parse(pp, new XmlOptions());
      PunchPlan punchplan = fromPunchPlan(d.getPunchplan());

      return new PunchFileBag(vbr.virtualBook, punchplan);

    } finally {
      fis.close();
    }
  }
}
