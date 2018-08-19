package org.barrelorgandiscovery.scale.importer;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;


/**
 * Outil de génération de la page web sur les gammes
 * 
 * @author Freydiere Patrice
 */
public class MidiboekConverterTask extends Task {

	public MidiboekConverterTask() {

	}

	private File outputfolder;

	public File getOutputfolder() {
		return outputfolder;
	}

	public void setOutputfolder(File outputfolder) {
		this.outputfolder = outputfolder;
	}

	private File midiBoekScalesfolder;

	public File getMidiBoekScalesfolder() {
		return midiBoekScalesfolder;
	}

	public void setMidiBoekScalesfolder(File scalesfolder) {
		this.midiBoekScalesfolder = scalesfolder;
	}

	@Override
	public void execute() throws BuildException {
		try {

			log("Verification des paramètres");

			if (midiBoekScalesfolder == null)
				throw new BuildException("no scalesfolder defined");

			if (outputfolder == null)
				throw new BuildException("no outputfolder defined");

			// Conversion des gammes ....

		
				System.out.println("using local :" + Locale.getDefault());

				File[] midiboekfiles = midiBoekScalesfolder
						.listFiles(new FileFilter() {
							public boolean accept(File pathname) {
								if (pathname.getName().endsWith(".gam"))
									return true;
								return false;
							}
						});

				for (File f : midiboekfiles) {
					try {
						Scale g = MidiBoekGammeImporter.importScale(f);
						ScaleIO.writeGamme(g, new File(outputfolder + "/"
								+ g.getName() + ".gamme"));
					} catch (Exception ex) {
						log("Erreur dans la transcription de " + f.getName()
								+ " " + ex.toString());
					}
				}
		
		} catch (Exception ex) {

			throw new BuildException(ex.getMessage(), ex);
		}

	}
}
