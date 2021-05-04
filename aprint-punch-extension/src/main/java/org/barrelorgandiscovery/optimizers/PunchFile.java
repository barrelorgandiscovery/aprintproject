package org.barrelorgandiscovery.optimizers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.barrelorgandiscovery.optimizers.model.Punch;

public class PunchFile {

	/**
	 * Save the punch file
	 * 
	 * @param file
	 *            filename
	 * @param punches
	 *            liste of "punches"
	 * @throws IOException
	 */
	public static void savePunch(File file, Punch[] punches) throws IOException {

		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		OutputStreamWriter fw = new OutputStreamWriter(bos);
		try {
			for (int i = 0; i < punches.length; i++) {
				Punch p = punches[i];
				fw.write(i + ": ");
				fw.write(Double.toString(p.x));
				fw.write(" ");
				fw.write(Double.toString(p.y));
				fw.write("\r\n");
			}
		} finally {
			fw.close();
		}

	}

}
