package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.math.MathLine;
import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JShapeLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.SnapSelectBehaviour;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * creation tool for points in a shape layer
 * 
 * @author pfreydiere
 * 
 */
public class CreatePointForLineTool extends Tool {

	private static Logger logger = Logger.getLogger(CreatePointForLineTool.class);

	private JDisplay display;

	private JShapeLayer<Rectangle2D.Double> shapeLayer;

	private State currentState = new State();

	private int maxPointToDraw = -1; // unlimited

	private Cursor customCursor;

	private void changeState(State newState) {
		logger.debug("" + currentState.getClass().getSimpleName() + " -> " + newState.getClass().getSimpleName());
		this.currentState = newState;
	}

	private class State extends MouseAdapter implements MouseMotionListener {

		public void mouseDragged(MouseEvent e) {

		}

		public void mouseMoved(MouseEvent e) {

		}

		public void draw(Graphics2D g2d) {

		}

		public void keyPressed(KeyEvent e) {

		}

	}

	private class CursorOnLine extends State {

		private Point2D.Double projectedPoint;
		private int index;

		public CursorOnLine(Point2D.Double projectedPoint, int index) {
			this.projectedPoint = projectedPoint;
			this.index = index;
			display.repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			super.mouseReleased(e);
			try {
				Rectangle2D.Double insertedPoint = new Rectangle2D.Double(projectedPoint.x - 5, projectedPoint.y - 5,
						10, 10);

				shapeLayer.add(insertedPoint, index);

				ArrayList<Double> col = new ArrayList<Rectangle2D.Double>();
				col.add(insertedPoint);
				shapeLayer.setSelected(col);
				changeState(new MoveState(e));

			} catch (Exception ex) {
				logger.debug("cannot create point :" + ex.getMessage(), ex);
			}
		}

		@Override
		public void draw(Graphics2D g2d) {
			super.draw(g2d);

			g2d.draw(new Rectangle2D.Double(projectedPoint.getX() - 5, projectedPoint.getY() - 5, 10, 10));

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			try {
				super.mouseMoved(e);

				Point2D originPosition = display.getOriginPosition(e.getX(), e.getY());
				double pixelsize = display.getPixelSize();

				if (originPosition.distance(this.projectedPoint) > 5 * pixelsize) {
					changeState(new CreateAndSnapState());
				}

			} catch (Exception ex) {
				logger.debug("error in mousemove :" + ex.getMessage(), ex);
			}
		}

	}

	private class CreateAndSnapState extends State {

		private SnapSelectBehaviour s;

		public CreateAndSnapState() {
			this.s = new SnapSelectBehaviour(new JShapeLayer[]{shapeLayer});
		}

		@Override
		public void mousePressed(MouseEvent e) {
			Set<Double> currentSelection = shapeLayer.getSelected();
			try {
				Point2D originPosition = display.getOriginPosition(e.getX(), e.getY());
				double pixelsize = display.getPixelSize();

				// there is a selection
				if (currentSelection != null && currentSelection.size() > 0) {
					// change state

					ArrayList<Rectangle2D.Double> ret = new ArrayList<Rectangle2D.Double>();
					shapeLayer.find(originPosition.getX(), originPosition.getY(), 5 * pixelsize, ret);
					if (ret.size() > 0) {
						changeState(new MoveState(e));
						return;
					}

				}

			} catch (Exception ex) {
				logger.error("error in going to move state :" + ex.getMessage(), ex);
			}

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			try {

				if (maxPointToDraw >= 0) {
					if (shapeLayer.getGraphics().size() >= maxPointToDraw) {
						logger.debug("max point reached, don't add");
						return;
					}
				}

				Point2D pt = display.getOriginPosition(e.getX(), e.getY());

				Rectangle.Double r = new Rectangle.Double(pt.getX() - 5, pt.getY() - 5, 10, 10);

				shapeLayer.add(r);

				callSnapBehaviour(e);

			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			callSnapBehaviour(e);
		}

		/**
		 * @param e
		 */
		private void callSnapBehaviour(MouseEvent e) {
			try {

				Point2D pt = display.getOriginPosition(e.getX(), e.getY());
				if (s != null) {
					double d = display.getPixelSize();
					if (s.informMousePosition(pt.getX(), pt.getY(), 6 * d)) {
						return;
					}
				}

				Point2D originPosition = display.getOriginPosition(e.getX(), e.getY());
				double pixelsize = display.getPixelSize();

				List<Double> g = shapeLayer.getGraphics();
				if (g.size() >= 2) {
					// watch distance to line

					for (int i = 0; i < g.size() - 1; i++) {

						Rectangle2D.Double r1 = g.get(i);
						Rectangle2D.Double r2 = g.get(i + 1);

						Point2D.Double p1 = new Point2D.Double(r1.getX() + 5, r1.getY() + 5);
						Point2D.Double p2 = new Point2D.Double(r2.getX() + 5, r2.getY() + 5);

						MathLine l = new MathLine(p1, p2);

						MathVect o = l.getVecteur().orthogonal();
						Point2D.Double doubleOrigin = new Point2D.Double(originPosition.getX(), originPosition.getY());
						MathLine l2 = new MathLine(doubleOrigin, o);

						Point2D.Double intersectPoint = l.intersect(l2);
						logger.debug("intersect point " + intersectPoint);
						// distance ?

						MathVect v = new MathVect(intersectPoint, doubleOrigin);
						logger.debug("norm :" + v.norme());
						logger.debug("pixel size " + pixelsize);
						if (v.norme() < 5 * pixelsize) {

							MathVect ov = new MathVect(p1, p2);
							MathVect ot = new MathVect(p1, intersectPoint);

							if (ov.angle(ot) < 1e-3 && ov.angle(ot) >= 0 && ot.norme() < ov.norme()) {
								logger.debug("snpped on line");
								changeState(new CursorOnLine(intersectPoint, i + 1));
								return;
							}
						}
					}

				}

			} catch (Exception ex) {
				logger.error("error in mouseMoved " + ex.getMessage(), ex);
			}
		}

	}

	private class MoveState extends State {

		private Point2D origin;

		public MoveState(MouseEvent from) throws Exception {
			origin = display.getOriginPosition(from.getX(), from.getY());

		}

		@Override
		public void mouseDragged(MouseEvent e) {
			try {
				// move selection
				// show move
				Set<Double> currentSelection = shapeLayer.getSelected();

				Point2D currentPos = display.getOriginPosition(e.getX(), e.getY());

				for (Iterator<Rectangle2D.Double> iterator = currentSelection.iterator(); iterator.hasNext();) {
					Rectangle2D.Double s = iterator.next();
					s.x = s.x + (currentPos.getX() - origin.getX());
					s.y = s.y + (currentPos.getY() - origin.getY());
				}

				origin.setLocation(currentPos.getX(), currentPos.getY());

				// move selection
				shapeLayer.setSelected(currentSelection);
				
				shapeLayer.signalLayerContentChanged();

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			changeState(new CreateAndSnapState());
			display.repaint();
		}

	}

	public CreatePointForLineTool(JDisplay display, JShapeLayer<Rectangle2D.Double> shapeLayer, int maxpoints)
			throws Exception {
		this.display = display;
		this.shapeLayer = shapeLayer;
		this.maxPointToDraw = maxpoints;

		customCursor = CursorTools.createCursorWithImage(ImageTools.loadImage(getClass().getResource("kedit.png")));

	}

	public CreatePointForLineTool(JDisplay display, JShapeLayer<Rectangle2D.Double> shapeLayer) throws Exception {
		this(display, shapeLayer, -1);
	}

	@Override
	public void activated() {
		super.activated();
		changeState(new CreateAndSnapState());
		display.setCursor(customCursor);

	}

	@Override
	public void unactivated() {
		changeState(new State());
		display.setCursor(Cursor.getDefaultCursor());
		super.unactivated();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		this.currentState.mouseReleased(e);

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		this.currentState.mouseMoved(e);

	}

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		this.currentState.mousePressed(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
		this.currentState.mouseDragged(e);
	}

	@Override
	public void paintElements(Graphics g) {
		super.paintElements(g);
		this.currentState.draw((Graphics2D) g);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);

		if (shapeLayer != null && shapeLayer.getSelected() != null && e.getKeyCode() == KeyEvent.VK_DELETE) {
			for (Rectangle2D.Double s : shapeLayer.getSelected()) {
				shapeLayer.remove(s);
			}
			
			shapeLayer.setSelected(null);

			display.repaint();
		}

		this.currentState.keyPressed(e);
	}

}
