package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.punchio;

import java.io.File;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.gui.atrace.OptimizerResult;
import org.barrelorgandiscovery.gui.atrace.Punch;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tracetools.punch.PunchConverterOptimizer;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.junit.Test;

public class TestPunchIO {

  public TestPunchIO() {
    
  }
  
  @Test
  public void testSaveLoad() throws Exception {
	  
	  VirtualBook s = new VirtualBook(Scale.getGammeMidiInstance());
	  s.addHole(new Hole(1, 1_000, 1_000_000));
	  
	  PunchConverterOptimizer p = new PunchConverterOptimizer();
	  OptimizerResult<Punch> r = p.optimize(s);
	  
	  PunchPlan pp = PunchPlan.createDefaultPunchPlan(r.result);
	  
	  PunchFileBag pb = new PunchFileBag(s, pp);
	  
	  PunchIO.exportToPunchFile(pb, new File("c:\\temp\\test.punch"));
	  
  }
  
}
