package org.barrelorgandiscovery.extensionsng.perfo.ng.optimizers;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.gui.atrace.Optimizer;
import org.barrelorgandiscovery.tracetools.ga.GeneticOptimizer;
import org.barrelorgandiscovery.tracetools.punch.NoReturnPunchConverterOptimizer;
import org.barrelorgandiscovery.tracetools.punch.PunchConverterOptimizer;

/**
 * repository knowing all the optimizers and know which ones can be used for
 * each machine (discovery) this repository permit also to instanciate the
 * proper optimizer depending on the given parameters
 * 
 * @author pfreydiere
 * 
 */
public class OptimizersRepository {

	/**
	 * each optimizer has it's own parameters class with the same name
	 */
	private static final String PARAMETERS_SUFFIX_CLASS = "Parameters";

	private static Class[] optimizersClasses = { 
			PunchConverterOptimizer.class,
			NoReturnPunchConverterOptimizer.class ,
			GeneticOptimizer.class };

	public OptimizersRepository() {

	}

	public List<Class> listAvailableOptimizersForMachine(AbstractMachine machine) {
		return Arrays.asList(optimizersClasses);
	}

	public Serializable instanciateParametersForOptimizer(Class optimizerClass)
			throws Exception {
		assert optimizerClass != null;

		String className = optimizerClass.getName() + PARAMETERS_SUFFIX_CLASS;
		Class<?> parametersClass = Class.forName(className);

		return (Serializable)parametersClass.newInstance();

	}

	public Optimizer newOptimizerWithParameters(Serializable parameters)
			throws Exception {

		String parametersClassName = parameters.getClass().getName();
		assert parametersClassName.endsWith(PARAMETERS_SUFFIX_CLASS);
		String optimizerClassName = parametersClassName
				.substring(0, parametersClassName.length()
						- PARAMETERS_SUFFIX_CLASS.length());

		Class opClass = Class.forName(optimizerClassName);

		Constructor cons = opClass.getConstructor(parameters.getClass());

		return (Optimizer) cons.newInstance(parameters);

	}

}
