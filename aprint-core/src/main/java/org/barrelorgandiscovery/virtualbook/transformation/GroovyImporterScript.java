package org.barrelorgandiscovery.virtualbook.transformation;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.InstrumentScript;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiConversionResult;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;

public class GroovyImporterScript extends AbstractMidiImporter {

	private static Logger logger = Logger.getLogger(GroovyImporterScript.class);

	private InstrumentScript is;
	private Scale destination;

	public GroovyImporterScript(InstrumentScript is, Scale destination) {
		this.is = is;
		this.destination = destination;
	}

	@Override
	public String getDescription() {
		return "Script " + is.getName();
	}

	@Override
	public String getName() {
		return is.getName();
	}

	@Override
	public MidiConversionResult convert(MidiFile midifile) {

		Object result = null;
		try {

			logger.debug("setting midifile property");

			Binding b = new Binding();

			b.setProperty("midifile", midifile);

			logger.debug("setting the scale parameter");
			b.setProperty("scale", destination);

			GroovyShell gs = new GroovyShell(b);

			result = gs.evaluate(is.getContent());

		} catch (Throwable ex) {

			throw new RuntimeException("error in evaluating the script :"
					+ ex.getMessage(), ex);

		}

		if (result instanceof MidiConversionResult)
			return (MidiConversionResult) result;

		if (result instanceof VirtualBook) {
			MidiConversionResult r = new MidiConversionResult();
			r.virtualbook = (VirtualBook) result;
			return r;
		}

		throw new RuntimeException("bad script, it returns an object of "
				+ result.getClass().getName() + " , whereas it must be "
				+ VirtualBook.class.getName() + " or "
				+ MidiConversionResult.class.getName());
	}

	@Override
	public Scale getScaleDestination() {
		return destination;
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
