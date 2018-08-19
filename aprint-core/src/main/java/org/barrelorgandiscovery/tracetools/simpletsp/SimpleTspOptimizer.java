package org.barrelorgandiscovery.tracetools.simpletsp;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.atrace.Optimizer;
import org.barrelorgandiscovery.gui.atrace.OptimizerProgress;
import org.barrelorgandiscovery.gui.atrace.OptimizerResult;
import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.gui.atrace.PunchConverter;
import org.barrelorgandiscovery.gui.atrace.Tools;
import org.barrelorgandiscovery.tracetools.punch.PunchConverterOptimizerParameters;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class SimpleTspOptimizer implements Optimizer {

	private static Logger logger = Logger.getLogger(SimpleTspOptimizer.class);

	private PunchConverterOptimizerParameters parameters;

	public SimpleTspOptimizer(PunchConverterOptimizerParameters parameters) {
		assert parameters != null;
		this.parameters = parameters;
	}

	@Override
	public Object getDefaultParameters() {
		return new PunchConverterOptimizerParameters();
	}

	@Override
	public String getTitle() {
		return "TSP Optimized";
	}
	
	@Override
	public ImageIcon getIcon() {
		return null;
	}
	
	@Override
	public OptimizerResult optimize(VirtualBook carton,
			OptimizerProgress progress, ICancelTracker ct) throws Exception {

		// Découpe du carton en page de 20 notes ...
		ArrayList<ArrayList<Hole>> pages = Tools.divide(
				carton.getOrderedHolesCopy(), 20);

		// Invariant, pages contient la liste des pages ...

		ArrayList<Punch> punchlist = new ArrayList<Punch>();

		PunchConverter pc = new PunchConverter(carton.getScale(),
				parameters.getPunchWidth(), parameters.getOverlap(),
				parameters.getNotPunchedIfLessThan());

		// on les optimisent ..
		for (int i = 0; i < pages.size(); i++) {
			ArrayList<Hole> n = pages.get(i);

			TracePage tp = new TracePage(n, pc);
			Punch[] punches = tp.optimize();

			// on les ajoute à la collection
			for (int j = 0; j < punches.length; j++) {
				punchlist.add(punches[j]);
			}
		}

		OptimizerResult r = new OptimizerResult();
		r.result = new Punch[punchlist.size()];
		punchlist.toArray(r.result);

		return r;
	}

	public OptimizerResult optimize(VirtualBook carton) throws Exception {
		return optimize(carton, null, null);
	}

}
