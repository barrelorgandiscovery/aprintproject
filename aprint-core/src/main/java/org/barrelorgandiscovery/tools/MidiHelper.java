package org.barrelorgandiscovery.tools;

import java.security.InvalidParameterException;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.ScaleException;


/**
 * Utility class
 * 
 * @author Freydiere Patrice
 * 
 */
public class MidiHelper {

	/**
	 * Read a coded midi code texte, and translate it to midi code
	 * 
	 * @param s
	 *            the midi code texte (D3, D#3 for example)
	 * @return the midi code associated to this text
	 * @throws ScaleException
	 *             Exception is raised if the text is not properly formatted
	 */
	public static int midiCode(String s) throws InvalidParameterException {

		if (s == null || s.length() == 0)
			throw new InvalidParameterException("string is null");

		int n = 0;

		char note = s.charAt(0);
		switch (Character.toUpperCase(note)) {
		case 'C':
			n = 0;
			break;
		case 'D':
			n = 2;
			break;
		case 'E':
			n = 4;
			break;
		case 'F':
			n = 5;
			break;
		case 'G':
			n = 7;
			break;
		case 'A':
			n = 9;
			break;
		case 'B':
			n = 11;
			break;
		default:
			throw new InvalidParameterException("note " + note + " inconnue");
		}

		if (s.length() < 2)
			throw new InvalidParameterException("bad note definition :" + note + ", missing octave");
		
		int octave;
		if (s.charAt(1) == '#') {
			n += 1;
			octave = Integer.parseInt("" + s.substring(2));
		} else {
			octave = Integer.parseInt("" + s.substring(1));
		}

		n += (octave * 12);

		return n;
	}

	public static final String[] NOTES = { "C", "C#", "D", "D#", "E", "F",
			"F#", "G", "G#", "A", "A#", "B" };

	/**
	 * This function convert a midi code to text, with octave
	 * 
	 * 
	 * @param code
	 * @return
	 */
	public static String midiLibelle(int code) {

		// Octage no

		int octave = getOctave(code);

		// note

		int octavedo = octave * 12;
		int note = code - octavedo;

		return NOTES[note] + octave;

	}

	/**
	 * retourne le no de la note, du code midi passé en paramètre
	 * @param code le code midi
	 * @return la note (0-11)
	 */
	public static int extractNoteFromMidiCode(int code) {
		int octave = getOctave(code);

		// note

		int octavedo = octave * 12;
		int note = code - octavedo;

		return note;
	}

	public static int computeMidiCodeFromNoteAndOctave(int midiNote, int octave) {
		return octave * 12 + midiNote;
	}

	/**
	 * Get the text associated to the midi code
	 * 
	 * @param code
	 * @return
	 */
	public static String localizedMidiLibelle(int code) {
		// Octage no

		int octave = getOctave(code);

		// note

		int octavedo = octave * 12;
		int note = code - octavedo;

		if (note < 0)
			note += 12;

		String n = Messages.getString("note." + note);

		return n + " " + octave;
	}

	/**
	 * Get a localized midi note text, without octave
	 * 
	 * @param note
	 * @return
	 */
	public static String getLocalizedMidiNote(int note) {
		// Octave no

		int octave = getOctave(note);

		// note

		int octavedo = octave * 12;
		int n = note - octavedo;

		return Messages.getString("note." + n);

	}

	/**
	 * get a text associated to the midi code, without octave
	 * 
	 * @param note
	 * @return the midicode text
	 */
	public static String getMidiNote(int note) {
		// Octave no

		int octave = getOctave(note);

		// note

		int octavedo = octave * 12;
		int n = note - octavedo;

		return NOTES[n];
	}

	/**
	 * compute the octave of a midi code
	 * 
	 * @param code
	 *            in (0-12) range
	 * @return
	 */
	public static int getOctave(int code) {
		return (int) (code / 12);
	}

	/**
	 * Convert midi code to hertz
	 * 
	 * @param midicode
	 * @return
	 */
	public static double hertz(int midicode) {
		return 440.0 * Math.pow(2, (((double) midicode - 69) / 12));
	}

	/**
	 * return a boolean to indicate if the midicode is a diese
	 * 
	 * @param midicode
	 * @return
	 */
	public static boolean isDiese(int midicode) {
		int note = midicode % 12;
		switch (note) {
		case 1:
		case 3:
		case 6:
		case 8:
		case 10:
			return true;
		}

		return false;
	}

	/**
	 * test function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		System.out.println(midiLibelle(72) + " doit être egal à C5");

		for (int i = 0; i < 128; i++) {
			System.out.println(midiLibelle(i));
			System.out.println(i + " - " + midiCode(midiLibelle(i)));
		}

		System.out.println("La 4 : " + hertz(midiCode("A4")));

		System.out.println("La 5 : " + hertz(midiCode("A5")));

	}

}
