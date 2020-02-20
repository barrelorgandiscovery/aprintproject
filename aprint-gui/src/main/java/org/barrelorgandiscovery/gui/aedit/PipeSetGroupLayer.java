package org.barrelorgandiscovery.gui.aedit;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractRegisterCommandDef;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.NoteDef;
import org.barrelorgandiscovery.scale.PipeStopListReference;
import org.barrelorgandiscovery.scale.RegisterSetCommandResetDef;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Position;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class PipeSetGroupLayer implements VirtualBookComponentLayer {

	private static Logger logger = Logger.getLogger(PipeSetGroupLayer.class);

	public static HashMap<String, Color> COLORS = new HashMap<String, Color>();
	static {
		COLORS.put(PipeStopListReference.REGISTERSET_ACCOMPAGNEMENT,
				Color.green);
		COLORS.put(PipeStopListReference.REGISTERSET_CHANT, Color.red);
		COLORS.put(PipeStopListReference.REGISTERSET_CHANT3, Color.cyan);
		COLORS.put(PipeStopListReference.REGISTERSET_CONTRECHAMP, Color.blue);
		COLORS.put(PipeStopListReference.REGISTERSET_BASSE, Color.DARK_GRAY);
	}
	
	public static Color DECLENCHEMENT = Color.pink;
	public static Color REGISTERS = Color.orange;

	public PipeSetGroupLayer() {

		

	}

	public void draw(Graphics g, JVirtualBookComponent jbookcomponentreference) {

		VirtualBook currentVirtualBook = jbookcomponentreference
				.getVirtualBook();
		if (currentVirtualBook == null)
			return;

		Scale scale = currentVirtualBook.getScale();
		if (scale == null)
			return;

		Rectangle clipBounds = new Rectangle(g.getClipBounds());
		clipBounds.grow(10, 10);

		Position startPosition = null;
		Position endPosition = null;

		if (clipBounds != null) {

			startPosition = jbookcomponentreference.query(clipBounds.x,
					clipBounds.y);
			endPosition = jbookcomponentreference.query(clipBounds.x
					+ clipBounds.width, clipBounds.y + clipBounds.height);

			if (startPosition == null) {
				startPosition = new Position();
				startPosition.position = jbookcomponentreference
						.MMToTime(jbookcomponentreference
								.convertScreenXToCarton(clipBounds.x));
				if (scale.isPreferredViewedInversed()) {
					startPosition.track = scale.getTrackNb() - 1;
				} else {
					startPosition.track = 0;
				}
			}

			if (endPosition == null) {
				endPosition = new Position();
				endPosition.position = jbookcomponentreference
						.MMToTime(jbookcomponentreference
								.convertScreenXToCarton(clipBounds.x
										+ clipBounds.width));
				if (scale.isPreferredViewedInversed()) {
					endPosition.track = 0;
				} else {
					endPosition.track = scale.getTrackNb() - 1;
				}
			}

		}

		Graphics2D g2d = (Graphics2D) g;
		Composite oldComposite = g2d.getComposite();
		try {

			g2d.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_ATOP, 0.3f));

			long s = Math.min(startPosition.position, endPosition.position);
			long e = Math.max(startPosition.position, endPosition.position);
			e += 100;

			for (int i = Math.min(startPosition.track, endPosition.track + 1); i < Math
					.max(startPosition.track, endPosition.track + 1); i++) {

				AbstractTrackDef[] tracksDefinition = scale
						.getTracksDefinition();
				if (i >= tracksDefinition.length)
					continue;

				AbstractTrackDef td = tracksDefinition[i];

				Color c = getColor(td);

				int y = jbookcomponentreference
						.convertCartonToScreenY(jbookcomponentreference
								.trackToMM(i));
				int h = jbookcomponentreference
						.MmToPixel(scale.getTrackWidth() / 2);

				g2d.setColor(c);
				g2d.fillRect(jbookcomponentreference
						.convertCartonToScreenX(jbookcomponentreference
								.timestampToMM(s)), y - h,
						jbookcomponentreference
								.MmToPixel(jbookcomponentreference.timeToMM(e
										- s)), 2 * h);

			}

		} finally {
			g2d.setComposite(oldComposite);
		}
	}

	public static  Color getColor(AbstractTrackDef td) {
		Color c = Color.LIGHT_GRAY;

		if (td instanceof NoteDef) {

			NoteDef d = (NoteDef) td;

			if (d.getRegisterSetName() != null) {
				if (COLORS.containsKey(d.getRegisterSetName())) {
					c = COLORS.get(d.getRegisterSetName());
				}
			}
		} else if (td instanceof AbstractRegisterCommandDef) {
			if (td instanceof RegisterSetCommandResetDef) {
				c = DECLENCHEMENT;
			} else {
				c = REGISTERS;
			}
		}
		return c;
	}

	private boolean visible = true;

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
