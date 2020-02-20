package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class PunchPlanIO {

  private static Logger logger = Logger.getLogger(PunchPlanIO.class);

  public static void exportToGRBL(File file, PunchPlan p) throws Exception {

    final FileWriter fos = new FileWriter(file);
    try {

      // write header
      final AtomicInteger ai = new AtomicInteger(0);

      CommandVisitor v =
          new CommandVisitor() {
            @Override
            public void visit(int index, DisplacementCommand displacementCommand) throws Exception {
              fos.write(
                  String.format(
                      Locale.ENGLISH,
                      "N%1$d G90 X%2$f Y%3$f\n", //$NON-NLS-1$
                      ai.addAndGet(1),
                      displacementCommand.getY(), // The X Axis (Y in punch plan)
                      displacementCommand.getX()));
            }

            @Override
            public void visit(int index, PunchCommand punchCommand) throws Exception {
              fos.write(
                  String.format(
                      Locale.ENGLISH,
                      "N%1$d G90 X%2$f Y%3$f\n",
                      ai.addAndGet(1), //$NON-NLS-1$
                      punchCommand.getY(),
                      punchCommand.getX()));
              fos.write("M100\n"); //$NON-NLS-1$
            }

            @Override
            public void visit(int index, HomingCommand command) throws Exception {}
          };

      v.visit(p);

    } finally {
      fos.close();
    }
  }

  /**
   * Read gcode from inputstream, and construct punchplan object
   *
   * @param gcodeFile
   * @return
   * @throws Exception
   */
  public static PunchPlan readFromGRBL(InputStream inputStreamGcode) throws Exception {

    LineNumberReader ln = new LineNumberReader(new InputStreamReader(inputStreamGcode));

    Pattern displacement = Pattern.compile("^(N[0-9]+ )?G90 X(.+) Y(.+)$");
    Pattern punch = Pattern.compile("^M100$");

    PunchPlan p = new PunchPlan();

    String readLine;

    double x = Double.NaN, y = Double.NaN;

    while ((readLine = ln.readLine()) != null) {

      Matcher m = displacement.matcher(readLine);
      if (m.matches()) {
    	  
        // handle displacement
    	
    	  // x -> it's the Y in the GCode file
        x = Double.parseDouble(m.group(3));
        // it's the X in the GCode
        y = Double.parseDouble(m.group(2));

      } else {

        Matcher mpunch = punch.matcher(readLine);
        if (mpunch.matches()) {
          //handle punch
          p.getCommandsByRef().add(new PunchCommand(x, y));
        } else {
        	throw new Exception("unknown line :" + readLine);
        }
      }
    }

    return p;
  }

  /**
   * Read gcode from file, and construct punchplan object
   *
   * @param gcodeFile
   * @return
   * @throws Exception
   */
  public static PunchPlan readFromGRBL(File gcodeFile) throws Exception {

    return readFromGRBL(new FileInputStream(gcodeFile));
  }
}
