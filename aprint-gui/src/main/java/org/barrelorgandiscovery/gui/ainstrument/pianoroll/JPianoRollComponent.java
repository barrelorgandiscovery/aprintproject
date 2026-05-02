package org.barrelorgandiscovery.gui.ainstrument.pianoroll;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.gui.ainstrument.IntPoint;
import org.barrelorgandiscovery.gui.ainstrument.SelectedRange;
import org.barrelorgandiscovery.tools.MidiHelper;

/**
 * Piano component, permiting to selected notes, define continuous range notes ...
 * 
 * @author Freydiere Patrice
 */
public class JPianoRollComponent extends JComponent {

	private static final Logger logger = Logger
			.getLogger(JPianoRollComponent.class);

	/** Soft fill for mapped ranges (non-current). */
	private static final Color RANGE_FILL_IDLE = new Color(59, 130, 246, 48);
	/** Slightly stronger fill for the active mapping. */
	private static final Color RANGE_FILL_ACTIVE = new Color(37, 99, 235, 72);
	/** Subtle outline for non-focused ranges. */
	private static final Color RANGE_STROKE_IDLE = new Color(30, 64, 120, 200);
	/** Solid accent for the selected range — no dashed stroke. */
	private static final Color RANGE_STROKE_ACTIVE = new Color(217, 119, 6);
	private static final Color HANDLE_FILL = new Color(255, 253, 248);
	private static final Color HANDLE_STROKE = new Color(180, 83, 9);

	private static final float STROKE_WIDTH_IDLE = 1.25f;
	private static final float STROKE_WIDTH_ACTIVE = 2f;
	private static final int HANDLE_WIDTH = 9;
	private static final int HANDLE_HEIGHT = 14;
	private static final int HANDLE_HIT_PAD = 4;

	/**
	 * All note selected ...
	 */
	private ArrayList<SelectedRange> selections = new ArrayList<SelectedRange>();
	private HashMap<SelectedRange, Polygon> associatedShapes = new HashMap<SelectedRange, Polygon>();
	private HashMap<SelectedRange, Object> rangeClientTags = new HashMap<SelectedRange, Object>();

	private SelectedRange currentSelectedRange = null;

	private PianoRollRangeEditListener rangeEditListener;

	private enum RangeGesture {
		NONE, MOVE, RESIZE_LEFT, RESIZE_RIGHT
	}

	private RangeGesture rangeGesture = RangeGesture.NONE;
	private int gestureOrigStart;
	private int gestureOrigEnd;
	private int gestureAnchorMidi;

	private PianoRenderingNote[] notes;

	private PianoRenderingNote currentselectednote = null;

	public JPianoRollComponent() {
		notes = new PianoRenderingNote[128];
		for (int i = 0; i < notes.length; i++) {
			notes[i] = new PianoRenderingNote(i);
			if (MidiHelper.extractNoteFromMidiCode(i) == 0) {
				notes[i].setKeyOctave("" + MidiHelper.getOctave(i)); //$NON-NLS-1$
			}
		}

		notes[69].setKeyText(MidiHelper.getLocalizedMidiNote(9));

		initComponents();
	}

	private void initComponents() {

		setToolTipText(""); // activating the tooltips .. //$NON-NLS-1$
	}

	@Override
	public String getToolTipText(MouseEvent event) {

		if (logger.isDebugEnabled())
			logger.debug("get tooltip text"); //$NON-NLS-1$

		PianoRenderingNote n = searchForKey(event.getX(), event.getY());

		if (n != null) {

			String localizedMidiLibelle = MidiHelper.localizedMidiLibelle(n
					.getMidicode()) + " - " //$NON-NLS-1$
					+ new Formatter().format("%.2f Hz", MidiHelper.hertz(n //$NON-NLS-1$
							.getMidicode()));

			if (logger.isDebugEnabled())
				logger.debug("text :" + localizedMidiLibelle); //$NON-NLS-1$
			return localizedMidiLibelle;
		}
		return super.getToolTipText();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		Rectangle clipBounds = g.getClipBounds();
		clipBounds.grow(1, 1);

		paintNotes(g2d, clipBounds);

		// draw the selected ranges ...

		paintSelectedRanges(g2d, clipBounds);

		// draw the current selected range

		paintCurrentSelectedRange(g2d, clipBounds);

		// draw the current selected note

		paintCurrentNote(g2d);

	}

	protected void paintCurrentNote(Graphics g) {
		if (currentselectednote != null)
			currentselectednote.paintSelected(g);
	}

	protected void paintCurrentSelectedRange(Graphics2D g2d,
			Rectangle clipBounds) {
		Object oldHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Stroke oldStroke = g2d.getStroke();
		try {
			for (Iterator iterator = associatedShapes.entrySet().iterator(); iterator
					.hasNext();) {
				Entry<SelectedRange, Polygon> current = (Entry<SelectedRange, Polygon>) iterator
						.next();
				SelectedRange range = current.getKey();
				Polygon p = current.getValue();
				if (!p.intersects(clipBounds)) {
					continue;
				}
				boolean isCurrent = range == currentSelectedRange;
				g2d.setColor(isCurrent ? RANGE_STROKE_ACTIVE : RANGE_STROKE_IDLE);
				g2d.setStroke(new BasicStroke(
						isCurrent ? STROKE_WIDTH_ACTIVE : STROKE_WIDTH_IDLE,
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2d.drawPolygon(p);
			}

			if (currentSelectedRange != null) {
				Polygon p = associatedShapes.get(currentSelectedRange);
				if (p != null && clipBounds.intersects(p.getBounds())) {
					paintRangeHandles(g2d, currentSelectedRange);
				}
			}

		} finally {
			g2d.setStroke(oldStroke);
			if (oldHint != null) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
			}
		}
	}

	private void paintRangeHandles(Graphics2D g2d, SelectedRange range) {
		int start = range.getStart();
		int end = range.getEnd();
		PianoRenderingNote n0 = notes[start];
		PianoRenderingNote n1 = notes[end];
		int yBase = PianoRenderingNote.KEYSIZE_Y - HANDLE_HEIGHT - 3;
		int lx = n0.getLeftPos() - HANDLE_WIDTH / 2;
		int rx = n1.getRightPos() - HANDLE_WIDTH / 2;

		RoundRectangle2D leftKnob = new RoundRectangle2D.Float(lx, yBase,
				HANDLE_WIDTH, HANDLE_HEIGHT, 4, 4);
		RoundRectangle2D rightKnob = new RoundRectangle2D.Float(rx, yBase,
				HANDLE_WIDTH, HANDLE_HEIGHT, 4, 4);

		g2d.setColor(HANDLE_FILL);
		g2d.fill(leftKnob);
		g2d.fill(rightKnob);
		g2d.setColor(HANDLE_STROKE);
		g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		g2d.draw(leftKnob);
		g2d.draw(rightKnob);
	}

	protected void paintSelectedRanges(Graphics2D g2d, Rectangle clipBounds) {
		Composite oldComposite = g2d.getComposite();
		try {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					1f));

			for (Iterator iterator = associatedShapes.entrySet().iterator(); iterator
					.hasNext();) {
				Entry<SelectedRange, Polygon> current = (Entry<SelectedRange, Polygon>) iterator
						.next();

				Polygon p = current.getValue();
				if (!p.intersects(clipBounds)) {
					continue;
				}
				boolean isCurrent = current.getKey() == currentSelectedRange;
				g2d.setColor(isCurrent ? RANGE_FILL_ACTIVE : RANGE_FILL_IDLE);
				g2d.fillPolygon(p);
			}

		} finally {
			g2d.setComposite(oldComposite);
		}
	}

	protected void paintNotes(Graphics g2d, Rectangle clipBounds) {
		for (int i = 0; i < notes.length; i++) {
			PianoRenderingNote note = notes[i];
			if (note.getPolygon().getBounds().intersects(clipBounds))
				note.paint(g2d);
		}
	}

	public PianoRenderingNote searchForKey(int x, int y) {
		// if (logger.isDebugEnabled())
		// logger.debug("search rendering note at " + x + " , " + y);
		for (int i = 0; i < notes.length; i++) {
			PianoRenderingNote n = notes[i];
			if (n.getPolygon().contains(x, y)) {
				return n;
			}
		}
		return null;
	}

	public void setCurrentSelectedNote(PianoRenderingNote n) {

		if (currentselectednote != null)
			repaint(currentselectednote.getPolygon().getBounds());

		this.currentselectednote = n;

		if (currentselectednote != null)
			repaint(currentselectednote.getPolygon().getBounds());

	}

	public void setCurrentSelectedNote(int i) {
		PianoRenderingNote pianoRenderingNote = notes[i];
		setCurrentSelectedNote(pianoRenderingNote);
	}

	public PianoRenderingNote getPianoRenderingNote(int i) {
		return notes[i];
	}

	/**
	 * unactivate all the note
	 */
	public void unActivateAllNotes() {
		for (int i = 0; i < notes.length; i++) {
			notes[i].setActivated(false);
		}
	}

	/**
	 * Activate the note
	 * 
	 * @param i
	 *            the midicode to activate
	 */
	public void activateNote(int i) {
		getPianoRenderingNote(i).setActivated(true);
	}

	/**
	 * get selected note
	 */
	public PianoRenderingNote getCurrentSelectedNote() {
		return this.currentselectednote;
	}

	/**
	 * Remove selected not
	 */
	public void clearCurrentSelectedNote() {
		setCurrentSelectedNote(null);
	}

	/**
	 * Add a range to the component
	 * 
	 * @param selectedRange
	 */
	public void addRange(SelectedRange selectedRange) {
		addRange(selectedRange, null);
	}

	/**
	 * Add a range; optional client tag (e.g. {@link org.barrelorgandiscovery.instrument.sample.SoundSample})
	 * for {@link PianoRollRangeEditListener} callbacks.
	 */
	public void addRange(SelectedRange selectedRange, Object clientTag) {
		Polygon p = buildPolygonForRange(selectedRange);
		selections.add(selectedRange);
		associatedShapes.put(selectedRange, p);
		if (clientTag != null) {
			rangeClientTags.put(selectedRange, clientTag);
		}
		repaint(growBounds(p.getBounds(), 6));
	}

	private Polygon buildPolygonForRange(SelectedRange selectedRange) {
		int start = selectedRange.start;
		ArrayList<IntPoint> leftPart = notes[start].getLeftPart();
		int end = selectedRange.end;
		ArrayList<IntPoint> rightPart = notes[end].getRightPart();

		if (start == end && MidiHelper.isDiese(start)) {
			leftPart.remove(leftPart.size() - 1);
			rightPart.remove(0);
		}

		Polygon p = new Polygon();
		for (Iterator iterator = leftPart.iterator(); iterator.hasNext();) {
			IntPoint intPoint = (IntPoint) iterator.next();
			intPoint.addTo(p);
		}

		for (Iterator iterator = rightPart.iterator(); iterator.hasNext();) {
			IntPoint intPoint = (IntPoint) iterator.next();
			intPoint.addTo(p);
		}
		return p;
	}

	private static Rectangle growBounds(Rectangle r, int g) {
		Rectangle b = new Rectangle(r);
		b.grow(g, g);
		return b;
	}

	/**
	 * Rebuild geometry after programmatic or interactive bounds change.
	 */
	public void rebuildRangeShape(SelectedRange range) {
		if (range == null || !associatedShapes.containsKey(range)) {
			return;
		}
		Rectangle before = growBounds(associatedShapes.get(range).getBounds(), 8);
		Polygon p = buildPolygonForRange(range);
		associatedShapes.put(range, p);
		repaint(before.union(growBounds(p.getBounds(), 8)));
	}

	public void setRangeEditListener(PianoRollRangeEditListener listener) {
		this.rangeEditListener = listener;
	}

	public PianoRollRangeEditListener getRangeEditListener() {
		return rangeEditListener;
	}

	/**
	 * Updates hover cursor for range handles / move. Returns true if the piano roll
	 * owns the cursor for this position (caller may skip note-hover logic).
	 */
	public boolean updateRangeHoverCursor(MouseEvent e) {
		if (currentSelectedRange == null || !associatedShapes.containsKey(currentSelectedRange)) {
			setCursor(Cursor.getDefaultCursor());
			return false;
		}
		int x = e.getX();
		int y = e.getY();
		RangeGesture h = hitTestCurrentRange(x, y);
		if (h == RangeGesture.RESIZE_LEFT || h == RangeGesture.RESIZE_RIGHT) {
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			return true;
		}
		if (h == RangeGesture.MOVE) {
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return true;
		}
		setCursor(Cursor.getDefaultCursor());
		return false;
	}

	/**
	 * @return true if this press starts a range drag or resize (caller should not
	 *         start note-level mapping drag).
	 */
	public boolean beginRangeGesture(MouseEvent e) {
		if (currentSelectedRange == null || !associatedShapes.containsKey(currentSelectedRange)) {
			rangeGesture = RangeGesture.NONE;
			return false;
		}
		RangeGesture hit = hitTestCurrentRange(e.getX(), e.getY());
		if (hit == RangeGesture.NONE) {
			rangeGesture = RangeGesture.NONE;
			return false;
		}
		gestureOrigStart = currentSelectedRange.getStart();
		gestureOrigEnd = currentSelectedRange.getEnd();
		rangeGesture = hit;
		if (hit == RangeGesture.MOVE) {
			PianoRenderingNote anchor = searchForKey(e.getX(), e.getY());
			if (anchor == null) {
				anchor = findKeyAtOrNearestX(e.getX());
			}
			gestureAnchorMidi = anchor != null ? anchor.getMidicode() : gestureOrigStart;
		}
		return true;
	}

	public void continueRangeGesture(MouseEvent e) {
		if (rangeGesture == RangeGesture.NONE || currentSelectedRange == null) {
			return;
		}
		PianoRenderingNote key = findKeyAtOrNearestX(e.getX());
		if (key == null) {
			return;
		}
		int m = key.getMidicode();
		switch (rangeGesture) {
		case RESIZE_LEFT: {
			int ns = Math.min(m, currentSelectedRange.getEnd());
			ns = clampMidi(ns);
			if (ns != currentSelectedRange.getStart()) {
				currentSelectedRange.setStart(ns);
				rebuildRangeShape(currentSelectedRange);
			}
			break;
		}
		case RESIZE_RIGHT: {
			int ne = Math.max(m, currentSelectedRange.getStart());
			ne = clampMidi(ne);
			if (ne != currentSelectedRange.getEnd()) {
				currentSelectedRange.setEnd(ne);
				rebuildRangeShape(currentSelectedRange);
			}
			break;
		}
		case MOVE: {
			int delta = m - gestureAnchorMidi;
			int ns = gestureOrigStart + delta;
			int ne = gestureOrigEnd + delta;
			if (ns < 0) {
				ne -= ns;
				ns = 0;
			}
			if (ne > 127) {
				ns -= (ne - 127);
				ne = 127;
			}
			ns = clampMidi(ns);
			ne = clampMidi(ne);
			if (ns != currentSelectedRange.getStart() || ne != currentSelectedRange.getEnd()) {
				currentSelectedRange.setStart(ns);
				currentSelectedRange.setEnd(ne);
				rebuildRangeShape(currentSelectedRange);
			}
			break;
		}
		default:
			break;
		}
	}

	/**
	 * Ends drag/resize and notifies listener if bounds changed.
	 */
	public void finishRangeGesture(MouseEvent e) {
		if (rangeGesture == RangeGesture.NONE) {
			return;
		}
		try {
			if (currentSelectedRange == null) {
				return;
			}
			int s = currentSelectedRange.getStart();
			int endMidi = currentSelectedRange.getEnd();
			if (s != gestureOrigStart || endMidi != gestureOrigEnd) {
				Object tag = rangeClientTags.get(currentSelectedRange);
				if (rangeEditListener != null) {
					rangeEditListener.rangeBoundsChangeCommitted(currentSelectedRange, tag,
							s, endMidi);
				}
			}
		} finally {
			rangeGesture = RangeGesture.NONE;
		}
	}

	public boolean isRangeGestureActive() {
		return rangeGesture != RangeGesture.NONE;
	}

	private int clampMidi(int m) {
		if (m < 0) {
			return 0;
		}
		if (m > 127) {
			return 127;
		}
		return m;
	}

	private PianoRenderingNote findKeyAtOrNearestX(int x) {
		int y = PianoRenderingNote.KEYSIZE_Y / 2;
		PianoRenderingNote n = searchForKey(x, y);
		if (n != null) {
			return n;
		}
		int best = -1;
		int bestDist = Integer.MAX_VALUE;
		for (int i = 0; i < notes.length; i++) {
			Rectangle b = notes[i].getPolygon().getBounds();
			int cx = b.x + b.width / 2;
			int d = Math.abs(x - cx);
			if (d < bestDist) {
				bestDist = d;
				best = i;
			}
		}
		return best >= 0 ? notes[best] : null;
	}

	private RangeGesture hitTestCurrentRange(int x, int y) {
		Polygon poly = associatedShapes.get(currentSelectedRange);
		if (poly == null || !poly.contains(x, y)) {
			return RangeGesture.NONE;
		}
		SelectedRange range = currentSelectedRange;
		int start = range.getStart();
		int end = range.getEnd();
		int yBase = PianoRenderingNote.KEYSIZE_Y - HANDLE_HEIGHT - 3;
		int pad = HANDLE_HIT_PAD;
		int lx = notes[start].getLeftPos() - HANDLE_WIDTH / 2 - pad;
		int rx = notes[end].getRightPos() - HANDLE_WIDTH / 2 - pad;
		Rectangle leftHit = new Rectangle(lx, yBase - pad, HANDLE_WIDTH + 2 * pad,
				HANDLE_HEIGHT + 2 * pad);
		Rectangle rightHit = new Rectangle(rx, yBase - pad, HANDLE_WIDTH + 2 * pad,
				HANDLE_HEIGHT + 2 * pad);
		if (leftHit.contains(x, y)) {
			return RangeGesture.RESIZE_LEFT;
		}
		if (rightHit.contains(x, y)) {
			return RangeGesture.RESIZE_RIGHT;
		}
		return RangeGesture.MOVE;
	}

	/**
	 * Get the range items
	 * 
	 * @return
	 */
	public int getRangeCount() {
		return selections.size();
	}

	/**
	 * get the range items
	 * 
	 * @return
	 */
	public int getRangeItem() {
		return selections.size();
	}

	/**
	 * remove selected range
	 */
	public void clearSelectedRangeItem() {
		internalSetSelectedRangeItem(null);
	}

	/**
	 * define the selected range
	 * 
	 * @param index
	 */
	public void setSelectedRangeItem(int index) {
		internalSetSelectedRangeItem(selections.get(index));
	}

	/**
	 * define the selected range
	 * 
	 * @param range
	 */
	private void internalSetSelectedRangeItem(SelectedRange range) {
		if (this.currentSelectedRange != null) {
			// this.currentSelectedRange = null;
			repaint(associatedShapes.get(currentSelectedRange).getBounds());
		}

		this.currentSelectedRange = range;

		if (this.currentSelectedRange != null)
			repaint(associatedShapes.get(currentSelectedRange).getBounds());

	}

	public SelectedRange getSelectedRangeItem() {
		return this.currentSelectedRange;
	}

	public void removeSelectedRange(SelectedRange r) {
		selections.remove(r);
		rangeClientTags.remove(r);
		Polygon p = associatedShapes.get(r);
		if (p != null) {
			associatedShapes.remove(r);
			Rectangle bounds = p.getBounds();
			bounds.grow(5, 5);
			repaint(bounds);
		}
	}

	public void removeAllSelectedRange() {
		while (selections.size() > 0) {
			removeSelectedRange(selections.get(0));
		}
		rangeClientTags.clear();
	}

	// @Override
	// public int getHeight() {
	// return notes[0].getPolygon().getBounds().height + 1;
	// }
	//
	// @Override
	// public int getWidth() {
	// Rectangle n = notes[127].getPolygon().getBounds();
	// return n.x + n.width + 1;
	// }

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		JFrame f = new JFrame();
		final JPianoRollComponent pr = new JPianoRollComponent();

		class MyMouseHandler implements MouseListener, MouseMotionListener {

			public void mouseMoved(MouseEvent e) {

				PianoRenderingNote currentSelectedNote2 = pr
						.getCurrentSelectedNote();
				if (currentSelectedNote2 != null) {
					if (currentSelectedNote2.getPolygon().contains(e.getX(),
							e.getY()))
						// nothing to do ...
						return;
				}

				PianoRenderingNote n = pr.searchForKey(e.getX(), e.getY());

				pr.setCurrentSelectedNote(n);

			}

			int state = 0;
			int firstPos = -1;

			public void mouseDragged(MouseEvent e) {

				if (state == 1)
					return;

				PianoRenderingNote searchForKey = pr.searchForKey(e.getX(),
						e.getY());
				if (searchForKey == null)
					return;

				logger.debug("start " + searchForKey.getMidicode()); //$NON-NLS-1$
				state = 1;
				firstPos = searchForKey.getMidicode();

			}

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseExited(MouseEvent e) {
				logger.debug("exited"); //$NON-NLS-1$

			}

			public void mousePressed(MouseEvent e) {
				logger.debug("pressed"); //$NON-NLS-1$
			}

			public void mouseReleased(MouseEvent e) {

				logger.debug("released"); //$NON-NLS-1$
				if (state == 1) {
					logger.debug("end of "); //$NON-NLS-1$
					PianoRenderingNote searchForKey = pr.searchForKey(e.getX(),
							e.getY());
					if (searchForKey == null) {
						state = 0;
						return;
					}

					SelectedRange r = new SelectedRange(firstPos,
							searchForKey.getMidicode());
					pr.addRange(r);
					pr.setSelectedRangeItem(0);
					state = 0;
				}
			}
		}

		MyMouseHandler l = new MyMouseHandler();
		pr.addMouseMotionListener(l);
		pr.addMouseListener(l);

		JScrollPane scrollPane = new JScrollPane(pr);
		f.getContentPane().add(scrollPane);
		f.setSize(500, 200);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

	@Override
	public Dimension getPreferredSize() {
		Rectangle bounds = this.notes[127].getPolygon().getBounds();
		return new Dimension(bounds.x + bounds.width, bounds.height);
	}

}
