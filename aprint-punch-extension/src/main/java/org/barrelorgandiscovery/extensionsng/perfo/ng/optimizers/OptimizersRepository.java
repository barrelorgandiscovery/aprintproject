package org.barrelorgandiscovery.extensionsng.perfo.ng.optimizers;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.AbstractMachineParameters;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.BaseAbstractLazerMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.BaseAbstractPunchMachine;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CutToCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.optimizers.Optimizer;
import org.barrelorgandiscovery.optimizers.PunchDefaultConverter;
import org.barrelorgandiscovery.optimizers.cad.XOptim;
import org.barrelorgandiscovery.optimizers.ga.GeneticOptimizer;
import org.barrelorgandiscovery.optimizers.model.CutLine;
import org.barrelorgandiscovery.optimizers.model.GroupedCutLine;
import org.barrelorgandiscovery.optimizers.model.OptimizedObject;
import org.barrelorgandiscovery.optimizers.model.Punch;
import org.barrelorgandiscovery.optimizers.punch.NoReturnPunchConverterOptimizer;
import org.barrelorgandiscovery.optimizers.punch.PunchConverterOptimizer;

/**
 * repository knowing all the optimizers and know which ones can be used for
 * each machine (discovery) this repository permit also to instanciate the
 * proper optimizer depending on the given parameters.
 * 
 * This repository dissociate the optimization from the machine control (to keep
 * it simple)
 * 
 * @author pfreydiere
 * 
 */
public class OptimizersRepository {

	/**
	 * each optimizer has it's own parameters class with the same name
	 */
	private static final String PARAMETERS_SUFFIX_CLASS = "Parameters";

	public OptimizersRepository() {

	}

	public static Class[] punchOptimizersClasses = { PunchConverterOptimizer.class,
			NoReturnPunchConverterOptimizer.class, GeneticOptimizer.class };

	public List<Class> listAvailableOptimizersForMachine(AbstractMachine machine) {

		if (machine instanceof BaseAbstractPunchMachine) {
			return Arrays.asList(punchOptimizersClasses);
		} else if (machine instanceof BaseAbstractLazerMachine) {
			return Arrays.asList(new Class[] { XOptim.class });
		}
		return Arrays.asList(new Class[0]);
	}

	public Serializable instanciateParametersForOptimizer(Class optimizerClass) throws Exception {
		assert optimizerClass != null;

		String className = optimizerClass.getName() + PARAMETERS_SUFFIX_CLASS;
		Class<?> parametersClass = Class.forName(className);

		return (Serializable) parametersClass.newInstance();
	}

	/**
	 * this method create the Optimizer class using the parameters instance, 
	 * it take the class name and create the associated optimizer class, stripping the "Parameters" suffix
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public Optimizer newOptimizerWithParameters(Serializable parameters) throws Exception {

		String parametersClassName = parameters.getClass().getName();
		assert parametersClassName.endsWith(PARAMETERS_SUFFIX_CLASS);
		
		String optimizerClassName = parametersClassName.substring(0,
				parametersClassName.length() - PARAMETERS_SUFFIX_CLASS.length());

		Class opClass = Class.forName(optimizerClassName);

		Constructor cons = opClass.getConstructor(parameters.getClass());

		return (Optimizer) cons.newInstance(parameters);

	}

	/**
	 * create a punch plan from machine parameters
	 * 
	 * @param parameters
	 * @return
	 */
	public PunchPlan createDefaultPunchPlanFromOptimizeResult(AbstractMachine machine,
			AbstractMachineParameters parameters, OptimizedObject[] optimizedObjects) throws Exception {

		//
		if (machine instanceof BaseAbstractPunchMachine) {

			Punch[] punches = null;
			if (optimizedObjects instanceof Punch[]) {
				punches = (Punch[]) optimizedObjects;
			} else if (optimizedObjects instanceof OptimizedObject[]) {
				
				punches = new Punch[optimizedObjects.length];
				for (int i = 0 ; i < optimizedObjects.length ; i ++) {
					punches[i] = (Punch)optimizedObjects[i];
				}

			} else {
				throw new Exception("bad parameters :" + optimizedObjects);
			}

			PunchPlan pp = PunchDefaultConverter.createDefaultPunchPlan(punches);
			return pp;

		} else if (machine instanceof BaseAbstractLazerMachine) {
			return createDefaultPunchPlanForLazerMachine(parameters, optimizedObjects);
		}

		return null;
	}

	protected void recurseAddCutLines(OptimizedObject[] objects, List<CutLine> cutlines) throws Exception {
		assert cutlines != null;
		for (OptimizedObject o : objects) {
			if (o instanceof GroupedCutLine) {
				GroupedCutLine g = (GroupedCutLine) o;
				recurseAddCutLines(g.getLinesByRefs().toArray(new CutLine[0]), cutlines);
			} else if (o instanceof CutLine) {
				cutlines.add((CutLine) o);
			} else {
				throw new Exception("unknown class " + o.getClass());
			}
		}
	}

	/**
	 * this convert the optimised objects into commands
	 * 
	 * @param parameters
	 * @param optimizedObjects
	 * @return
	 * @throws Exception
	 */
	public PunchPlan createDefaultPunchPlanForLazerMachine(AbstractMachineParameters parameters,
			OptimizedObject[] optimizedObjects) throws Exception {

		// flatten the grouped objects
		ArrayList<CutLine> lines = new ArrayList<CutLine>();
		recurseAddCutLines(optimizedObjects, lines);

		PunchPlan pp = new PunchPlan();

		// add move to 0/0
		pp.getCommandsByRef().add(new DisplacementCommand(0, 0));

		for (CutLine p : lines) {
			if (p == null)
				continue;
			pp.getCommandsByRef().add(new DisplacementCommand(p.x1, p.y1));
			pp.getCommandsByRef().add(new CutToCommand(p.x2, p.y2, p.powerFraction, p.speedFraction));
		}

		return pp;
	}

}
