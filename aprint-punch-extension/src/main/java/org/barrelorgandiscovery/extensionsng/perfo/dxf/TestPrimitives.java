package org.barrelorgandiscovery.extensionsng.perfo.dxf;

import org.barrelorgandiscovery.extensionsng.perfo.dxf.canvas.DXFDeviceDrawing;

import com.vividsolutions.jts.geom.Coordinate;

public class TestPrimitives {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DXFDeviceDrawing d = new DXFDeviceDrawing();

		d.drawArrondi(new Coordinate(0,5), new Coordinate(0,0), 2);
		
		
	}

}
