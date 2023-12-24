package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.punchio;

import java.io.File;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.optimizers.OptimizerResult;
import org.barrelorgandiscovery.optimizers.PunchDefaultConverter;
import org.barrelorgandiscovery.optimizers.model.Punch;
import org.barrelorgandiscovery.optimizers.punch.PunchConverterOptimizer;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.junit.jupiter.api.Test;

public class TestPunchIO {

  public TestPunchIO() {
    
  }
  
  @Test
  public void testSaveLoad() throws Exception {
	  
	  VirtualBook s = new VirtualBook(Scale.getGammeMidiInstance());
	  s.addHole(new Hole(1, 1_000, 1_000_000));
	  
	  PunchConverterOptimizer p = new PunchConverterOptimizer();
	  OptimizerResult<Punch> r = p.optimize(s);
	  
	  PunchPlan pp = PunchDefaultConverter.createDefaultPunchPlan(r.result);
	  
	  PunchFileBag pb = new PunchFileBag(s, pp);
	  
	  PunchIO.exportToPunchFile(pb, new File("c:\\temp\\test.punch"));
	  
  }
  
}
