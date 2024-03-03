package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.generic.lasergcode;

import java.io.StringWriter;
import java.util.List;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.Command;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CutToCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.HomingCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlanIO;
import org.junit.jupiter.api.Test;

/**
 * Test generic laser generation
 * 
 * @author pfreydiere
 *
 */
public class TestGenericLazerGCodeGeneration {

	public PunchPlan createTestPunchPlan() {
		PunchPlan punchPlan = new PunchPlan();
		List<Command> commands = punchPlan.getCommandsByRef();

		commands.add(new HomingCommand());
		commands.add(new DisplacementCommand(0, 0));
		commands.add(new CutToCommand(10, 10, 1.0, 5.0));

		return punchPlan;
	}

	@Test
	public void testGeneration() throws Exception {
		GenericLazerGCodeParameterGeneration genericLazerGCodeParameterGeneration = new GenericLazerGCodeParameterGeneration();
		GenericLazerCompilerVisitor visitor = new GenericLazerCompilerVisitor(100, 100,
				genericLazerGCodeParameterGeneration);

		// generate a test
		StringWriter sw = new StringWriter();
		PunchPlanIO.exportToGRBL(sw, createTestPunchPlan(), visitor);

		System.out.println(sw.toString());
	}
	
	@Test
	public void testGeneration1() throws Exception {
		GenericLazerGCodeParameterGeneration genericLazerGCodeParameterGeneration = new GenericLazerGCodeParameterGeneration();
		genericLazerGCodeParameterGeneration.cuttingPostCommand ="endcut";
		genericLazerGCodeParameterGeneration.cuttingPreCommand ="begincut";
		genericLazerGCodeParameterGeneration.cuttingToCommandPattern ="cut X%1$f Y%2$f F%3$d S%4$d";
		
		genericLazerGCodeParameterGeneration.displacementPreCommand ="startdisplacement";
		genericLazerGCodeParameterGeneration.displacementPostCommand ="endingdisplacement";
		genericLazerGCodeParameterGeneration.displacementCommandPattern ="displacement  X%1$f Y%2$f";
		
		genericLazerGCodeParameterGeneration.startBookPrecommands="startbook";
		genericLazerGCodeParameterGeneration.endBookPrecommands = "endbook";
		
		genericLazerGCodeParameterGeneration.powerChangeCommand = "changepower";
		genericLazerGCodeParameterGeneration.homingCommands = "homingcommand";
		
		
		GenericLazerCompilerVisitor visitor = new GenericLazerCompilerVisitor(100, 100,
				genericLazerGCodeParameterGeneration);

		// generate a test
		StringWriter sw = new StringWriter();
		PunchPlanIO.exportToGRBL(sw, createTestPunchPlan(), visitor);

		System.out.println(sw.toString());
	}

}
