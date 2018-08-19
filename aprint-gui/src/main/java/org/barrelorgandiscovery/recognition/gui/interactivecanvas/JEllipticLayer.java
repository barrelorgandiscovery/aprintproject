package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.barrelorgandiscovery.recognition.math.EllipseParameters;
import org.barrelorgandiscovery.recognition.math.EllipticRegression;

import Jama.Matrix;

/**
 * Layer drawing an ellipse
 * 
 * @author pfreydiere
 * 
 */
public class JEllipticLayer extends JShapeLayer<Rectangle2D.Double> {

	private EllipseParameters ellipse;

	protected Color drawingColor = Color.yellow;

	public JEllipticLayer() {

		Runnable compute = new Runnable() {

			public void run() {

				List<Double> j = lastJob.getAndSet(null);
				if (j != null) {

					final EllipseParameters e = EllipticRegression
							.iterativeRegression(j, null);
					// once the regression is OK, prevent the
					// swing UI thread
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							JEllipticLayer.this.ellipse = e;
							// cause the redraw
							JEllipticLayer.super.fireLayerContentChanged();
						}
					});
				}
			}
		};

		async.scheduleAtFixedRate(compute, 100, 100, TimeUnit.MILLISECONDS);

	}

	public void setEllipseDrawingColor(Color newEllipseColor) {
		this.drawingColor = newEllipseColor;
	}

	@Override
	public void drawLayer(Graphics2D g2d) {
		super.drawLayer(g2d);

		Color oldColor = g2d.getColor();
		try {
			g2d.setColor(drawingColor);
			if (ellipse != null) {

				Path2D.Double ellipseShape = constructEllipseShape(ellipse);

				g2d.draw(ellipseShape);

				Path2D.Double centerDraw = new Path2D.Double();
				int size = 30;

				// cross
				centerDraw.moveTo(ellipse.centre.x, ellipse.centre.y - size);
				centerDraw.lineTo(ellipse.centre.x, ellipse.centre.y + size);

				g2d.draw(centerDraw);

				centerDraw = new Path2D.Double();
				centerDraw.moveTo(ellipse.centre.x - size, ellipse.centre.y);
				centerDraw.lineTo(ellipse.centre.x + size, ellipse.centre.y);

				g2d.draw(centerDraw);

			}

		} finally {
			g2d.setColor(oldColor);
		}

	}

	protected Path2D.Double constructEllipseShape(EllipseParameters ellipse) {
		// draw ellipse
		Path2D.Double p = new Path2D.Double();

		Matrix rotationMatrix = new Matrix(new double[][] {
				{ Math.cos(ellipse.angle), -Math.sin(ellipse.angle) },
				{ Math.sin(ellipse.angle), Math.cos(ellipse.angle) }

		});

		Matrix center = new Matrix(new double[][] { { ellipse.centre.x,
				ellipse.centre.y, } });

		for (double angle = 0; angle < 2 * Math.PI + 1; angle += 0.05) {

			Matrix vpos = new Matrix(
					new double[][] { { ellipse.a * Math.cos(angle),
							ellipse.b * Math.sin(angle) } });

			Matrix r = rotationMatrix.times(vpos.transpose());

			Matrix res = r.plus(center.transpose());

			if (angle == 0) {
				p.moveTo(res.get(0, 0), res.get(1, 0));
			} else {
				p.lineTo(res.get(0, 0), res.get(1, 0));
			}

		}
		return p;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.recognition.gui.interactivecanvas.JLayer#
	 * fireLayerContentChanged()
	 */
	@Override
	protected void fireLayerContentChanged() {

		List<Double> g = getGraphics();
		
		
		if (g != null && g.size() >= 5) {

			// this.ellipse = EllipticRegression.iterativeRegression(g,
			// ellipse);

			
			asyncComputeEllipse(g);

		} else {
			this.ellipse = null;
		}

		super.fireLayerContentChanged();
	}

	private ScheduledExecutorService async = Executors
			.newScheduledThreadPool(1);

	private AtomicReference<List<Double>> lastJob = new AtomicReference<List<Double>>(
			null);

	// in swing thread
	private void asyncComputeEllipse(List<Double> pointList) {

		// copy list
		List<Double> lcopy = new ArrayList<Rectangle2D.Double>();
		lcopy.addAll(pointList);
		lastJob.set(lcopy);

	}

	public EllipseParameters getCurrentEllipseParameters() {
		return ellipse;
	}

	public void setEllipseParameters(EllipseParameters ellipseParameters) {
		this.ellipse = ellipseParameters;
		fireLayerContentChanged();
	}

}
