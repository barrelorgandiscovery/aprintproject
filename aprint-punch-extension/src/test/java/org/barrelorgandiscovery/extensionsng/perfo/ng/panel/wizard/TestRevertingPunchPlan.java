package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.CutToCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.DisplacementCommand;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.optimizers.model.CutLine;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.junit.jupiter.api.Test;

public class TestRevertingPunchPlan {

	@Test
	public void testRevertingPunchPlan() throws Exception {
		
		VirtualBook vb = new VirtualBook(Scale.getGammeMidiInstance());
		vb.addHole(new Hole(0,0,10000));
		
		PunchPlan pp = new PunchPlan();
		pp.getCommandsByRef().add(new DisplacementCommand(10, 20));
		pp.getCommandsByRef().add(new CutToCommand(30, 50, 1, 1));

		CutLine cutLine = new CutLine(0d,0d,10d,20d,1d,1d);
		
		
		
		// PunchBookAndPlan out = StepResume.compileAndOptionnalyReverseToHaveTheReferenceUp(vb, new OptimizedObject[] {cutLine});
		
	//	System.out.println(out);
		
	}

}
