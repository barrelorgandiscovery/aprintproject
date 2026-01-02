package org.barrelorgandiscovery.mcp.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PercussionDef;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Context object that pre-processes a VirtualBook to provide efficient access
 * to musical data for feature extraction.
 * 
 * This is similar to MIDIIntermediateRepresentations in jSymbolic, but works
 * directly with VirtualBook data structures.
 * 
 * @author APrint Development Team
 */
public class VirtualBookFeatureContext {
	
	private final VirtualBook virtualBook;
	
	// Pre-computed data
	private final List<Hole> orderedHoles;
	private final int totalHoleCount;
	private final long totalLength; // in microseconds
	
	// Note information (for pitched tracks)
	private final List<NoteInfo> allNotes;
	private final Map<Integer, List<NoteInfo>> notesByTrack;
	private final Map<Integer, List<NoteInfo>> notesByPitch;
	
	// Pitch statistics
	private final int minPitch;
	private final int maxPitch;
	private final int pitchRange;
	
	// Duration statistics (in seconds)
	private final List<Double> noteDurations;
	private final double averageNoteDuration;
	private final double minNoteDuration;
	private final double maxNoteDuration;
	
	// Time between attacks (in seconds)
	private final List<Double> timeBetweenAttacks;
	private final double averageTimeBetweenAttacks;
	
	// Track usage
	private final Map<Integer, Integer> notesPerTrack;
	
	/**
	 * Information about a single note/hole for feature extraction.
	 */
	public static class NoteInfo {
		private final int track;
		private final long timestamp; // microseconds
		private final long length; // microseconds
		private final int midiPitch; // -1 if percussion or unknown
		private final boolean isPercussion;
		private final double durationSeconds;
		
		public NoteInfo(int track, long timestamp, long length, int midiPitch, boolean isPercussion) {
			this.track = track;
			this.timestamp = timestamp;
			this.length = length;
			this.midiPitch = midiPitch;
			this.isPercussion = isPercussion;
			this.durationSeconds = length / 1_000_000.0;
		}
		
		public int getTrack() { return track; }
		public long getTimestamp() { return timestamp; }
		public long getLength() { return length; }
		public int getMidiPitch() { return midiPitch; }
		public boolean isPercussion() { return isPercussion; }
		public double getDurationSeconds() { return durationSeconds; }
		public long getEndTimestamp() { return timestamp + length; }
	}
	
	/**
	 * Create a feature context from a VirtualBook.
	 */
	public VirtualBookFeatureContext(VirtualBook virtualBook) {
		this.virtualBook = virtualBook;
		
		// Get ordered holes
		this.orderedHoles = virtualBook.getOrderedHolesCopy();
		this.totalHoleCount = orderedHoles.size();
		this.totalLength = virtualBook.getLength();
		
		// Process holes into note information
		this.allNotes = new ArrayList<>();
		this.notesByTrack = new HashMap<>();
		this.notesByPitch = new HashMap<>();
		this.notesPerTrack = new HashMap<>();
		this.noteDurations = new ArrayList<>();
		this.timeBetweenAttacks = new ArrayList<>();
		
		AbstractTrackDef[] trackDefs = virtualBook.getScale().getTracksDefinition();
		
		int minP = 127;
		int maxP = 0;
		boolean hasPitch = false;
		
		// Process each hole
		for (Hole hole : orderedHoles) {
			int track = hole.getTrack();
			if (track >= trackDefs.length) continue;
			
			AbstractTrackDef trackDef = trackDefs[track];
			int midiPitch = -1;
			boolean isPercussion = false;
			
			if (trackDef instanceof NoteDef) {
				NoteDef noteDef = (NoteDef) trackDef;
				midiPitch = noteDef.getMidiNote();
				hasPitch = true;
				minP = Math.min(minP, midiPitch);
				maxP = Math.max(maxP, midiPitch);
			} else if (trackDef instanceof PercussionDef) {
				isPercussion = true;
			}
			
			NoteInfo noteInfo = new NoteInfo(track, hole.getTimestamp(), hole.getTimeLength(), 
				midiPitch, isPercussion);
			
			allNotes.add(noteInfo);
			
			// Index by track
			notesByTrack.computeIfAbsent(track, k -> new ArrayList<>()).add(noteInfo);
			
			// Index by pitch (only for pitched notes)
			if (midiPitch >= 0) {
				notesByPitch.computeIfAbsent(midiPitch, k -> new ArrayList<>()).add(noteInfo);
			}
			
			// Track usage
			notesPerTrack.put(track, notesPerTrack.getOrDefault(track, 0) + 1);
			
			// Duration in seconds
			double durationSec = hole.getTimeLength() / 1_000_000.0;
			noteDurations.add(durationSec);
		}
		
		this.minPitch = hasPitch ? minP : 0;
		this.maxPitch = hasPitch ? maxP : 0;
		this.pitchRange = maxPitch - minPitch;
		
		// Calculate duration statistics
		if (!noteDurations.isEmpty()) {
			this.averageNoteDuration = noteDurations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
			this.minNoteDuration = noteDurations.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
			this.maxNoteDuration = noteDurations.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
		} else {
			this.averageNoteDuration = 0.0;
			this.minNoteDuration = 0.0;
			this.maxNoteDuration = 0.0;
		}
		
		// Calculate time between attacks (time between consecutive note starts on same track)
		// This measures the interval between when notes start playing
		for (Map.Entry<Integer, List<NoteInfo>> entry : notesByTrack.entrySet()) {
			List<NoteInfo> trackNotes = entry.getValue();
			// Sort by timestamp
			trackNotes.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
			
			for (int i = 1; i < trackNotes.size(); i++) {
				// Time between attacks = time from previous note start to current note start
				long timeDiff = trackNotes.get(i).getTimestamp() - trackNotes.get(i-1).getTimestamp();
				if (timeDiff > 0) {
					// Convert from microseconds to seconds
					timeBetweenAttacks.add(timeDiff / 1_000_000.0);
				}
			}
		}
		
		if (!timeBetweenAttacks.isEmpty()) {
			this.averageTimeBetweenAttacks = timeBetweenAttacks.stream()
				.mapToDouble(Double::doubleValue).average().orElse(0.0);
		} else {
			this.averageTimeBetweenAttacks = 0.0;
		}
	}
	
	// Getters
	public VirtualBook getVirtualBook() { return virtualBook; }
	public List<Hole> getOrderedHoles() { return Collections.unmodifiableList(orderedHoles); }
	public int getTotalHoleCount() { return totalHoleCount; }
	public long getTotalLength() { return totalLength; }
	public double getTotalLengthSeconds() { return totalLength / 1_000_000.0; }
	
	public List<NoteInfo> getAllNotes() { return Collections.unmodifiableList(allNotes); }
	public Map<Integer, List<NoteInfo>> getNotesByTrack() { return Collections.unmodifiableMap(notesByTrack); }
	public Map<Integer, List<NoteInfo>> getNotesByPitch() { return Collections.unmodifiableMap(notesByPitch); }
	
	public int getMinPitch() { return minPitch; }
	public int getMaxPitch() { return maxPitch; }
	public int getPitchRange() { return pitchRange; }
	
	public List<Double> getNoteDurations() { return Collections.unmodifiableList(noteDurations); }
	public double getAverageNoteDuration() { return averageNoteDuration; }
	public double getMinNoteDuration() { return minNoteDuration; }
	public double getMaxNoteDuration() { return maxNoteDuration; }
	
	public List<Double> getTimeBetweenAttacks() { return Collections.unmodifiableList(timeBetweenAttacks); }
	public double getAverageTimeBetweenAttacks() { return averageTimeBetweenAttacks; }
	
	public Map<Integer, Integer> getNotesPerTrack() { return Collections.unmodifiableMap(notesPerTrack); }
	
	/**
	 * Get notes for a specific track.
	 */
	public List<NoteInfo> getNotesForTrack(int track) {
		return notesByTrack.getOrDefault(track, Collections.emptyList());
	}
	
	/**
	 * Get notes for a specific pitch.
	 */
	public List<NoteInfo> getNotesForPitch(int pitch) {
		return notesByPitch.getOrDefault(pitch, Collections.emptyList());
	}
	
	/**
	 * Get the number of unique pitches used.
	 */
	public int getUniquePitchCount() {
		return notesByPitch.size();
	}
	
	/**
	 * Get the number of active tracks (tracks with at least one note).
	 */
	public int getActiveTrackCount() {
		return notesByTrack.size();
	}
}

