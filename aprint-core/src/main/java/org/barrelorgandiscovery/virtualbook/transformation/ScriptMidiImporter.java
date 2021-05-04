package org.barrelorgandiscovery.virtualbook.transformation;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.scale.StorageScaleManager;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 * Midi importer transformation using javascript script
 * 
 * @author Freydiere Patrice
 * 
 */
public class ScriptMidiImporter extends AbstractMidiImporter {

	private static Logger logger = Logger.getLogger(ScriptMidiImporter.class);

	private String name = null;

	private Scriptable engine;
	private Context ctx;

	/**
	 * Construct an object for midi importer
	 * 
	 * @param name
	 *            the name of the midi transformation
	 * @param script
	 *            the script input stream
	 * @param scalemanager
	 *            the scalemanager
	 * @throws Exception
	 */
	public ScriptMidiImporter(String name, InputStream script,
			ScaleManager scalemanager) throws Exception {

		assert name != null;
		assert script != null;
		assert scalemanager != null;

		this.name = name;

		// Lecture du script ...

		logger.debug("Lecture du script");

		InputStreamReader ir = new InputStreamReader(script);

		StringBuffer sb = new StringBuffer();
		// lecture de toutes les lignes ....
		int c;
		while ((c = ir.read()) != -1) {
			sb.append((char) c);
		}

		logger.debug("Script lu, interprétation du script");

		// create a script engine manager

		ctx = Context.enter();

		engine = ctx.initStandardObjects();

		// ajout de la variable gammemanager ...
		ctx.evaluateString(engine, sb.toString(), name, 1, null);

		Object wrappedOut = Context.javaToJS(scalemanager, engine);
		ScriptableObject.putProperty(engine, "gammemanager", wrappedOut);

		// vérification que la gamme de destination existe bien
		if (getScaleDestination() == null)
			throw new Exception("destination gamme for importer script " + name
					+ " not found");

		logger.debug("script interprété");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.cartonvirtuel.AbstractMidiImporter#convert(fr.freydierepatrice.cartonvirtuel.importer.MidiFile)
	 */
	@Override
	public MidiConversionResult convert(MidiFile midifile) {
		try {

			Object fObj = engine.get("convert", engine);
			if (!(fObj instanceof Function)) {
				throw new Exception("convert is undefined or not a function.");
			} else {
				Object functionArgs[] = { midifile };
				Function f = (Function) fObj;
				Object result = f.call(ctx, engine, engine, functionArgs);
				return (MidiConversionResult) result;
			}

		} catch (Exception ex) {
			logger.error("convert " + this.name, ex);
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.cartonvirtuel.AbstractMidiImporter#getDescription()
	 */
	@Override
	public String getDescription() {
		try {
			Object fObj = engine.get("description", engine);
			if (!(fObj instanceof Function)) {
				throw new Exception(
						"description is undefined or not a function.");
			} else {
				Object functionArgs[] = {};
				Function f = (Function) fObj;
				NativeJavaObject result = (NativeJavaObject) f.call(ctx,
						engine, engine, functionArgs);
				return (String) result.unwrap();
			}

		} catch (Exception ex) {
			logger.error("getDescription " + this.name, ex);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.cartonvirtuel.AbstractTransformation#getGammeDestination()
	 */
	@Override
	public Scale getScaleDestination() {
		try {
			Object fObj = engine.get("gamme", engine);
			if (!(fObj instanceof Function)) {
				throw new Exception("gamme is undefined or not a function.");
			} else {
				Object functionArgs[] = {};
				Function f = (Function) fObj;
				NativeJavaObject result = (NativeJavaObject) f.call(ctx,
						engine, engine, functionArgs);
				return (Scale) result.unwrap();
			}

		} catch (Exception ex) {
			logger.error("getGammeDestination " + this.name, ex);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.freydierepatrice.virtualbook.transformation.AbstractTransformation#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Test function
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		// Création d'un gamme manager ...
		FolderStreamStorage fis = new FolderStreamStorage(
				new File(
						"C:\\Documents and Settings\\Freydiere Patrice\\workspace\\APrint\\gammes"));
		ScaleManager gm = new StorageScaleManager(fis);

		ScriptMidiImporter importer = new ScriptMidiImporter("name", fis
				.openStream("50Limonaire.importerscript"), gm);

		// lecture du fichier midi ...
		MidiFile mf = MidiFileIO
				.read(new File(
						"C:/Documents and Settings/Freydiere Patrice/Bureau/La_Souris_Noire_20080212.MID"));

		logger.debug(importer.getScaleDestination());

		logger.debug(importer.convert(mf));

	}

	private String description_cache = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (description_cache == null) {
			try {
				description_cache = getDescription();
			} catch (Exception ex) {
				logger
						.debug("impossible de récupérer la description du script");
				description_cache = "";
			}
		}
		return this.name + "-" + description_cache;
	}

}
