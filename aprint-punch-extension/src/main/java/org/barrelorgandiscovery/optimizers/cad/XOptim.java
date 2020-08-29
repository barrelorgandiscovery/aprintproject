package org.barrelorgandiscovery.optimizers.cad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;

import org.barrelorgandiscovery.extensionsng.perfo.cad.CADParameters;
import org.barrelorgandiscovery.extensionsng.perfo.cad.CADVirtualBookExporter;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.PunchPlanDeviceDrawing;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.optimizers.Optimizer;
import org.barrelorgandiscovery.optimizers.OptimizerProgress;
import org.barrelorgandiscovery.optimizers.OptimizerResult;
import org.barrelorgandiscovery.optimizers.model.CutLine;
import org.barrelorgandiscovery.optimizers.model.Extent;
import org.barrelorgandiscovery.optimizers.model.GroupedCutLine;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.tools.SerializeTools;
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

	/**
	 * because we use the export function to convert the book to cad
	 * this function creates the associated CADParameters 
	 * @return
	 */
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

		CADParameters cadparameters = constructCADParameters();
		CADVirtualBookExporter exporter = new CADVirtualBookExporter();

		// create a punch plan device drawing
		PunchPlanDeviceDrawing pp = new PunchPlanDeviceDrawing();
		exporter.export(carton, cadparameters, pp);

		ArrayList<OptimizedObject> optimized = pp.getCurrentDraw();

		// exported elements have a 1.0 power and speed fraction
		OptimizerResult<OptimizedObject> result = new OptimizerResult<>();

		result.result = optimized.toArray(new OptimizedObject[optimized.size()]);

		// sort the elements by x, either groups and cut lines
		Arrays.sort(result.result, new Comparator<OptimizedObject>() {
			@Override
			public int compare(OptimizedObject arg0, OptimizedObject arg1) {
				assert arg0 != null;
				assert arg1 != null;

				Extent e = arg0.getExtent();
				Extent e2 = arg1.getExtent();
				int c = Double.compare((e.xmin + e.xmax) / 2, (e2.xmin + e2.xmax) / 2);
				if (c != 0)
					return c;
				return Double.compare(e.ymin, e2.ymin);
			}
		});
		
		// handle pass 1 and pass 2
		result.result = recurseHandlePasses(result.result);

		return result;
	}

	/**
	 * this function change the power and speed parameters, and handle the pass1 and pass2 elements
	 * @param objects
	 * @return
	 */
	private OptimizedObject[] recurseHandlePasses(OptimizedObject[] objects) {
		if (objects == null || objects.length == 0) {
			return new OptimizedObject[0];
		}

		ArrayList<OptimizedObject> list = new ArrayList<>();

		for (OptimizedObject o : objects) {

			if (o instanceof CutLine) {
				CutLine cl = (CutLine) o;
				cl.powerFraction = parameters.getPowerFractionPass1();
				cl.speedFraction = parameters.getSpeedFractionPass1();
				list.add(cl);

			} else if (o instanceof GroupedCutLine) {

				GroupedCutLine gcl = (GroupedCutLine) o;
				{
					List<CutLine> innerLines = gcl.getLinesByRefs();
					assert innerLines != null;
					for (CutLine c : innerLines) {
						c.powerFraction = parameters.getPowerFractionPass1();
						c.speedFraction = parameters.getSpeedFractionPass1();
					}
				}

				list.add(gcl);

				if (parameters.isHas2pass()) {
					GroupedCutLine pass2group = SerializeTools.deepClone(gcl);
					List<CutLine> innerLines = gcl.getLinesByRefs();
					assert innerLines != null;
					for (CutLine c : innerLines) {
						c.powerFraction = parameters.getPowerFractionPass2();
						c.speedFraction = parameters.getSpeedFractionPass2();
					}
					list.add(pass2group);
				}
			}
		}
		
		return list.toArray(new OptimizedObject[list.size()]);
	}

	@Override
	public OptimizerResult<OptimizedObject> optimize(VirtualBook carton, 
			OptimizerProgress progress, 
			ICancelTracker ct)
					throws Exception {
		return optimize(carton);
	}

}
