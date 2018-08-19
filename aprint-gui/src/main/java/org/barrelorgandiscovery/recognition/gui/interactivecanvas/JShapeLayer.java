package org.barrelorgandiscovery.recognition.gui.interactivecanvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Layer for displaying java 2D shapes
 * @author pfreydiere
 *
 * @param <T>
 */
public class JShapeLayer<T extends Shape> extends JLayer {

	// Graphics selection

	private List<T> graphics = new ArrayList<T>();

	protected IShapeDrawer graphicsDrawer;
	
	protected Color selectedColor = Color.BLUE;
	

	@Override
	public String getName() {
		return null;
	}
	
	public List<T> getGraphics()
	{
		return Collections.unmodifiableList(graphics);
	}

	@Override
	public void drawLayer(Graphics2D g2d) {

		IShapeDrawer defaultDrawer = new IShapeDrawer() {
			public void draw(Shape s, Graphics2D g2d) {
				g2d.draw(s);
			}
		};

		// draw layers
		for (Iterator<T> iterator = graphics.iterator(); iterator.hasNext();) {
			Shape e = iterator.next();
			if (e != null) {

				IShapeDrawer d = graphicsDrawer;
				if (d == null)
					d = defaultDrawer;

				if (d != null) {
					d.draw(e, g2d);
				} else {
					g2d.draw(e);
				}
			}
		}

		// draw selected
		if (selected != null && selected.size() > 0) {

			Color oldColor = g2d.getColor();
			try {
				
				g2d.setColor(selectedColor);
				for (Shape selectedShape : selected) {
					IShapeDrawer d = selectedDrawer;
					if (d == null)
						d = defaultDrawer;

					if (selectedShape != null)
						if (d != null) {
							d.draw(selectedShape, g2d);
						} else {
							g2d.draw(selectedShape);
						}
				}
			} finally {
				g2d.setColor(oldColor);
			}
		}

	}

	/**
	 * add graphic
	 * @param shape
	 */
	public void add(T shape) {
		if (shape != null) {
			graphics.add(shape);
			fireLayerContentChanged();
		}
	}
	
	public void addAll(Collection<T> shape) {
		if (shape != null) {
			graphics.addAll(shape);
			fireLayerContentChanged();
		}
	}
	
	public void add(T shape, int index) {
		if (shape != null) {
			graphics.add(index, shape);
			fireLayerContentChanged();
		}
	}
	
	public void signalLayerContentChanged() {
		fireLayerContentChanged();
	}
	
	
	public void remove(T shape) {
		graphics.remove(shape);
		fireLayerContentChanged();
	}
	
	public void removeAll() {
		graphics.clear();
	}
	
	public void clear() {
		removeAll();
	}
	

	public void removeLayer(String name) {
		graphics.remove(name);
	}

	protected void setLayerGraphicDrawer(IShapeDrawer drawer) {
		this.graphicsDrawer = drawer;
	}

	private IShapeDrawer selectedDrawer;

	protected void setSelectedDrawer(IShapeDrawer selectedDrawer) {
		this.selectedDrawer = selectedDrawer;
	}

	public interface IShapeDrawer {
		void draw(Shape s, Graphics2D g2d);
	}

	public void find(double x, double y, double tolerance, Collection<T> result) {
		if (result == null)
			return;

		Rectangle2D.Double f = new Rectangle2D.Double(x - tolerance, y
				- tolerance, 2 * tolerance, 2 * tolerance);

		for (T shape : graphics) {
			if (shape != null) {
				if (shape.intersects(f)) {
					result.add(shape);
				}
			}
		}
	}

	private Set<T> selected = null;

	public void setSelected(Collection<T> s) {
		if (s == null) {
			selected = null;
			return;
		}
		// else
		selected = new HashSet<T>(s);
		fireLayerSelectionChanged();
	}

	public Set<T> getSelected() {
		if (selected == null)
			return null;
		return Collections.unmodifiableSet(selected);
	}

	@Override
	public Rectangle2D getExtent() {
		
		Rectangle2D r = null;
		
		for (T s : graphics) {
			
			if (r == null)
			{
				r = s.getBounds2D();
			} else 
			{
				Rectangle2D.union(s.getBounds2D(), r, r);
			}
			
		}
		
		return r;
	}
	
	public void setGraphicsDrawer(IShapeDrawer graphicsDrawer) {
		this.graphicsDrawer = graphicsDrawer;
	}
	
	public IShapeDrawer getGraphicsDrawer() {
		return graphicsDrawer;
	}
	
}
