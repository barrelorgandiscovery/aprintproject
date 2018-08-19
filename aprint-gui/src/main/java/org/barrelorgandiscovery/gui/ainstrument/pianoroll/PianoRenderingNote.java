package org.barrelorgandiscovery.gui.ainstrument.pianoroll;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Iterator;

import org.barrelorgandiscovery.gui.ainstrument.IntPoint;
import org.barrelorgandiscovery.tools.MidiHelper;

public class PianoRenderingNote {

	private Polygon polygon;

	private int halfkeysizex = 8;
	private int keysizey = 95;
	private int halfblackkeysizex = 5;
	private int blackkeysizey = 58;
	private int midicode;

	private Color color = Color.white;

	private Color selectedColor = Color.orange;

	private Color outlineColor = Color.DARK_GRAY;

	private int shiftx;

	private Color textColor;

	private boolean activated = true; // for rendering in grey ...

	public PianoRenderingNote(int midicode) {

		this.midicode = midicode;

		if (MidiHelper.isDiese(midicode)) {
			this.polygon = constructBlackKey();
			this.color = Color.BLACK;
			this.outlineColor = Color.BLACK;
			textColor = Color.WHITE;
		} else {
			textColor = Color.BLACK;
			boolean isleftdiese = isLeftDiese(midicode);

			boolean isrightdiese = isRightDiese(midicode);

			this.polygon = constructWhiteKey(isleftdiese, isrightdiese);
		}

		shiftx = computeShift();

		polygon.translate(shiftx, 0);

	}

	private int computeShift() {

		int octaves = midicode / 12;
		int space = octaves * 14 * halfkeysizex;

		int n = midicode % 12;
		if (n > 4)
			n++;

		int shiftx = space + (n + 1) * halfkeysizex;
		return shiftx;
	}

	private boolean isRightDiese(int midicode) {
		boolean isrightdiese = false;
		if (midicode < 127 && MidiHelper.isDiese(midicode + 1))
			isrightdiese = true;
		return isrightdiese;
	}

	private boolean isLeftDiese(int midicode) {
		boolean isleftdiese = false;
		if (midicode > 0 && MidiHelper.isDiese(midicode - 1))
			isleftdiese = true;
		return isleftdiese;
	}

	/**
	 * Construct the drawing of a white key
	 * 
	 * @param leftdiese
	 *            is left key a diese ?
	 * @param rightdiese
	 *            is right key a diese ?
	 * @return
	 */
	private Polygon constructWhiteKey(boolean leftdiese, boolean rightdiese) {

		Polygon p = new Polygon();

		ArrayList<IntPoint> ap = new ArrayList<IntPoint>();
		addLeftPartWhiteKey(leftdiese, ap);

		addRightPartWhiteKey(rightdiese, ap);

		for (Iterator iterator = ap.iterator(); iterator.hasNext();) {
			IntPoint intPoint = (IntPoint) iterator.next();
			intPoint.addTo(p);
		}

		return p;

	}

	private void addRightPartWhiteKey(boolean rightdiese, ArrayList<IntPoint> p) {

		p.add(new IntPoint(halfkeysizex, keysizey));
		p.add(new IntPoint(halfkeysizex, blackkeysizey));

		int right = halfkeysizex - (rightdiese ? halfblackkeysizex : 0);
		p.add(new IntPoint(right, blackkeysizey));
		p.add(new IntPoint(right, 0));
		p.add(new IntPoint(0, 0));
	}

	private void addLeftPartWhiteKey(boolean leftdiese, ArrayList<IntPoint> p) {
		p.add(new IntPoint(0, 0));
		int left = -halfkeysizex + (leftdiese ? halfblackkeysizex : 0);
		p.add(new IntPoint(left, 0));
		p.add(new IntPoint(left, blackkeysizey));
		p.add(new IntPoint(-halfkeysizex, blackkeysizey));
		p.add(new IntPoint(-halfkeysizex, keysizey));
		p.add(new IntPoint(0, keysizey)); // bas ...
	}

	/**
	 * Construct a black key
	 * 
	 * @return
	 */
	private Polygon constructBlackKey() {
		Polygon p = new Polygon();

		ArrayList<IntPoint> ip = new ArrayList<IntPoint>();
		addLeftPartBlackNote(ip);
		addRightPartBlackNote(ip);

		for (Iterator iterator = ip.iterator(); iterator.hasNext();) {
			IntPoint intPoint = (IntPoint) iterator.next();
			intPoint.addTo(p);
		}

		return p;
	}

	private void addRightPartBlackNote(ArrayList<IntPoint> p) {
		p.add(new IntPoint(0, blackkeysizey));
		p.add(new IntPoint(halfblackkeysizex, blackkeysizey));
		p.add(new IntPoint(halfblackkeysizex, 0));
		p.add(new IntPoint(-halfblackkeysizex, 0));
	}

	private void addLeftPartBlackNote(ArrayList<IntPoint> p) {
		p.add(new IntPoint(-halfblackkeysizex, 0));
		p.add(new IntPoint(-halfblackkeysizex, blackkeysizey));
		p.add(new IntPoint(0, blackkeysizey));
	}

	/**
	 * Get shape geometry for the not polygon
	 * 
	 * @return
	 */
	public Shape getPolygon() {
		return polygon;
	}

	private GradientPaint lazyBlackGradientPaint = null;
	private GradientPaint lazyWhiteGradientPaint = null;

	/**
	 * Paint the note
	 * 
	 * @param g
	 */
	public void paint(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;

		int basex;
		int basey;

		if (isBlack()) {

			if (activated) {

				GradientPaint p = lazyBlackGradientPaint;

				if (p == null)
					p = lazyBlackGradientPaint = new GradientPaint(
							(float) polygon.getBounds2D().getMinX(), 0,
							Color.LIGHT_GRAY, (float) polygon.getBounds2D()
									.getMinX() + halfblackkeysizex, 0, color,
							false);

				g2d.setPaint(p);
			}

			basex = -halfblackkeysizex;
			basey = blackkeysizey;

		} else {

			if (activated) {

				g.setColor(color);
				GradientPaint p = lazyWhiteGradientPaint;
				if (p == null)
					p = lazyWhiteGradientPaint = new GradientPaint(
							(float) polygon.getBounds2D().getMinX(), 0,
							Color.LIGHT_GRAY, (float) polygon.getBounds2D()
									.getMinX(), 10, color, false);

				g2d.setPaint(p);
			} else {
				g2d.setColor(Color.GRAY);
			}

			basex = -halfkeysizex;
			basey = keysizey;

		}

		g.fillPolygon(polygon);

		g.setColor(outlineColor);
		g.drawPolygon(polygon);

		if (this.text != null) {
			basex += shiftx + 1;
			basey -= 1;
			g.drawString(text, basex, basey);
		}

		if (this.keyOctave != null) {
			basex += shiftx + 1;
			basey -= keysizey - 15;
			g.drawString(keyOctave, basex, basey);
		}

	}

	/**
	 * Paint the note in selected
	 * 
	 * @param g
	 */
	public void paintSelected(Graphics g) {
		g.setColor(selectedColor);
		g.fillPolygon(polygon);

		g.setColor(Color.DARK_GRAY);
		g.drawPolygon(polygon);
	}

	/**
	 * Get the midi code associated to the key note
	 * 
	 * @return
	 */
	public int getMidicode() {
		return midicode;
	}

	/**
	 * Left geometry part for the piano Rendering
	 * 
	 * @return
	 */
	public ArrayList<IntPoint> getLeftPart() {

		ArrayList<IntPoint> retvalue = new ArrayList<IntPoint>();
		if (isBlack()) {
			addLeftPartBlackNote(retvalue);
			retvalue.add(new IntPoint(0, keysizey));
		} else {

			addLeftPartWhiteKey(isLeftDiese(midicode), retvalue);
		}

		shiftArrayPoint(retvalue);

		return retvalue;
	}

	/**
	 * get the left hightlight point
	 * 
	 * @return
	 */
	public int getLeftPos() {

		int x;

		if (isBlack()) {
			x = -halfblackkeysizex;

		} else {
			x = -halfkeysizex + (isLeftDiese(midicode) ? halfblackkeysizex : 0);

		}

		return x + computeShift();
	}

	/**
	 * Right position
	 * 
	 * @return
	 */
	public int getRightPos() {

		int x;

		if (isBlack()) {
			x = +halfblackkeysizex;

		} else {
			x = halfkeysizex - (isRightDiese(midicode) ? halfblackkeysizex : 0);
		}

		return x + computeShift();
	}

	private boolean isBlack() {
		return MidiHelper.isDiese(midicode);
	}

	public ArrayList<IntPoint> getRightPart() {

		ArrayList<IntPoint> retvalue = new ArrayList<IntPoint>();
		if (isBlack()) {
			retvalue.add(new IntPoint(0, keysizey));
			addRightPartBlackNote(retvalue);
		} else {

			addRightPartWhiteKey(isRightDiese(midicode), retvalue);
		}

		shiftArrayPoint(retvalue);

		return retvalue;
	}

	private void shiftArrayPoint(ArrayList<IntPoint> ap) {
		int xshift = computeShift();

		for (Iterator iterator = ap.iterator(); iterator.hasNext();) {
			IntPoint intPoint = (IntPoint) iterator.next();
			intPoint.translate(xshift, 0);
		}
	}

	public void setBackgroundColor(Color color) {
		this.color = color;
	}

	public Color getBackgroundColor() {
		return color;
	}

	private String text = null;

	public String getKeyText() {
		return this.text;
	}

	public void setKeyText(String text) {
		this.text = text;
	}

	private String keyOctave = null;

	public String getKeyOctave() {
		return keyOctave;
	}

	public void setKeyOctave(String keyOctave) {
		this.keyOctave = keyOctave;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

	public void setSelectedColor(Color selectedColor) {
		this.selectedColor = selectedColor;
	}

}
