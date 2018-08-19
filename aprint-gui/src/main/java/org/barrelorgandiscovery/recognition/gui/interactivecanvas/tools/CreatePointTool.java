package org.barrelorgandiscovery.recognition.gui.interactivecanvas.tools;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.tools.CursorTools;
import org.barrelorgandiscovery.math.MathVect;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JDisplay;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.JShapeLayer;
import org.barrelorgandiscovery.recognition.gui.interactivecanvas.SnapSelectBehaviour;
import org.barrelorgandiscovery.tools.ImageTools;

/**
 * creation tool for points in a shape layer
 *
 * @author pfreydiere
 */
public class CreatePointTool extends Tool {

  private static Logger logger = Logger.getLogger(CreatePointTool.class);

  private JDisplay display;

  private JShapeLayer<Rectangle2D.Double> shapeLayer;

  private State currentState = new State();

  private int maxPointToDraw = -1; // unlimited

  private Cursor customCursor;

  private void changeState(State newState) {
    logger.debug(
        ""
            + currentState.getClass().getSimpleName()
            + " -> "
            + newState.getClass().getSimpleName());
    this.currentState = newState;
  }

  private class State extends MouseAdapter implements MouseMotionListener {

    public void mouseDragged(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {}

    public void keyUp(KeyEvent e) {}
  }

  private class CreateAndSnapState extends State {

    private SnapSelectBehaviour s;

    public CreateAndSnapState() {
      this.s = new SnapSelectBehaviour(new JShapeLayer[] {shapeLayer});
    }

    @Override
    public void mousePressed(MouseEvent e) {
      Set<Double> currentSelection = shapeLayer.getSelected();
      if (currentSelection != null && currentSelection.size() > 0) {
        // change state
        try {

          double d = display.getPixelSize();
          Point2D originPosition = display.getOriginPosition(e.getX(), e.getY());
          ArrayList<Rectangle2D.Double> ret = new ArrayList<Rectangle2D.Double>();
          shapeLayer.find(originPosition.getX(), originPosition.getY(), 5 * d, ret);
          if (ret.size() > 0) {
            changeState(new MoveState(e));
          }
        } catch (Exception ex) {
          logger.error("error in going to move state :" + ex.getMessage(), ex);
        }
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

    /** @param e */
    private void callSnapBehaviour(MouseEvent e) {
      try {

        Point2D pt = display.getOriginPosition(e.getX(), e.getY());
        if (s != null) {
          double d = display.getPixelSize();
          s.informMousePosition(pt.getX(), pt.getY(), 5 * d);
        }

      } catch (Exception ex) {
        logger.error("error in mouseMoved " + ex.getMessage(), ex);
      }
    }

    @Override
    public void keyUp(KeyEvent e) {

      double xadder = 0.0;
      double yadder = 0.0;

      Set<Double> currentSelection = shapeLayer.getSelected();
      if (currentSelection == null || currentSelection.size() == 0) {
        return;
      }

      if (e.getKeyCode() == KeyEvent.VK_UP) {
        yadder -= 1.0;
      } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
        yadder += 1.0;
      } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
        xadder -= 1.0;
      } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
        xadder += 1.0;
      }

      for (Iterator<Rectangle2D.Double> iterator = currentSelection.iterator();
          iterator.hasNext();
          ) {
        Rectangle2D.Double s = iterator.next();
        s.x = s.x + (xadder);
        s.y = s.y + (yadder);
      }

      shapeLayer.setSelected(currentSelection);

      if (anchorPointAdjuster != null) {
        anchorPointAdjuster.adjust(
            shapeLayer.getGraphics(), currentSelection, new MathVect(xadder, yadder));
      }

      shapeLayer.signalLayerContentChanged();
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

        movePosition(currentSelection, currentPos);

      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    }

    private void movePosition(Set<Double> currentSelection, Point2D currentPos) {

      MathVect displacement =
          new MathVect(currentPos.getX() - origin.getX(), currentPos.getY() - origin.getY());
      for (Iterator<Rectangle2D.Double> iterator = currentSelection.iterator();
          iterator.hasNext();
          ) {
        Rectangle2D.Double s = iterator.next();
        s.x = s.x + (displacement.getX());
        s.y = s.y + (displacement.getY());
      }

      origin.setLocation(currentPos.getX(), currentPos.getY());

      // move selection

      // logger.debug("move ....");

      shapeLayer.setSelected(currentSelection);

      if (anchorPointAdjuster != null) {
        anchorPointAdjuster.adjust(shapeLayer.getGraphics(), currentSelection, displacement);
      }

      shapeLayer.signalLayerContentChanged();

      // add it to the layer !!
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      super.mouseReleased(e);
      changeState(new CreateAndSnapState());
      display.repaint();
    }
  }

  public CreatePointTool(
      JDisplay display, JShapeLayer<Rectangle2D.Double> shapeLayer, int maxpoints)
      throws Exception {
    this.display = display;
    this.shapeLayer = shapeLayer;
    this.maxPointToDraw = maxpoints;

    customCursor =
        CursorTools.createCursorWithImage(
            ImageTools.loadImage(getClass().getResource("kedit.png")));
  }

  public CreatePointTool(JDisplay display, JShapeLayer<Rectangle2D.Double> shapeLayer)
      throws Exception {
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
  public void keyReleased(KeyEvent e) {
    super.keyReleased(e);
    this.currentState.keyUp(e);
  }

  IAnchorPointAdjuster anchorPointAdjuster;

  public void setAnchorPointAdjuster(IAnchorPointAdjuster anchorPointAdjuster) {
    this.anchorPointAdjuster = anchorPointAdjuster;
  }
}
