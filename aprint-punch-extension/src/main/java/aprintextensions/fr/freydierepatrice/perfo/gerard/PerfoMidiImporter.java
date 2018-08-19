package aprintextensions.fr.freydierepatrice.perfo.gerard;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import org.barrelorgandiscovery.gui.aprint.extensions.ImporterParameters;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.AbstractMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.LinearTransposition;
import org.barrelorgandiscovery.virtualbook.transformation.importer.LinearMidiImporter;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiAdvancedEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiEventGroup;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiNote;
import org.barrelorgandiscovery.virtualbook.transformation.importer.Utils;

public class PerfoMidiImporter extends AbstractMidiImporter implements
		ImporterParameters {

	private Logger logger = Logger.getLogger(PerfoMidiImporter.class);

	private Scale destinationscale = null;

	private LinearTransposition trans = null;

	private String nameprefix = null;

	private double minholesize = 3.0;

	public PerfoMidiImporter(String nameprefix, Scale destinationscale,
			LinearTransposition trans) {
		this(nameprefix, destinationscale, trans, 3.0);
	}

	public PerfoMidiImporter(String nameprefix, Scale destinationscale,
			LinearTransposition trans, double minholesize) {
		assert destinationscale != null;
		this.destinationscale = destinationscale;
		assert trans != null;
		this.trans = trans;
		this.nameprefix = nameprefix;
		this.minholesize = minholesize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.freydierepatrice.virtualbook.transformation.AbstractMidiImporter#convert
	 * (fr.freydierepatrice.virtualbook.transformation.importer.MidiFile)
	 */
	@Override
	public MidiConversionResult convert(MidiFile midifile) {

		try {
			MidiEventGroup meg = new MidiEventGroup();

			for (Iterator<MidiAdvancedEvent> iterator = midifile.iterator(); iterator
					.hasNext();) {
				MidiAdvancedEvent midiAdvancedEvent = iterator.next();

				if (midiAdvancedEvent.getClass().getName()
						.equals(MidiNote.class.getName())) {
					MidiNote mn = (MidiNote) midiAdvancedEvent;

					if (mn.getChannel() == 9) {
						// percu

					} else {

						int midinote = mn.getMidiNote();
						midinote += this.parameters.getDecalage();
						if (midinote >= 0 && midinote < 128) {
							mn = new MidiNote(mn.getTimeStamp(),
									mn.getLength(), midinote, mn.getTrack(),
									mn.getChannel());

						} else {
							logger.warn("midi note " + midinote
									+ " out of scope");
							continue;
						}

					}

					// Vérification de la longueur ...
					if (destinationscale.timeToMM(mn.getLength()) < parameters
							.getMinholesize()) {
						mn = new MidiNote(mn.getTimeStamp(),
								destinationscale.mmToTime(parameters
										.getMinholesize()) + 10,
								mn.getMidiNote(), mn.getTrack(),
								mn.getChannel());
						logger.info("enlarge midinote " + mn);
					}

					meg.add(mn);

				} else {
					meg.add(midiAdvancedEvent);
				}

			}

			// setup the translation ...

			LinearMidiImporter li = Utils.linearToMidiImporter(trans);

			MidiConversionResult mcr = li.convert(meg);

			VirtualBook intermediateVirtualBook = mcr.virtualbook;

			VirtualBook flattened = intermediateVirtualBook
					.flattenVirtualBook();

			intermediateVirtualBook = flattened;

			if (parameters.isPreserveInterHoles()) {
				logger.debug("force les brides entre les trous ...");

				VirtualBook v = new VirtualBook(
						intermediateVirtualBook.getScale());

				ArrayList<Hole> holesCopy = intermediateVirtualBook
						.getOrderedHolesCopy();

				logger.debug("holecopy getted ..");
				for (Iterator iterator = holesCopy.iterator(); iterator
						.hasNext();) {
					Hole hole = (Hole) iterator.next();

					// recherche les trous suivant + bride
					long timedInterHoles = intermediateVirtualBook.getScale()
							.mmToTime(parameters.getMininterholesize());

					ArrayList<Hole> findHoles = new ArrayList<Hole>(
							intermediateVirtualBook.findHoles(
									hole.getTimestamp() + hole.getTimeLength()
											+ timedInterHoles, 1,
									hole.getTrack(), hole.getTrack()));

					if (findHoles.size() > 0) {

						logger.debug("hole :" + hole + " must be reduced");

						long endtimestamp = hole.getTimestamp()
								+ hole.getTimeLength() + timedInterHoles;
						for (Iterator iterator2 = findHoles.iterator(); iterator2
								.hasNext();) {
							Hole hevaluated = (Hole) iterator2.next();
							if (hevaluated.getTimestamp() < endtimestamp)
								endtimestamp = hevaluated.getTimestamp();
						}

						long length = endtimestamp - timedInterHoles
								- hole.getTimestamp();

						if (length < 0)
							length = 1;

						Hole newHole = new Hole(hole.getTrack(),
								hole.getTimestamp(), length);

						logger.debug("newHole :" + newHole);

						hole = newHole;

					}
					// on ajoute le trou
					v.addHole(hole);
				}

				mcr.virtualbook = v;

			}

			return mcr;

		} catch (Throwable t) {
			logger.error("error while processing midi importer");
			BugReporter.sendBugReport();
			throw new RuntimeException(t.getMessage(), t);
		}
	}

	@Override
	public String getDescription() {

		return "Transposition";
	}

	@Override
	public Scale getScaleDestination() {
		return this.destinationscale;
	}

	@Override
	public String getName() {
		return (nameprefix == null ? "" : nameprefix + ":")
				+ "Conversion avec transposition en utilisant "
				+ this.trans.getName();
	}

	private PerfoMidiParameters parameters = new PerfoMidiParameters();

	public Object getParametersInstanceBean() {
		this.parameters.setMinholesize(this.minholesize);
		return this.parameters;
	}

	public void setParametersToUse(Object parameters) {
		this.parameters = (PerfoMidiParameters) parameters;
	}

	@Override
	public String toString() {
		return getName();
	}

}
