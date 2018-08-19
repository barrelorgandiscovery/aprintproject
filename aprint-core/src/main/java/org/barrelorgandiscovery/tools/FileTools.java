package org.barrelorgandiscovery.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;

/**
 * Tools for manipulating Files
 */
public class FileTools {

	private static Logger logger = Logger.getLogger(FileTools.class);

	public static boolean rename(File first, File newFile) {
		if (newFile.exists()) {
			logger.debug("destination file already exist");
			return false;
		}

		boolean renameOperation = first.renameTo(newFile);
		if (renameOperation)
			return true; // ok

		// else 
		// copy file ...
		try {
			FileOutputStream fos = new FileOutputStream(newFile);
			try {
				FileInputStream fis = new FileInputStream(first);
				try {

					StreamsTools.copyStream(fis, fos);

					logger.debug("rename done");

				} finally {
					fis.close();
				}

				first.delete(); // delete the original file

				return true;
			} finally {
				fos.close();
			}

		} catch (Exception ex) {
			logger.error("error in renaming file :" + ex.getMessage(), ex);
			return false;
		}
	}

	
	
	
}
