/**
 * 
 */
package org.barrelorgandiscovery.gui.aprintng;

import groovy.lang.Binding;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.HashMap;

import javax.sound.midi.Sequence;

import org.barrelorgandiscovery.editableinstrument.InstrumentScript;
import org.barrelorgandiscovery.groovy.APrintGroovyShell;
import org.barrelorgandiscovery.listeningconverter.MIDIListeningConverter;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFileIO;

public class GroovyScriptVirtualBookToMidiConverter implements
		MIDIListeningConverter {

	private InstrumentScript instrumentScript = null;

	public GroovyScriptVirtualBookToMidiConverter(
			InstrumentScript instrumentScrpt) {
		this.instrumentScript = instrumentScrpt;
	}

	public Sequence convert(VirtualBook vb) throws Exception {

		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("virtualbook", vb); //$NON-NLS-1$

		final StringWriter sw = new StringWriter();

		Binding binding = new Binding();
		// binding for the output in the console ...
		binding.setProperty("out", new PrintStream(new OutputStream() { //$NON-NLS-1$

					@Override
					public void write(byte[] b, int off, int len)
							throws IOException {

						String s = new String(b, off, len);
						try {

							sw.append(s).append("\n");
						} catch (Exception ex) {
							ex.printStackTrace(System.err);
						}

					}

					@Override
					public void write(int b) throws IOException {
						try {
							sw.append("ERROR :")
									.append("" + (char) b).append("\n"); //$NON-NLS-1$
						} catch (Exception ex) {
							ex.printStackTrace(System.err);
						}
					}
				}));

		binding.setProperty("virtualbook", vb);

		APrintGroovyShell printGroovyShell = new APrintGroovyShell(binding);
		Object result = null;

		try {
			result = printGroovyShell.evaluate(instrumentScript
					.getContent());
		} catch (Throwable t) {
			APrintNGVirtualBookInternalFrame.logger.error("error while evaluating the script :"
					+ t.getMessage(), t);
			APrintNGVirtualBookInternalFrame.logger.error(sw.toString());
		}

		if (result == null)
			throw new Exception("null object returned");

		if (result instanceof MidiFile) {
			MidiFile f = (MidiFile) result;

			return MidiFileIO.createSequence(f);
		}

		throw new Exception("return object is not instanceof MidiFile");

	}

}