package org.barrelorgandiscovery.gui.aedit;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.gui.ainstrument.SBRegistersPlay;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.Position;
import org.barrelorgandiscovery.virtualbook.VirtualBook;


public class PlayTool extends Tool {

	private static Logger logger = Logger.getLogger(PlayTool.class);

	private SBRegistersPlay p;
	private JVirtualBookScrollableComponent c;

	public PlayTool(SBRegistersPlay p, JVirtualBookScrollableComponent c) {
		this.p = p;
		this.c = c;
	}

	@Override
	public void mousePressed(MouseEvent e) {

		Position p = c.query(e.getX(), e.getY());
		if (p == null)
			return;

		VirtualBook vb = c.getVirtualBook();
		int findSection = vb.findSection(p.position);

		logger.debug("section found :" + findSection);

		String[] registers = null;
		if (findSection >= 0) {
			registers = vb.getSectionRegisters(findSection);
			if (logger.isDebugEnabled()) {
				for (String r : registers) {
					logger.debug("Register found in this section :" + r);
				}
			}
		}

		if (registers == null) {
			registers = new String[] { EditableInstrument.DEFAULT_PIPESTOPGROUPNAME };
		} else {
			String[] tmp = new String[registers.length + 1];
			System.arraycopy(registers, 0, tmp, 0, registers.length);
			tmp[registers.length] = EditableInstrument.DEFAULT_PIPESTOPGROUPNAME;

			registers = tmp;
		}

		ArrayList<Hole> findHoles = vb.findHoles(p.position, 0);
		HashSet<Integer> hs = new HashSet<Integer>();

		for (Hole h : findHoles) {
			hs.add(h.getTrack());
		}

		int[] tracks = new int[hs.size()];

		Integer[] elems = hs.toArray(new Integer[0]);

		for (int i = 0; i < elems.length; i++) {
			tracks[i] = elems[i];
		}

		this.p.setCurrentRegisterGroupRegister(registers);
		this.p.playTracks(tracks);

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		p.stopNote();
	}

}
