package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.List;

public class JLinesLayer extends JShapeLayer<Rectangle2D.Double> {

	public JLinesLayer() {
		super();
	}
	
	@Override
	public void drawLayer(Graphics2D g2d) {
		super.drawLayer(g2d);
		
		List<Double> gphscs = getGraphics();
		
		// draw line between points
		 if (gphscs != null && gphscs.size() > 1) {
			 
			 Path2D.Double l2d = new Path2D.Double();
			 for (int i = 0 ; i < gphscs.size() ; i ++ ) {
				 Rectangle2D.Double d = gphscs.get(i);
				 double x = d.getX() + 5;
				double y = d.getY() + 5;
				if (i == 0 ) {
					 l2d.moveTo(x , y);
				 } else {
					 l2d.lineTo(x, y);
				 }
			 }
			 
			 g2d.draw(l2d);
			 
		 }
		
		
	}
	

}
