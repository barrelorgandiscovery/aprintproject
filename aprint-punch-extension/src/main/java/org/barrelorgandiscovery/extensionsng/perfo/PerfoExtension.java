package org.barrelorgandiscovery.extensionsng.perfo;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensions.IExtension;
import org.barrelorgandiscovery.extensions.SimpleExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.ImportersExtensionPoint;
import org.barrelorgandiscovery.gui.aprint.extensionspoints.InformRepositoryExtensionPoint;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameExtensionPoints;
import org.barrelorgandiscovery.repository.Repository;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractTransformation;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;

import aprintextensions.fr.freydierepatrice.perfo.gerard.PerfoMidiImporter;

/**
 * Extension de perforation
 * @author pfreydiere
 *
 */
public class PerfoExtension implements IExtension,
		VirtualBookFrameExtensionPoints, ImportersExtensionPoint,
		InformRepositoryExtensionPoint {

	private static Logger logger = Logger.getLogger(PerfoExtension.class);

	private ExtensionPoint[] extensionPoints = null;

	public PerfoExtension() {
		try {
			extensionPoints = new ExtensionPoint[] {
					new SimpleExtensionPoint(
							VirtualBookFrameExtensionPoints.class, this),
					new SimpleExtensionPoint(ImportersExtensionPoint.class,
							this),
					new SimpleExtensionPoint(
							InformRepositoryExtensionPoint.class, this) };
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public ExtensionPoint[] getExtensionPoints() {
		return extensionPoints;
	}

	public String getName() {
		return "Perfo Extension";
	}

	public IExtension newExtension() {
		return new PerfoExtensionVirtualBook();
	}

	private Repository rep = null;

	public void informRepository(Repository repository) {
		this.rep = repository;
	}

	public ArrayList<AbstractMidiImporter> getExtensionImporterInstance(
			Scale destinationscale) {

		logger.debug("getExtensionImporterInstance");

		Scale gmidi = Scale.getGammeMidiInstance();
		ArrayList<AbstractTransformation> trans = rep.getTranspositionManager()
				.findTransposition(gmidi, destinationscale);

		if (trans == null || trans.size() == 0) {
			logger.warn("no midi transposition for " + destinationscale);
			return null;
		}

		ArrayList<AbstractMidiImporter> l = new ArrayList<AbstractMidiImporter>();

		for (Iterator<AbstractTransformation> iterator = trans.iterator(); iterator
				.hasNext();) {
			AbstractTransformation abstractTransformation = iterator.next();

			if (abstractTransformation instanceof LinearTransposition) {
				LinearTransposition lt = (LinearTransposition) abstractTransformation;
				l.add(new PerfoMidiImporter(getName(), destinationscale, lt));
			}
		}

		return l;
	}

}
