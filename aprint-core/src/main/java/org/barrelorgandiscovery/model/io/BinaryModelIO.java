package org.barrelorgandiscovery.model.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.barrelorgandiscovery.model.Model;
import org.barrelorgandiscovery.tools.SerializeTools;

/**
 * simple class to load and save model, for DEBUG purpose
 * 
 * @author pfreydiere
 *
 */
public class BinaryModelIO {

	public static String PROCESSMODEL_FILEEXTENSION = ".processmodel";

	/**
	 * save model in file
	 * 
	 * @param model
	 * @param outputFile
	 * @throws Exception
	 */
	public static void saveModel(Model model, File outputFile) throws Exception {
		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			SerializeTools.writeObject(model, fos);
		}
	}

	public static Model loadModel(File inputFile) throws Exception {
		try (FileInputStream fis = new FileInputStream(inputFile)) {
			return (Model) SerializeTools.readObject(fis);
		}
	}

}
