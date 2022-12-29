package org.barrelorgandiscovery.virtualbook;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractRegisterCommandDef;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.HashCodeUtils;
import org.barrelorgandiscovery.tools.ReadOnlySet;
import org.barrelorgandiscovery.tools.SerializeTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Virtual book data structure. This structure is used internally for
 * manipulation.
 * 
 * @author Freydiere Patrice
 */
public class VirtualBook implements Serializable, VirtualBookSectionManipulation {

	/**
	 * logger.
	 */
	private static Logger logger = Logger.getLogger(VirtualBook.class);

	/**
	 * for the serialization.
	 */
	private static final long serialVersionUID = 2406557419052686712L;

	/**
	 * Reference scale for the book.
	 */
	private Scale bookscale;

	/**
	 * holes in the book
	 */
	private ArrayList<Hole> notes = new ArrayList<Hole>();

	/**
	 * Internal treeset for much efficient finding.
	 */
	// spatial index ...
	private HoleSpatialIndex si = new HoleSpatialIndex();

	/**
	 * book name (for printing).
	 */
	private String name;

	/**
	 * associated book events events, they are always ordered.
	 */
	private SortedSet<AbstractEvent> events = new TreeSet<AbstractEvent>(new AbstractEventComparator());

	/**
	 * Metadata on the virtualbook.
	 */
	private VirtualBookMetadata metadata = null;

	/**
	 * index from tracks.
	 */
	private ArrayList<Hole>[] trackIndex = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int hash = HashCodeUtils.hash(HashCodeUtils.SEED, events);
		hash = HashCodeUtils.hash(hash, name);
		hash = HashCodeUtils.hash(hash, notes);
		hash = HashCodeUtils.hash(hash, bookscale);
		hash = HashCodeUtils.hash(hash, metadata);

		return hash;
	}

	/**
	 * default constructor.
	 * 
	 * @param scale the scale of this book
	 */
	public VirtualBook(Scale scale) {
		this.bookscale = scale;

		trackIndex = new ArrayList[scale.getTrackNb()];
		for (int i = 0; i < trackIndex.length; i++) {
			trackIndex[i] = new ArrayList<Hole>();
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param scale scale
	 * @param holes the holes for the book
	 */
	public VirtualBook(Scale scale, ArrayList<Hole> holes) {
		this(scale);
		for (int i = 0; i < holes.size(); i++) {
			Hole n = holes.get(i);
			addHole(n);
		}
	}

	/**
	 * construct a new VirtualBook with holes and events.
	 */
	public VirtualBook(Scale scale, VirtualBook copy) {
		this(scale);
		if (copy != null) {
			List<Hole> holes = copy.getOrderedHolesCopy();
			for (int i = 0; i < holes.size(); i++) {
				Hole n = holes.get(i);
				addHole(n);
			}

			Set<AbstractEvent> events = copy.getOrderedEventsByRef();
			for (Iterator iterator = events.iterator(); iterator.hasNext();) {
				AbstractEvent abstractEvent = (AbstractEvent) iterator.next();
				addEvent(abstractEvent);
			}

		}
	}

	/**
	 * Get the reference scale of the book
	 * 
	 * @return the book's scale
	 */
	public Scale getScale() {
		return bookscale;
	}

	// ///////////////////////////////////////////////////////////
	// gestion des notes
	// ///////////////////////////////////////////////////////////

	/**
	 * Add a hole in the book.
	 * 
	 * @param hole hole to add
	 */
	public void addHole(Hole hole) {

		if (hole == null) {
			return;
		}

		if (hole.getTrack() >= trackIndex.length) {
			throw new RuntimeException("for hole " + hole + " track number " + hole.getTrack() + " is out of scope ("
					+ trackIndex.length + ") for the scale :" + bookscale);
		}

		// Vérification de la note associée au carton ..
		notes.add(hole);
		// notesorderedbyend.add(n);
		si.add(hole);

		trackIndex[hole.getTrack()].add(hole);

		if (bookscale.getTracksDefinition()[hole.getTrack()] instanceof AbstractRegisterCommandDef) {
			invalidateRegistrationSections();
		}

	}

	/**
	 * Add a hole collection to the book.
	 * 
	 * @param holecollection
	 */
	public void addHole(Collection<Hole> holecollection) {

		if (holecollection == null) {
			return;
		}

		for (Hole h : holecollection) {
			addHole(h);
		}

	}

	/**
	 * add a list of holes
	 * 
	 * @param holecollection
	 */
	public void addHoles(Collection<Hole> holecollection) {
		this.addHole(holecollection);
	}

	/**
	 * Add and merge hole if some holes are overlapping
	 * 
	 * @param n
	 */
	public void addAndMerge(Hole n) {

		List<Hole> result = findHoles(n.getTimestamp(), n.getTimeLength(), n.getTrack(), n.getTrack());

		if (result.size() > 0) {
			// on fusionne toutes les notes touchant la note ...
			Hole r = new Hole(n);
			while (result.size() > 0) {
				Hole f = result.get(0);
				r = Hole.union(r, f);
				result.remove(f);
				removeHole(f);
			}
			addHole(r);
		} else {
			addHole(n);
		}
	}

	/**
	 * Cut book by the hole passed in parameter
	 * 
	 * @param n la note de decoupe
	 */
	public void cutHoles(Hole n) {
		List<Hole> recherche = findHoles(n.getTimestamp(), n.getTimeLength(), n.getTrack(), n.getTrack());

		if (!recherche.isEmpty()) {
			// on fusionne toutes les notes touchant la note ...
			while (recherche.size() > 0) {
				Hole f = recherche.get(0);

				Hole[] decoupe = Hole.minus(f, n);
				recherche.remove(f);
				removeHole(f);
				for (int i = 0; i < decoupe.length; i++) {
					addHole(decoupe[i]);
				}
			}

		}
	}

	/**
	 * Remove a hole :-)computer science priviledge :-)
	 * 
	 * @param n
	 */
	public void removeHole(Hole n) {
		notes.remove(n);
		// notesorderedbyend.remove(n);
		si.remove(n);

		trackIndex[n.getTrack()].remove(n);

		if (bookscale.getTracksDefinition()[n.getTrack()] instanceof AbstractRegisterCommandDef) {
			invalidateRegistrationSections();
		}
	}

	/**
	 * remove a list of hole
	 * 
	 * @param holes
	 */
	public void removeHoles(List<Hole> holes) {
		if (holes == null) {
			return;
		}

		for (Iterator iterator = holes.iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();
			removeHole(hole);
		}
	}

	/**
	 * This method clear all the holes on the book
	 * 
	 * @since 2011.6
	 */
	public void clear() {

		List<Hole> orderedHolesCopy = getOrderedHolesCopy();
		for (Iterator iterator = orderedHolesCopy.iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();
			removeHole(hole);
		}
	}

	/**
	 * Find all the holes in the window specified in parameters
	 * 
	 * @param start
	 * @param length
	 * @return the holes arraylist in the range
	 */
	public ArrayList<Hole> findHoles(long start, long length) {
		return si.find(start, start + length); // optimized version
	}

	/**
	 * Find all the holes from the start position
	 * 
	 * @param start
	 * 
	 * @return the holes arraylist in the range
	 */
	public ArrayList<Hole> findHoles(long start) {
		return si.find(start, Long.MAX_VALUE); // optimized version
	}

	/**
	 * Find all the holes in the window specified in parameters
	 * 
	 * @param result the list to be filled
	 * @param start
	 * @param length
	 */
	public void findHoles(long start, long length, Set<Hole> result) {
		si.find(start, start + length, result); // optimized version
	}

	/**
	 * Find all the holes in the window specified in parameters, with an additionnal
	 * filter
	 * 
	 * @param result the list to be filled
	 * @param start
	 * @param length
	 * @param f      the additional filter
	 */
	public void findHoles(long start, long length, Collection<Hole> result, HoleFilter f) {
		si.find(start, start + length, result, f);
	}

	/**
	 * Range Hole filter.
	 * 
	 * @author pfreydiere
	 *
	 */
	private static class RangeHoleFilter implements HoleFilter {

		private int start;
		private int end;

		public RangeHoleFilter(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public boolean take(Hole h) {
			if (h == null)
				return false;

			int t = h.getTrack();
			if (t < start)
				return false;

			if (t > end)
				return false;

			return true;
		}

	}

	/**
	 * Recherche toutes les notes dans la fenetre donnée de temps donnée.
	 * 
	 * @param start
	 * @param length
	 * @return
	 */
	public void findHoles(long start, long length, int firsttrack, int endtrack, Set<Hole> result) {
		if (result == null)
			return;

		int p1 = Math.min(firsttrack, endtrack);
		int p2 = Math.max(firsttrack, endtrack);

		if (p1 == p2 && p1 > 0 && p1 < bookscale.getTrackNb()) {

			Hole intersectHole = new Hole(p1, start, length);

			for (Iterator iterator = trackIndex[p1].iterator(); iterator.hasNext();) {
				Hole hole = (Hole) iterator.next();
				if (Hole.isIntersect(hole, intersectHole))
					result.add(hole);
			}

		} else {

			findHoles(start, length, result, new RangeHoleFilter(p1, p2));

		}
	}

	/**
	 * Recherche toutes les notes dans la fenetre donnée de temps donnée.
	 * 
	 * @param start
	 * @param length
	 * @return
	 */
	public void findHoles(long start, long length, int firsttrack, int endtrack, List<Hole> resultList) {

		if (resultList == null)
			return;

		final int p1 = Math.min(firsttrack, endtrack);
		final int p2 = Math.max(firsttrack, endtrack);

		findHoles(start, length, resultList, new HoleFilter() {

			public boolean take(Hole h) {
				int track = h.getTrack();
				return (track >= p1 && track <= p2);

			}
		});

	}

	/**
	 * Search holes or notes, in a given window.
	 * 
	 * @param start      the start timestamp
	 * @param length     the length of the search
	 * @param firsttrack the first track
	 * @param endtrack
	 * @return the array of holes found
	 */
	public List<Hole> findHoles(long start, long length, int firsttrack, int endtrack) {

		ArrayList<Hole> retvalue = new ArrayList<Hole>();

		findHoles(start, length, firsttrack, endtrack, retvalue);

		return retvalue;

	}

	/**
	 * Get the book length in microseconds
	 * 
	 * @return the book length in microseconds
	 */
	public long getLength() {

		long longueur = 0;

		Iterator<Hole> it = notes.iterator();
		while (it.hasNext()) {
			Hole n = it.next();
			long l = n.getTimestamp() + n.getTimeLength();
			if (l > longueur)
				longueur = l;
		}
		if (!events.isEmpty()) {
			AbstractEvent ae = events.last();
			if (ae.getTimestamp() > longueur)
				longueur = ae.getTimestamp();
		}

		return longueur;
	}

	/**
	 * Get the book length in microseconds.
	 * 
	 * @return the book length in microseconds
	 */
	public long getLength_fast() {

		return si.getLength();

	}

	/**
	 * get the first hole timestamp, if not found, Long.MAX_VALUE is returned.
	 * 
	 * @return get the first hole timestamp
	 */
	public long getFirstHoleStart() {

		long start = Long.MAX_VALUE;

		Iterator<Hole> it = notes.iterator();
		while (it.hasNext()) {
			Hole n = it.next();
			long l = n.getTimestamp();
			if (l < start)
				start = l;
		}

		return start;
	}

	public void shift(long delta) {
		shiftAt(0, delta);
	}

	/**
	 * shift the holes, that begin at start and events of a delta (in micro seconds)
	 * from the start position.
	 * 
	 * @param delta
	 */
	public void shiftAt(long start, long delta) {

		// shift the holes
		Iterator<Hole> it = this.notes.iterator();
		ArrayList<Hole> newnotes = new ArrayList<Hole>();
		while (it.hasNext()) {
			Hole n = it.next();

			if (n.getTimestamp() >= start) {
				n = new Hole(n.getTrack(), n.getTimestamp() + delta, n.getTimeLength());
			}

			newnotes.add(n);
		}

		// replace holes with shifted holes

		for (Iterator iterator = getHolesCopy().iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();
			removeHole(hole);
		}

		for (Iterator<Hole> iter = newnotes.iterator(); iter.hasNext();) {
			Hole element = iter.next();
			addHole(element);
		}

		// shift also the abstract events associated to the virtualbook
		for (Iterator iterator = events.iterator(); iterator.hasNext();) {
			AbstractEvent e = (AbstractEvent) iterator.next();
			if (e.getTimestamp() >= start)
				e.shift(delta);
		}

		// re-sort the events
		SortedSet newEvents = new TreeSet<AbstractEvent>(new AbstractEventComparator());
		newEvents.addAll(events);
		events = newEvents;

		invalidateRegistrationSections();

	}

	/**
	 * Get a copy of all the holes, sorted by timestamp.
	 * 
	 * @return get a copy array of the contained holes in the book
	 */
	public ArrayList<Hole> getOrderedHolesCopy() {

		ArrayList<Hole> retvalue = new ArrayList<Hole>();

		Hole[] holeArray = notes.toArray(new Hole[0]);
		Arrays.sort(holeArray);

		for (int i = 0; i < holeArray.length; i++) {
			Hole hole = holeArray[i];
			retvalue.add(hole);
		}
		return retvalue;
	}

	/**
	 * Get a copy of the holes.
	 * 
	 * @return
	 */
	@Deprecated
	public ArrayList<Hole> getHolesCopy() {
		return getOrderedHolesCopy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Virtual Book : \n");
		sb.append("  Scale :").append(this.getScale()).append("\n");

		Iterator<Hole> it = notes.iterator();
		while (it.hasNext()) {
			Hole n = it.next();
			sb.append("\n").append(n.toString());
		}

		sb.append("\n").append("Meta Events :\n");

		Set<AbstractEvent> se = events;

		for (Iterator iterator = se.iterator(); iterator.hasNext();) {
			AbstractEvent abstractEvent = (AbstractEvent) iterator.next();
			sb.append(abstractEvent).append("\n");
		}

		return sb.toString();
	}

	/**
	 * Get the name.
	 * 
	 * @return the name of the book
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Add an Event.
	 * 
	 * @param event
	 */
	public void addEvent(AbstractEvent event) {
		events.add(event);
	}

	/**
	 * find events of a given type from a start and end.
	 * 
	 * @param start start
	 * @param end   end
	 * @param type  event type, or null if all events are wished
	 * @return the events in the specified range
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<AbstractEvent> findEvents(long start, long end, Class<?> type) {

		if (start > end) {
			long tmp = start;
			start = end;
			end = tmp;
		}

		ArrayList<AbstractEvent> retvalue = new ArrayList<AbstractEvent>();

		for (Iterator<AbstractEvent> it = events.iterator(); it.hasNext();) {
			AbstractEvent e = it.next();

			if (e.getTimestamp() >= start && e.getTimestamp() <= end) {

				if (type != null && !type.isAssignableFrom(e.getClass()))
					continue;

				retvalue.add(e);
			}

		}

		return retvalue;
	}

	/**
	 * Find the previous event applicable from this timeStamp.
	 * 
	 * @param <T>       the event Type
	 * @param timeStamp the timeStamp
	 * @param type      the type of the event
	 * @return
	 */
	public <T extends AbstractEvent> T findPreviousEvent(long timeStamp, Class<T> type) {
		ArrayList<AbstractEvent> l = findEvents(-Long.MIN_VALUE, timeStamp, type);
		if (l == null || l.size() == 0)
			return null;

		AbstractEvent first = l.get(0);
		for (int i = 1; i < l.size(); i++) {
			AbstractEvent current = l.get(i);
			if (first.getTimestamp() <= current.getTimestamp()) {
				first = current;
			}
		}

		return (T) first;
	}

	/**
	 * Find the next event of the same type.
	 * 
	 * @param <T> the event Type
	 * @param ae  the abstract event
	 * @return
	 */
	public <T extends AbstractEvent> T findNextEvent(T ae) {

		if (!events.contains(ae))
			throw new IllegalArgumentException("the event is not in the virtualbook");

		SortedSet<AbstractEvent> tailSet = events.tailSet(ae);
		if (tailSet.size() <= 1)
			return null;

		Iterator<AbstractEvent> it = tailSet.iterator();

		// read the first
		AbstractEvent current = it.next();
		assert current == ae;

		for (; it.hasNext();) {
			AbstractEvent abstractEvent = (AbstractEvent) it.next();
			if (ae.getClass().isAssignableFrom(abstractEvent.getClass())) {
				return (T) abstractEvent;
			}
		}

		return null;
	}

	/**
	 * get the events, ordered by timestamp [READ ONLY].
	 */
	public Set<AbstractEvent> getOrderedEventsByRef() {
		return new ReadOnlySet<AbstractEvent>(this.events);
	}

	/**
	 * find the first event of a given type from the start point.
	 * 
	 * @param start start point of the search
	 * @param type  event type
	 * @return the first event
	 */
	@SuppressWarnings("unchecked")
	private AbstractEvent findFirstEvent(long start, Class type) {

		AbstractEvent current = null;

		for (Iterator<AbstractEvent> it = events.iterator(); it.hasNext();) {
			AbstractEvent e = it.next();

			if (e.getTimestamp() > start)
				return current; // fin

			if (type.isAssignableFrom(e.getClass()))
				current = e;
		}
		return current;
	}

	/**
	 * remove an event.
	 * 
	 * @param event
	 */
	public void removeEvent(AbstractEvent event) {
		events.remove(event);
	}

	// ///////////////////////////////////////////////////////////////////////////
	// methodes associées à la registration

	/**
	 * Propriété mémorisant la liste des sections associées.
	 */
	private RegistrationSection[] rsections = null;

	private void invalidateRegistrationSections() {
		logger.debug("invalidate sections");
		rsections = null;
	}

	/**
	 * Function that get the Registration Sections, if the registration has been
	 * invalidated then it regenerate it.
	 * 
	 * @return
	 */
	private RegistrationSection[] getOrComputeRegistrationSections() {

		if (rsections != null) {
			return rsections;
		}

		logger.debug("compute section parsing ...");
		// calcul des sections de registres ...

		RegisterSectionParsing rp = new RegisterSectionParsing(this);
		try {
			ArrayList<RegistrationSection> newregsection = new ArrayList<RegistrationSection>();
			newregsection = rp.parse();
			rsections = newregsection.toArray(new RegistrationSection[0]);

			if (logger.isDebugEnabled()) {
				for (RegistrationSection tmp : rsections) {
					logger.debug("Sections parsed :" + tmp.getStart());
				}
			}

			return rsections;
		} catch (Exception ex) {
			throw new IllegalStateException("parsing error", ex);
		}
	}

	/**
	 * get the section count.
	 * 
	 * @return the number of registers section computed in the book
	 */
	public int getSectionCount() {

		RegistrationSection[] rsect = getOrComputeRegistrationSections();
		if (rsect == null)
			return 0;
		return rsect.length;
	}

	/**
	 * get the section start of a section
	 * 
	 * @param section
	 * @return
	 */
	public long getSectionStart(int section) {
		return getOrComputeRegistrationSections()[section].getStart();
	}

	/**
	 * get the activated registers in the section.
	 * 
	 * @param section the section index
	 * @return the activated registers in the section
	 */
	public String[] getSectionRegisters(int section) {

		if (section < 0)
			return new String[0];

		RegistrationSection registrationSection = getOrComputeRegistrationSections()[section];

		Vector<String> v = new Vector<String>();

		for (PipeStopGroup rs : bookscale.getPipeStopGroupListRef()) {
			try {
				String[] registersInRegisterset = registrationSection.getRegisters(rs.getName());
				for (int i = 0; i < registersInRegisterset.length; i++) {
					v.add(rs.getName() + "-" + registersInRegisterset[i]);
				}
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
		return v.toArray(new String[0]);
	}

	/**
	 * Get the section from the timestamp
	 * 
	 * @param timestamp
	 * @return
	 */
	public int findSection(long timestamp) {
		int index = -1;

		long last = -1;

		for (int i = 0; i < getSectionCount(); i++) {
			if (getSectionStart(i) < timestamp && last < getSectionStart(i)) {
				index = i;
				last = getSectionStart(i);
			}
		}

		return index;

	}

	/**
	 * 
	 * Create a new Virtual Book where all overlapped holes are merged
	 * 
	 * @return
	 * 
	 */
	public VirtualBook flattenVirtualBook() {

		VirtualBook vb = new VirtualBook(bookscale);

		List<Hole> orderedHolesCopy = getOrderedHolesCopy();

		for (Iterator iterator = orderedHolesCopy.iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();
			vb.addAndMerge(hole);
		}

		vb.setName(name);
		vb.setMetadata(metadata);

		return vb;
	}

	/**
	 * get virtual book metadata object
	 * 
	 * @param metadata
	 */
	public void setMetadata(VirtualBookMetadata metadata) {
		this.metadata = metadata;
		if (metadata != null) {
			setName(metadata.getName());
		}
	}

	/**
	 * set virtual book metadata object
	 * 
	 * @return
	 */
	public VirtualBookMetadata getMetadata() {
		return metadata;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.virtualbook.VirtualBookSectionManipulation#
	 * listMarkers()
	 */
	public List<MarkerEvent> listMarkers() {
		ArrayList<AbstractEvent> evts = findEvents(0, getLength(), MarkerEvent.class);
		ArrayList<MarkerEvent> mkevents = new ArrayList<MarkerEvent>();
		for (Iterator iterator = evts.iterator(); iterator.hasNext();) {
			AbstractEvent ev = (AbstractEvent) iterator.next();
			mkevents.add((MarkerEvent) ev);
		}
		return mkevents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.virtualbook.VirtualBookSectionManipulation#
	 * getMarkerLength(org.barrelorgandiscovery.virtualbook.MarkerEvent)
	 */
	public long getMarkerLength(MarkerEvent marker) {

		MarkerEvent nextMarker = findNextEvent(marker);
		if (nextMarker == null) {
			logger.debug("no next marker, we go to the end of the book");
			return getLength() - marker.getTimestamp();
		}

		// return the length between the next marker and the current one
		return nextMarker.getTimestamp() - marker.getTimestamp();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.virtualbook.VirtualBookSectionManipulation#
	 * selectMarker(org.barrelorgandiscovery.virtualbook.MarkerEvent)
	 */
	public Fragment selectMarker(MarkerEvent marker) {
		return new Fragment(marker.getTimestamp(), getMarkerLength(marker));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.virtualbook.VirtualBookSectionManipulation#
	 * selectMarkers(org.barrelorgandiscovery.virtualbook.MarkerEvent,
	 * org.barrelorgandiscovery.virtualbook.MarkerEvent)
	 */
	public Fragment selectMarkers(MarkerEvent from, MarkerEvent to) {

		assert from != null;
		assert to != null;

		// swap markers if not in the proper order
		if (from.getTimestamp() > to.getTimestamp()) {
			// swap
			MarkerEvent tmp = from;
			from = to;
			to = tmp;
		}

		assert from.getTimestamp() <= to.getTimestamp();

		return new Fragment(from.getTimestamp(), to.getTimestamp() - from.getTimestamp());

	}

	private static class SelectionContent {
		ArrayList<Hole> holes;
		ArrayList<AbstractEvent> events;
	}

	/**
	 * Select the holes belonging to the selection, hole that start and the end of
	 * the selection are not taken into consideration
	 * 
	 * @param sel
	 * @return
	 */
	protected SelectionContent getSelectionContent(Fragment sel) {
		assert sel != null;

		ArrayList<Hole> holes = findHoles(sel.start, sel.length - 1);
		// exclude the holes starting at the end (we exclude the end)

		int cpt = 0;
		while (cpt < holes.size()) {
			Hole hole = holes.get(cpt);
			if (hole.getTimeLength() == (sel.start + sel.length)) {
				holes.remove(cpt);
			} else {
				cpt++;
			}
		}

		// clone the events in selection
		ArrayList<AbstractEvent> eventsToAdd = (ArrayList<AbstractEvent>) SerializeTools
				.deepClone(findEvents(sel.start, sel.start + sel.length - 1, null));

		SelectionContent sc = new SelectionContent();
		sc.holes = holes;
		sc.events = eventsToAdd;

		return sc;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.virtualbook.VirtualBookSectionManipulation#
	 * insertAt (org.barrelorgandiscovery.virtualbook.Selection, long)
	 */
	public void insertAt(Fragment selection, long atPosition) {
		assert selection != null;

		SelectionContent sc = getSelectionContent(selection);

		// shift the book
		shiftAt(atPosition, selection.length);

		// add elements

		long offset = atPosition - selection.start;

		for (Iterator iterator = sc.holes.iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();
			addHole(hole.newHoleWithOffset(offset));
		}

		// copy associated events

		for (Iterator iterator = sc.events.iterator(); iterator.hasNext();) {
			AbstractEvent abstractEvent = (AbstractEvent) iterator.next();
			abstractEvent.shift(offset);
			events.add(abstractEvent);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.virtualbook.VirtualBookSectionManipulation#
	 * removeSelection(org.barrelorgandiscovery.virtualbook.Selection)
	 */
	public void removeFragment(Fragment selection) {

		SelectionContent sc = getSelectionContent(selection);

		for (Iterator iterator = sc.holes.iterator(); iterator.hasNext();) {
			Hole hole = (Hole) iterator.next();
			removeHole(hole);
		}

		for (Iterator iterator = sc.events.iterator(); iterator.hasNext();) {
			AbstractEvent ae = (AbstractEvent) iterator.next();
			removeEvent(ae);
		}

		shiftAt(selection.start + selection.length, -selection.length);
	}

	/**
	 * Clone the content of the virtual book
	 */
	public VirtualBook clone() {

		VirtualBookMetadata m = getMetadata();
		setMetadata(null);

		VirtualBook copy = (VirtualBook) SerializeTools.deepClone(this);
		setMetadata(m);
		copy.setMetadata(m);
		return copy;
	}

}
