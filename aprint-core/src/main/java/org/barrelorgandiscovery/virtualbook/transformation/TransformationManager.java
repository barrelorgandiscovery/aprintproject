package org.barrelorgandiscovery.virtualbook.transformation;

import java.io.IOException;
import java.util.ArrayList;

import org.barrelorgandiscovery.scale.Scale;

/**
 * define the transformation manager interface, providing possible
 * transformations for books
 * 
 * @author pfreydiere
 *
 */
public interface TransformationManager {

	/**
	 * 
	 * Look for all the transformation for a source and a destination scale
	 * 
	 * @param source      la gamme source de la transposition
	 * @param destination la gamme destination de la transposition
	 * @return
	 */
	public abstract ArrayList<AbstractTransformation> findTransposition(Scale source, Scale destination);

	/**
	 * look for an importer for a destination Scale
	 * 
	 * @param destination
	 * @return
	 */
	public abstract ArrayList<AbstractMidiImporter> findImporter(Scale destination);

	/**
	 * delete an importer
	 * 
	 * @param importer
	 * @throws IOException
	 */
	public void deleteImporter(AbstractMidiImporter importer) throws Exception;

	public void saveImporter(AbstractMidiImporter importer) throws Exception;

	public void saveTransformation(AbstractTransformation transformation) throws Exception;

	public void deleteTransformation(AbstractTransformation transformation) throws Exception;

}