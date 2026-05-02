package org.barrelorgandiscovery.mcp;

import java.util.LinkedHashMap;
import java.util.Map;

import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Informations sur le livre virtuel de la fenêtre active (outil get_virtual_book_info).
 */
public final class VirtualBookFrameInfo {

	private final boolean frameActive;
	private final String inactiveMessage;
	private final Boolean hasVirtualBook;
	private final Integer holeCount;
	private final String scaleName;
	private final Integer trackCount;
	private final Long length;
	private final String instrumentName;

	private VirtualBookFrameInfo(boolean frameActive, String inactiveMessage, Boolean hasVirtualBook,
			Integer holeCount, String scaleName, Integer trackCount, Long length, String instrumentName) {
		this.frameActive = frameActive;
		this.inactiveMessage = inactiveMessage;
		this.hasVirtualBook = hasVirtualBook;
		this.holeCount = holeCount;
		this.scaleName = scaleName;
		this.trackCount = trackCount;
		this.length = length;
		this.instrumentName = instrumentName;
	}

	public static VirtualBookFrameInfo noActiveFrame() {
		return new VirtualBookFrameInfo(false, "No virtual book frame is currently active",
			null, null, null, null, null, null);
	}

	public static VirtualBookFrameInfo fromFrame(APrintNGVirtualBookFrame frame) {
		if (frame == null) {
			return noActiveFrame();
		}
		VirtualBook vb = frame.getVirtualBook();
		boolean hasVb = vb != null;
		Integer holes = hasVb ? vb.getHolesCopy().size() : null;
		String scaleName = null;
		Integer tracks = null;
		Long len = null;
		if (hasVb) {
			if (vb.getScale() != null) {
				scaleName = vb.getScale().getName();
				tracks = vb.getScale().getTrackNb();
			}
			len = vb.getLength();
		}
		String ins = frame.getCurrentInstrument() != null ? frame.getCurrentInstrument().getName() : null;
		return new VirtualBookFrameInfo(true, null, hasVb, holes, scaleName, tracks, len, ins);
	}

	public boolean isFrameActive() {
		return frameActive;
	}

	public String getInactiveMessage() {
		return inactiveMessage;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> m = new LinkedHashMap<>();
		if (!frameActive) {
			m.put("error", inactiveMessage);
			return m;
		}
		m.put("hasVirtualBook", hasVirtualBook != null && hasVirtualBook);
		if (Boolean.TRUE.equals(hasVirtualBook)) {
			if (holeCount != null) {
				m.put("holeCount", holeCount);
			}
			if (scaleName != null) {
				m.put("scale", scaleName);
			}
			if (trackCount != null) {
				m.put("trackCount", trackCount);
			}
			if (length != null) {
				m.put("length", length);
			}
		}
		if (instrumentName != null) {
			m.put("instrument", instrumentName);
		}
		return m;
	}
}
