package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.Polygon;

public class IntPoint {

	
	private int x;
	private int y;
	
	public IntPoint(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}


	public int getY() {
		return y;
	}


	public void addTo(Polygon p)
	{
		p.addPoint(x, y);
	}

	public void translate(int deltax , int deltay)
	{
		x+= deltax;
		y+= deltay;
	}
	
	
}
