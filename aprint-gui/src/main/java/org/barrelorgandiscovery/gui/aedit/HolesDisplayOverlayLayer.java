package org.barrelorgandiscovery.gui.aedit;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * Layer for visualizing background recognition image and associated holes
 * 
 * @author use
 * 
 */
public class HolesDisplayOverlayLayer implements VirtualBookComponentBackgroundLayer {

	/**
	 * the layer is visible ?
	 */
	private boolean visible = true;
	/**
	 * offset of the image and holes
	 */
	private double xoffset = 0;

	private double xscale = 1.0d;

	/**
	 * flip the image display
	 */
	private boolean flipHorizontallyTheImage = false;

	private boolean disableRescale = false;

	/**
	 * specific holes drawing color, red by default
	 */
	private Color holesColor = Color.red;

	private Stroke holesStroke = null;

	private String layerInternalName = null;

	public void setLayerInternalName(String layerInternalName) {
		this.layerInternalName = layerInternalName;
	}

	public String getLayerInternalName() {
		return layerInternalName;
	}

	public void setHolesColor(Color holesColor) {
		if (holesColor != null)
			this.holesColor = holesColor;
	}

	public Color getHolesColor() {
		return holesColor;
	}

	public void setHolesStroke(Stroke holesStroke) {
		this.holesStroke = holesStroke;
	}

	public Stroke getHolesStroke() {
		return holesStroke;
	}

	public void setDisableRescale(boolean disableRescale) {
		this.disableRescale = disableRescale;
	}

	public boolean isDisableRescale() {
		return disableRescale;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	private float opacity = 1.0f;

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public float getOpacity() {
		return opacity;
	}

	/**
	 * Construct affine transformation
	 * 
	 * @param component
	 * @param dimensionOnBook
	 * @param dimensionInOrigin
	 * @return
	 */
	private AffineTransform constructAffineTransformForDisplay(JVirtualBookComponent component, double dimensionOnBook,
			double dimensionInOrigin) {
		AffineTransform t = AffineTransform.getScaleInstance(1, 1);

		if (flipHorizontallyTheImage) {
			// invert the image
			AffineTransform scaleTransform = AffineTransform.getScaleInstance(1, -1);
			AffineTransform translateInstance = AffineTransform.getTranslateInstance(0, dimensionInOrigin);

			translateInstance.concatenate(scaleTransform);
			t.concatenate(translateInstance);
		}

		Toolkit kit = Toolkit.getDefaultToolkit();
		double screendpi = kit.getScreenResolution();
		double factor = 1.0 / component.getXfactor() * (screendpi * 1.0d) / 25.4;

		AffineTransform display = AffineTransform.getScaleInstance(factor, factor);
		display.concatenate(t);

		AffineTransform shift = AffineTransform.getTranslateInstance(component.convertCartonToScreenXDecimal(xoffset),
				component.convertCartonToScreenY(0));
		shift.concatenate(display);
		return shift;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.gui.aedit.VirtualBookComponentLayer#draw(java.
	 * awt.Graphics, org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent)
	 */
	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {
		if (!isVisible())
			return;

		VirtualBook vb = jbookcomponentreference.getVirtualBook();
		if (vb == null)
			return;

		ArrayList<Hole> holes = vb.getHolesCopy();
		if (holes.isEmpty())
			return;

		Scale scale = vb.getScale();

		g.setColor(holesColor);
		g.setPaintMode();

		Graphics2D g2d = (Graphics2D) g;

		Stroke stroke = new BasicStroke(2.0f);
		if (this.holesStroke != null) {
			stroke = this.holesStroke;
		}
		g2d.setStroke(stroke);

		AffineTransform a = constructAffineTransformForDisplay(jbookcomponentreference, scale.getWidth(),
				scale.getWidth());
		AffineTransform oldTransform = g2d.getTransform();
		try {
			a.preConcatenate(oldTransform);
			g2d.setTransform(a);

			for (Iterator<Hole> iterator = holes.iterator(); iterator.hasNext();) {
				Hole h = iterator.next();

				double xmm = xoffset + jbookcomponentreference.timestampToMM(h.getTimestamp()) * xscale;
				double widthmm = jbookcomponentreference.timeToMM(h.getTimeLength()) * xscale;

				double y = scale.getFirstTrackAxis() + scale.getIntertrackHeight() * h.getTrack();

				if (scale.isPreferredViewedInversed())
					y = scale.getWidth() - y;

				y -= scale.getTrackWidth() / 2;

				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, this.opacity));

				Rectangle2D r = new Rectangle2D.Double(xmm, y, widthmm, scale.getTrackWidth());
				g2d.draw(r);
			}

			g2d.setPaintMode();

		} finally {
			g2d.setTransform(oldTransform);
		}

	}

	public void setXoffset(double xoffset) {
		this.xoffset = xoffset;
	}

	/**
	 * Image Offset in mm
	 * 
	 * @return
	 */
	public double getXoffset() {
		return xoffset;
	}

	public void setXscale(double xscale) {
		this.xscale = xscale;
	}

	public double getXscale() {
		return xscale;
	}

	public void setFlipHorizontallyTheImage(boolean flipHorizontallyTheImage) {
		this.flipHorizontallyTheImage = flipHorizontallyTheImage;
	}

	public boolean isFlipHorizontallyTheImage() {
		return flipHorizontallyTheImage;
	}

	private AtomicReference<int[]> currentVisibleTiles = new AtomicReference<int[]>(new int[0]);

	public int[] getCurrentVisibleTiles() {
		int[] c = currentVisibleTiles.get();
		assert c != null;
		return c;
	}

	public static void main(String[] args) throws Exception {

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(600, 800);

		f.getContentPane().setLayout(new BorderLayout());

		JVirtualBookScrollableComponent s = new JVirtualBookScrollableComponent();
		f.getContentPane().add(s, BorderLayout.CENTER);

		VirtualBook vb = new VirtualBook(Scale.getGammeMidiInstance());
		s.setVirtualBook(vb);

		HolesDisplayOverlayLayer l = new HolesDisplayOverlayLayer();
		s.addLayer(l);

		f.setVisible(true);

		s.fitToScreen();

	}

	@Override
	public void drawBackground(Graphics g, JVirtualBookComponent component) {

	}

}
