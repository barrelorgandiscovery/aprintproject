package org.barrelorgandiscovery.optimizers.cad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import org.barrelorgandiscovery.extensionsng.perfo.cad.CADParameters;
import org.barrelorgandiscovery.extensionsng.perfo.cad.CADVirtualBookExporter;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.PunchPlanDeviceDrawing;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.optimizers.Optimizer;
import org.barrelorgandiscovery.optimizers.OptimizerProgress;
import org.barrelorgandiscovery.optimizers.OptimizerResult;
import org.barrelorgandiscovery.optimizers.model.Extent;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class XOptim implements Optimizer<OptimizedObject> {

	private XOptimParameters parameters = new XOptimParameters();

	public XOptim() {

	}

	public XOptim(XOptimParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public String getTitle() {
		return "Simple Lazer Optimizer";
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public Object getDefaultParameters() {
		return parameters;
	}

	CADParameters constructCADParameters() {
		CADParameters parameters = new CADParameters();
		parameters.setExportDecoupeDesBords(false);
		parameters.setExportPliures(this.parameters.isExportPliures());
		parameters.setPasDePontSiIlReste(this.parameters.getPasDePontSiIlReste());
		parameters.setNombreDePlisAAjouterAuDebut(this.parameters.getNombreDePlisAAjouterAuDebut());
		parameters.setPont(this.parameters.getPont());
		parameters.setTaillePagePourPliure(this.parameters.getTaillePagePourPliure());
		parameters.setTailleTrous(this.parameters.getTailleTrous());
		parameters.setTypePliure(this.parameters.getTypePliure());
		parameters.setTypeTrous(this.parameters.getTypeTrous());
		parameters.setTypePonts(this.parameters.getTypePonts());
		return parameters;
	}

	@Override
	public OptimizerResult<OptimizedObject> optimize(VirtualBook carton) throws Exception {

		CADParameters parameters = constructCADParameters();

		CADVirtualBookExporter exporter = new CADVirtualBookExporter();

		PunchPlanDeviceDrawing pp = new PunchPlanDeviceDrawing();
		exporter.export(carton, parameters, pp);

		ArrayList<OptimizedObject> optimized = pp.getCurrentDraw();

		OptimizerResult<OptimizedObject> result = new OptimizerResult<>();
		result.result = optimized.toArray(new OptimizedObject[optimized.size()]);

		// sort the elements by x
		Arrays.sort(result.result, new Comparator<OptimizedObject>() {
			@Override
			public int compare(OptimizedObject arg0, OptimizedObject arg1) {
				assert arg0 != null;
				assert arg1 != null;

				Extent e = arg0.getExtent();
				Extent e2 = arg1.getExtent();
				int c = Double.compare(e.xmin, e2.xmin);
				if (c != 0)
					return c;
				return Double.compare(e.ymin, e2.ymin);
			}
		});

		return result;
	}

	@Override
	public OptimizerResult<OptimizedObject> optimize(VirtualBook carton, OptimizerProgress progress, ICancelTracker ct)
			throws Exception {
		return optimize(carton);
	}

}
