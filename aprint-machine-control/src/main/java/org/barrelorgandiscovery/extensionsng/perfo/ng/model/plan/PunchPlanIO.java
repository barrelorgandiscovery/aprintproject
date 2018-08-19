package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import java.io.File;
import java.io.FileWriter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class PunchPlanIO {

	private static Logger logger = Logger.getLogger(PunchPlanIO.class);
	
	public static void exportToGRBL(File file, PunchPlan p) throws Exception {

		final FileWriter fos = new FileWriter(file);
		try {

			// write header
			final AtomicInteger ai = new AtomicInteger(0);

			CommandVisitor v = new CommandVisitor() {
				@Override
				public void visit(int index,DisplacementCommand displacementCommand)
						throws Exception {

					fos.write(String.format(Locale.ENGLISH, "N%1$d G90 X%2$f Y%3$f\n", //$NON-NLS-1$
							ai.addAndGet(1), displacementCommand.getY(),
							displacementCommand.getX()));

				}

				@Override
				public void visit(int index,PunchCommand punchCommand) throws Exception {
					fos.write( String.format(Locale.ENGLISH,"N%1$d G90 X%2$f Y%3$f\n", ai.addAndGet(1), //$NON-NLS-1$
							punchCommand.getY(), punchCommand.getX()));
					fos.write("M100\n"); //$NON-NLS-1$
				}
				
				@Override
				public void visit(int index,HomingCommand command) throws Exception {
					
				}
				
			};

			v.visit(p);

		} finally {
			fos.close();
		}

	}
}
