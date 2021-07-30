package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JLinesLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JShapeLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.SnapSelectBehaviour;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * creation tool for moving horizontal lines
 * 
 * @author pfreydiere
 * 
 */
public class MoveTool extends Tool {

	private static Logger logger = Logger.getLogger(MoveTool.class);

	private JDisplay display;

	private JShapeLayer<Rectangle2D.Double>[] shapeLayer;

	private State currentState = new State();

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

	private class SnapState extends State {

		private SnapSelectBehaviour s;

		public SnapState() {

			this.s = new SnapSelectBehaviour(shapeLayer);
		}

		@Override
		public void mousePressed(MouseEvent e) {

			for (int i = 0; i < shapeLayer.length; i++) {
				Set<Double> currentSelection = shapeLayer[i].getSelected();
				try {
					Point2D originPosition = display.getOriginPosition(e.getX(), e.getY());
					double pixelsize = display.getPixelSize();

					// there is a selection
					if (currentSelection != null && currentSelection.size() > 0) {
						// change state

						ArrayList<Rectangle2D.Double> ret = new ArrayList<Rectangle2D.Double>();
						shapeLayer[i].find(originPosition.getX(), originPosition.getY(), 5 * pixelsize, ret);
						if (ret.size() > 0) {
							changeState(new MoveState(e, i));
							return;
						}

					}

				} catch (Exception ex) {
					logger.error("error in going to move state :" + ex.getMessage(), ex);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			try {

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
						// snapped on points, don't go further
						return;
					}
				}

			} catch (Exception ex) {
				logger.error("error in mouseMoved " + ex.getMessage(), ex);
			}
		}

	}

	private class MoveState extends State {

		private Point2D origin;
		private int shapeLayerIndex;

		public MoveState(MouseEvent from, int shapeLayerIndex) throws Exception {
			origin = display.getOriginPosition(from.getX(), from.getY());
			this.shapeLayerIndex = shapeLayerIndex;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			try {
				// move selection
				// show move

				Set<Double> currentSelection = new HashSet(shapeLayer[shapeLayerIndex].getGraphics());

				Point2D currentPos = display.getOriginPosition(e.getX(), e.getY());

				for (Iterator<Rectangle2D.Double> iterator = currentSelection.iterator(); iterator.hasNext();) {
					Rectangle2D.Double s = iterator.next();
					s.x = s.x + (currentPos.getX() - origin.getX());
					s.y = s.y + (currentPos.getY() - origin.getY());
				}

				origin.setLocation(currentPos.getX(), currentPos.getY());

				// move selection
				shapeLayer[shapeLayerIndex].setSelected(currentSelection);

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			changeState(new SnapState());
			display.repaint();
		}

	}

	public MoveTool(JDisplay display, JShapeLayer<Rectangle2D.Double>[] shapeLayer) throws Exception {
		this.display = display;
		this.shapeLayer = shapeLayer;

		customCursor = CursorTools.createCursorWithImage(ImageTools.loadImage(getClass().getResource("kedit.png")));

	}

	@Override
	public void activated() {
		super.activated();
		changeState(new SnapState());
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

		for (int i = 0; i < shapeLayer.length; i++) {
			Set<Double> selected = shapeLayer[i].getSelected();
			if (shapeLayer[i] != null && selected != null) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					for (Rectangle2D.Double s : selected) {
						shapeLayer[i].remove(s);
					}

					shapeLayer[i].setSelected(null);
				} else {
					// move by key
						

					      double xadder = 0.0;
					      double yadder = 0.0;

					      if (e.getKeyCode() == KeyEvent.VK_UP) {
					        yadder -= 1.0;
					      } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					        yadder += 1.0;
					      } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					        xadder -= 1.0;
					      } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					        xadder += 1.0;
					      }
						
					      double fxadder = xadder;
					      double fyadder = yadder;

					      // move shapes
					      selected.forEach( (r) -> {
					    	  r.x += fxadder;
					    	  r.y += fyadder; 
					      });
						
						
					
				} // else
			}
		}
		display.repaint();
		this.currentState.keyPressed(e);
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// test method

	public static void main(String[] args) throws Exception {

		JFrame f = new JFrame();
		f.setSize(800, 600);

		JDisplay disp = new JDisplay();
		f.getContentPane().setLayout(new BorderLayout());

		f.getContentPane().add(disp, BorderLayout.CENTER);

		JShapeLayer<Rectangle2D.Double> sl = new JLinesLayer();
		sl.add(new Rectangle2D.Double(10, 10, 10, 10));
		sl.add(new Rectangle2D.Double(100, 10, 10, 10));
		disp.addLayer(sl);

		JShapeLayer<Rectangle2D.Double>[] arrayOfShapeLayer = new JShapeLayer[] { sl };

		disp.setCurrentTool(new MoveTool(disp, arrayOfShapeLayer));

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

}
