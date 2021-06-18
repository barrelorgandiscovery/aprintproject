package org.barrelorgandiscovery.virtualbook.transformation.exporter;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.virtualbook.transformation.importer.AbstractMidiEventVisitor;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiControlChange;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiGenericEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiMetaGenericEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiNote;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiProgramChange;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiSignatureEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiSysExGenericEvent;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiTempoEvent;

public class MidiExporterVisitor extends AbstractMidiEventVisitor {

	private static Logger logger = Logger.getLogger(MidiExporterVisitor.class);
	private Sequence seq;
	private long micropertick;
	private Track track;

	public MidiExporterVisitor() throws Exception {
		this(384);
	}

	public MidiExporterVisitor(int ticksperbeat) throws Exception {

		int bpm = 120;
		
		
		seq = new Sequence(Sequence.PPQ, ticksperbeat);
		track = seq.createTrack();

		
		long l = 60_000_000L / bpm;
		
		micropertick = (long) ( 60_000_000L / bpm  / ticksperbeat);
		logger.debug("micropertick " + micropertick); //$NON-NLS-1$


		logger.debug("convert"); //$NON-NLS-1$


		byte[] b = { (byte) ((l >> 16) & 0xFF), (byte) ((l >> 8) & 0xFF), (byte) (l & 0xFF) };

		// Ajout du message de tempo ....

		MetaMessage mtm = new MetaMessage();
		mtm.setMessage(0x51, b, b.length);
		MidiEvent mt = new MidiEvent(mtm, 0);

		logger.debug("add a track for midi 0"); //$NON-NLS-1$
		track.add(mt);

	}

	public long timeStampToTick(long timestamp) {
		return (timestamp /* + decalage */) / micropertick;
	}

	@Override
	public void visit(MidiNote mn) throws Exception {

		if (mn.getMidiNote() != -1 && mn.getLength() != 0) {

			long tick = timeStampToTick(mn.getTimeStamp());
			ShortMessage mm = new ShortMessage();

			mm.setMessage(0x90, mn.getChannel(), mn.getMidiNote(), 127);
			MidiEvent me = new MidiEvent(mm, tick);

			track.add(me);

			tick = timeStampToTick(mn.getTimeStamp() + mn.getLength());
			mm = new ShortMessage();
			mm.setMessage(0x80, mn.getChannel(), mn.getMidiNote(), 127);

			me = new MidiEvent(mm, tick);
			track.add(me);
		}
	}

	@Override
	public void visit(MidiTempoEvent midiTempoEvent) throws Exception {

	}

	@Override
	public void visit(MidiSignatureEvent midiSignatureEvent) throws Exception {

	}

	@Override
	public void visit(MidiGenericEvent mge) throws Exception {

		long tick = timeStampToTick(mge.getTimeStamp());

		byte[] data = mge.getData();

		if (mge instanceof MidiMetaGenericEvent) {

			MidiMetaGenericEvent mmge = (MidiMetaGenericEvent) mge;

			MetaMessage mm = new MetaMessage();
			mm.setMessage(mmge.getType(), mmge.getData(), mmge.getData().length);

			MidiEvent me = new MidiEvent(mm, tick);
			track.add(me);

		} else if (mge instanceof MidiSysExGenericEvent) {

			MidiSysExGenericEvent msege = (MidiSysExGenericEvent) mge;
			SysexMessage mm = new SysexMessage();
			mm.setMessage(msege.getData(), msege.getData().length);

			MidiEvent me = new MidiEvent(mm, tick);
			track.add(me);

		} else if (data.length <= 3 && data.length >= 2) {

			if (data[0] == -1) {
				// unsupported event ..
				throw new Exception("unsupported event");
			} else {

				ShortMessage sm = new ShortMessage();

				int d1 = data[1];
				int d2 = 0;

				if (data.length > 2)
					d2 = data[2];

				sm.setMessage(data[0], d1, d2);
				MidiEvent me = new MidiEvent(sm, tick);
				track.add(me);
			}
		} else {

			int status = data[0] & 0xFF;

			if (status == 0xF0 || status == 0xF7) {
				SysexMessage mm = new SysexMessage();
				mm.setMessage(data, data.length);
				MidiEvent me = new MidiEvent(mm, tick);
				track.add(me);
			} else {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < data.length; i++) {
					sb.append(data[i]).append("-"); //$NON-NLS-1$
				}
				throw new Exception("unsupported generic message with length " //$NON-NLS-1$
						+ data.length + "--> " + sb.toString()); //$NON-NLS-1$
				// unsupported message
			}

			//
			// StringBuffer sb = new StringBuffer();
			// for (int i = 0; i < data.length; i++) {
			// sb.append(data[i]).append("-"); //$NON-NLS-1$
			// }
			//
			// throw new Exception(
			// "unsupported generic message with length " //$NON-NLS-1$
			// + data.length + "--> " + sb.toString()); //$NON-NLS-1$
		}
	}

	@Override
	public void visit(MidiControlChange mcc) throws Exception {
		long tick = timeStampToTick(mcc.getTimeStamp());

		ShortMessage sm = new ShortMessage();
		sm.setMessage(11 << 4, mcc.getChannel(), mcc.getControl(), mcc.getValue());

		MidiEvent me = new MidiEvent(sm, tick);
		track.add(me);
	}

	@Override
	public void visit(MidiProgramChange mcc) throws Exception {

		long tick = timeStampToTick(mcc.getTimeStamp());

		ShortMessage sm = new ShortMessage();
		sm.setMessage(12 << 4, mcc.getChannel(), mcc.getProgram(), 0);

		MidiEvent me = new MidiEvent(sm, tick);
		track.add(me);

	}

	public Sequence getSequence() {
		return seq;
	}

}
