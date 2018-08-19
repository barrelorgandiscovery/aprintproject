package org.barrelorgandiscovery.listeningconverter;

import java.io.File;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.io.MidiIO;


/**
 * Test Routine for MIDI Conversion
 * @author use
 *
 */
public class TestEcoute {

	/**
	 * 
	 * Joue un fichier MIDI ... 
	 * 
	 * Ceci est une routine de test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

//			Sequence sequence = MidiSystem
//					.getSequence(new File(
//							"C:\\Documents and Settings\\Freydiere Patrice\\Bureau\\midi\\76tromb_.mid"));

			
			BasicConfigurator.configure(new LF5Appender());
			
			VirtualBook carton = MidiIO.readCarton(new File("C:\\Documents and Settings\\Freydiere Patrice\\Bureau\\midi\\76tromb_.mid"));
			
			
			Sequence sequence = EcouteConverter.convert(carton); 
			
			
			
			Sequencer seq = MidiSystem.getSequencer();
			
			seq.setSequence(sequence);
			
			seq.open();
			seq.start();

			while (seq.isRunning())
			{
				Thread.sleep(1000);
			}
			
			seq.stop();
			
			seq.close();
			
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}
}
