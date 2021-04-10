package org.barrelorgandiscovery.recognition.gui.disks.steps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.File;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.recognition.gui.disks.DiskImageTools;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JEllipticLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools.CreatePointTool;
import org.barrelorgandiscovery.recognition.math.EllipseParameters;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;

/**
 * Layer drawing tracks with an external ellipse (passed initially), and drawing
 * an inner ellipse to adjust tracks positions
 * 
 * @author pfreydiere
 * 
 */
public class J2EllipticTracksLayer extends JEllipticLayer {

	private static Logger logger = Logger.getLogger(J2EllipticTracksLayer.class);
	
	private EllipseParameters externalEllipse;

	protected Color drawingColor = Color.red;

	private Scale scale;

	public J2EllipticTracksLayer(EllipseParameters externalEllipse) {
		super();
		this.externalEllipse = externalEllipse;
	}

	public void setScale(Scale scale) {
		this.scale = scale;
	}

	public void setExternalEllipse(EllipseParameters externalEllipse) {
		this.externalEllipse = externalEllipse;
	}
	
	public EllipseParameters getExternalEllipse() {
		return externalEllipse;
	}
	

	Path2D.Double constructEllipseFor(EllipseParameters exterior, EllipseParameters interior, double ratio) {

		// draw ellipse
		Path2D.Double p = new Path2D.Double();

		for (double angle = 0; angle < 2 * Math.PI + 1; angle += 0.05) {

			MathVect pt = DiskImageTools.interpolateBetween2Ellipse(exterior, interior, angle, ratio);

			if (angle == 0) {
				p.moveTo(pt.getX(), pt.getY());
			} else {
				p.lineTo(pt.getX(), pt.getY());
			}

		}
		return p;
	}

	@Override
	public void drawLayer(Graphics2D g2d) {
		super.drawLayer(g2d);

		Color oldc = g2d.getColor();
		try {
			
			if (externalEllipse != null) {
				g2d.setColor(Color.yellow);
				
				Path2D.Double shape = this.constructEllipseShape(externalEllipse);
				g2d.draw(shape);
			}

			// inner ellipsis is the First track
			EllipseParameters innerEllipsisParams = getCurrentEllipseParameters();
			if (innerEllipsisParams != null && scale != null && externalEllipse != null) {
				// draw the borders of tracks
				
				g2d.setColor(drawingColor);
	

				// first track low line
				double firstdistance = scale.getFirstTrackAxis() - scale.getIntertrackHeight() / 2;
				double lastDistance = scale.getWidth();
				
				EllipseParameters workingCopy = innerEllipsisParams.copy();
				
				for (int i = 0; i < scale.getTrackNb() + 1; i++) {

					double trackDistanceToCenter = firstdistance + i * scale.getIntertrackHeight();
					
					
					double ratio = (trackDistanceToCenter - firstdistance) / (lastDistance - firstdistance);
					if ( ratio  < 0) {
						logger.debug("implementation issue");
					}
					
					Path2D.Double shape = constructEllipseFor(externalEllipse, workingCopy,
							ratio);
					g2d.draw(shape);
					

				}
				g2d.setColor(Color.green);
				Path2D.Double shape = this.constructEllipseShape(workingCopy);
				g2d.draw(shape);

			}
		} finally {
			g2d.setColor(oldc);
		}

	}

	public static void main(String[] args) throws Exception {

		JFrame jFrame = new JFrame();
		jFrame.setDefaultCloseOperation(jFrame.EXIT_ON_CLOSE);
		JDisplay disp = new JDisplay();

		EllipseParameters ellipseParameters = new EllipseParameters();
		ellipseParameters.centre = new Point2D.Double(1500, 1400);
		ellipseParameters.a = 500;
		ellipseParameters.b = 200;
		ellipseParameters.angle = 0.1;

		J2EllipticTracksLayer j2EllipticTracksLayer = new J2EllipticTracksLayer(ellipseParameters);

		// set scale
		Scale scale = ScaleIO.readGamme(new File("/home/use/tmp/gamme par d_faut.scale"));
		j2EllipticTracksLayer.setScale(scale);

		disp.addLayer(j2EllipticTracksLayer);
		jFrame.getContentPane().setLayout(new BorderLayout());

		jFrame.getContentPane().add(disp, BorderLayout.CENTER);
		disp.setCurrentTool(new CreatePointTool(disp, j2EllipticTracksLayer));

		jFrame.setSize(800, 600);
		jFrame.setVisible(true);

	}

}
