package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import java.util.Arrays;
import java.util.List;

import org.barrelorgandiscovery.tracetools.ga.GeneticOptimizer;
import org.barrelorgandiscovery.tracetools.punch.NoReturnPunchConverterOptimizer;
import org.barrelorgandiscovery.tracetools.punch.PunchConverterOptimizer;

public abstract class BaseAbstractPunchMachine extends AbstractMachine {

	public static Class[] punchOptimizersClasses = { PunchConverterOptimizer.class, NoReturnPunchConverterOptimizer.class,
			GeneticOptimizer.class };

	@Override
	public List<Class> getAvailableOptimizerClasses() {
		return Arrays.asList(punchOptimizersClasses);
	}

}
