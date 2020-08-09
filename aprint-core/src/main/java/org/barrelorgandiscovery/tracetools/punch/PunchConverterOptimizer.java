package org.barrelorgandiscovery.tracetools.punch;

import java.util.Arrays;
import java.util.Comparator;

import javax.swing.ImageIcon;

import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.atrace.Optimizer;
import org.barrelorgandiscovery.gui.atrace.OptimizerProgress;
import org.barrelorgandiscovery.gui.atrace.OptimizerResult;
import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.gui.atrace.PunchConverter;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

/**
 * punch converter exposed as an optimizer
 * 
 * @author pfreydiere
 * 
 */
public class PunchConverterOptimizer implements Optimizer<Punch> {

	private PunchConverterOptimizerParameters parameters = new PunchConverterOptimizerParameters();

	/**
	 * default constructor
	 */
	public PunchConverterOptimizer() {

	}

	public PunchConverterOptimizer(PunchConverterOptimizerParameters parameters) {
		assert parameters != null;
		this.parameters = parameters;
	}

	@Override
	public Object getDefaultParameters() {
		return parameters;
	}

	@Override
	public String getTitle() {
		return Messages.getString("PunchConverterOptimizer.0"); //$NON-NLS-1$
	}

	@Override
	public ImageIcon getIcon() {
		// no icon yet
		return null;
	}

	@Override
	public OptimizerResult<Punch> optimize(VirtualBook carton,
			OptimizerProgress progress, ICancelTracker ct) throws Exception {
		
		assert carton != null;

		PunchConverter pc = new PunchConverter(carton.getScale(),
				parameters.getPunchWidth(), parameters.getOverlap(),
				parameters.getNotPunchedIfLessThan());

		OptimizerResult<Punch> convert = pc.convert(carton.getOrderedHolesCopy());

		// order the punches by x
		Arrays.sort(convert.result, new Comparator<Punch>() {
			@Override
			public int compare(Punch o1, Punch o2) {
				return Double.compare(o1.x, o2.x);
			}
		});

		return convert;
	}

	@Override
	public OptimizerResult<Punch> optimize(VirtualBook carton) throws Exception {
		return optimize(carton, null, null);
	}

}
