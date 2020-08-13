package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.gcode.GCodeCompiler;

/**
 * this class is responsible to import / export XML punch plans, and convert
 * them into model objects
 * 
 * @author pfreydiere
 *
 */
public class PunchPlanIO {

	private static Logger logger = Logger.getLogger(PunchPlanIO.class);

	public static void exportToGRBL(File file, PunchPlan p, GCodeCompiler gcodeCompiler) throws Exception {

		final FileWriter fos = new FileWriter(file);
		try {
			exportToGRBL(fos, p, gcodeCompiler);
		} finally {
			fos.close();
		}
	}

	public static void exportToGRBL(Writer writer, PunchPlan p, GCodeCompiler gcodeCompiler) throws Exception {

		// write header
		final AtomicInteger ai = new AtomicInteger(0);

		gcodeCompiler.visit(p);
		List<String> list = gcodeCompiler.getGCODECommands();
		for (String s : list) {
			writer.write(s + "\n");
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
					// handle punch
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
