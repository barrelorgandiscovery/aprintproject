package org.barrelorgandiscovery.extensionsng.perfo.cad.canvas;

import java.awt.geom.Path2D;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;

import org.barrelorgandiscovery.extensionsng.perfo.cad.CADExporterExtensionVirtualBook;
import org.barrelorgandiscovery.extensionsng.perfo.cad.CADVirtualBookExporter;
import org.barrelorgandiscovery.optimizers.model.CutLine;
import org.barrelorgandiscovery.optimizers.model.GroupedCutLine;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * this device draw the content of the cad into optimized object list,
 * 
 * @author pfreydiere
 *
 */
public class PunchPlanDeviceDrawing extends DeviceDrawing {

	// current drawing element
	private ArrayList<OptimizedObject> currentDraw = new ArrayList<>();

	// current constructed group
	private ArrayList<CutLine> currentGroup = new ArrayList<>();

	private double currentFractionPower = 1.0;
	private double currentFractionSpeed = 1.0;

	@Override
	public void setCurrentLayer(String layer) {
		super.setCurrentLayer(layer);

	}

	public void setCurrentFractionPower(double currentFractionPower) {
		this.currentFractionPower = currentFractionPower;
	}

	public void setCurrentFractionSpeed(double currentFractionSpeed) {
		this.currentFractionSpeed = currentFractionSpeed;
	}

	private void flushCurrent() {
		int size = this.currentGroup.size();
		switch (size) {
		case 0:
			return;
		case 1:
			currentDraw.add(currentGroup.get(0));
			break;
		default:
			GroupedCutLine g = new GroupedCutLine(currentGroup);
			currentGroup.clear();
			currentDraw.add(g);
		}
	}

	@Override
	public void startGroup() {
		super.startGroup();
		flushCurrent();
	}

	@Override
	public void endGroup() {
		super.endGroup();
		flushCurrent();
	}

	@Override
	protected void addObject(Geometry g) {

		if (!(g instanceof LineString)) {
			throw new RuntimeException("unsupported geometry " + g);
		}

		// don't export the reference arrow
		if (CADVirtualBookExporter.LAYER_REFERENCE.equals(getCurrentLayer())) {
			return;
		}

		// don't export the bords for punch machines
		if (CADVirtualBookExporter.LAYER_BORDS.equals(getCurrentLayer())) {
			return;
		}

		LineString ls = (LineString) g;
		Coordinate[] coordinates = ls.getCoordinates();

		if (coordinates.length == 0) {
			return;
		}
		assert coordinates.length > 0;

		Path2D.Double path = new Path2D.Double();
		Coordinate last = coordinates[0];
		for (int i = 1; i < coordinates.length; i++) {
			Coordinate current = coordinates[i];
			CutLine cutline = new CutLine(last.x, last.y, current.x, current.y, currentFractionPower,
					currentFractionSpeed);
			last = current;
			this.currentGroup.add(cutline);
		}
	}

	/**
	 * return the cutlines created for this drawing
	 * 
	 * @return
	 */
	public ArrayList<OptimizedObject> getCurrentDraw() {
		flushCurrent();
		return currentDraw;
	}

	@Override
	public void write(File file, String[] layers) throws Exception {
		throw new Exception("unsupported for this object");
	}

	@Override
	public void write(OutputStream outStream, String[] layers) throws Exception {
		throw new Exception("unsupported for this object");
	}

	@Override
	public boolean ignoreReference() {
		return true;
	}

}
