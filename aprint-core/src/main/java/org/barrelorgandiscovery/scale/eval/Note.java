package org.barrelorgandiscovery.scale.eval;

import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.tools.MidiHelper;
import org.barrelorgandiscovery.tools.StringTools;

/**
 * Note class associated to a registersetname
 * 
 * @author use
 * 
 */
public class Note implements Comparable<Note> {

	private int midiCode;

	private String registerSetName = null;

	public Note(int midiCode) {
		this.midiCode = midiCode;

	}

	public Note(int midiCode, String registerSetName) {
		this(midiCode);
		this.registerSetName = registerSetName;
	}
	
	public Note(NoteDef nd)
	{
		this(nd.getMidiNote(), nd.getRegisterSetName());
	}

	public int compareTo(Note o) {

		String s1 = registerSetName;
		String s2 = o.registerSetName;

		int res = StringTools.compare(s1, s2);
		if (res != 0)
			return res;

		if (o.midiCode > midiCode)
			return -1;

		if (o.midiCode < midiCode)
			return 1;

		return 0;

	}

	public boolean isSameSimpleNoteAs(Note n2) {
		if (n2 == null)
			return false;

		assert n2 != null;
		return MidiHelper.extractNoteFromMidiCode(midiCode) == MidiHelper
				.extractNoteFromMidiCode(n2.midiCode);
	}
	
	public boolean hasSameMidiCode(Note n2)
	{
		return midiCode == n2.midiCode;
	}
	

	public int getMidiCode() {
		return midiCode;
	}

	public String getRegisterSetName() {
		return registerSetName;
	}

	@Override
	public String toString() {
		return "" + MidiHelper.midiLibelle(midiCode)
				+ (registerSetName != null ? "-" + registerSetName : "");
	}

}
