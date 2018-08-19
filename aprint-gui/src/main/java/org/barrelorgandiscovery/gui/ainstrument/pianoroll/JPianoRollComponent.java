package org.barrelorgandiscovery.gui.ainstrument.pianoroll;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
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

	/**
	 * All note selected ...
	 */
	private ArrayList<SelectedRange> selections = new ArrayList<SelectedRange>();
	private HashMap<SelectedRange, Polygon> associatedShapes = new HashMap<SelectedRange, Polygon>();

	private SelectedRange currentSelectedRange = null;

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
		g2d.setColor(Color.BLUE);
		Stroke old = g2d.getStroke();
		try {
			g2d.setStroke(new BasicStroke(3f));

			for (Iterator iterator = associatedShapes.entrySet().iterator(); iterator
					.hasNext();) {
				Entry<SelectedRange, Polygon> current = (Entry<SelectedRange, Polygon>) iterator
						.next();
				Polygon p = current.getValue();
				if (p.intersects(clipBounds)) {
					g2d.drawPolygon(p);
				}
			}
			if (currentSelectedRange != null) {
				Stroke s = new BasicStroke(5f, BasicStroke.CAP_SQUARE,
						BasicStroke.JOIN_MITER, 10f, new float[] { 10f, 10f },
						0f);
				g2d.setStroke(s);
				g2d.setColor(Color.ORANGE);
				Polygon p = associatedShapes.get(currentSelectedRange);
				if (clipBounds.intersects(p.getBounds()))

					g2d.drawPolygon(p);

			}

		} finally {
			g2d.setStroke(old);
		}
	}

	protected void paintSelectedRanges(Graphics2D g2d, Rectangle clipBounds) {
		Composite old_composite = g2d.getComposite();
		try {

			g2d.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_ATOP, 0.5f));

			for (Iterator iterator = associatedShapes.entrySet().iterator(); iterator
					.hasNext();) {
				Entry<SelectedRange, Polygon> current = (Entry<SelectedRange, Polygon>) iterator
						.next();

				Polygon p = current.getValue();
				if (p.intersects(clipBounds)) {
					g2d.setColor(Color.CYAN);
					g2d.fillPolygon(p);
				}
			}

		} finally {
			g2d.setComposite(old_composite);
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

		selections.add(selectedRange);

		associatedShapes.put(selectedRange, p);

		repaint(p.getBounds());
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
