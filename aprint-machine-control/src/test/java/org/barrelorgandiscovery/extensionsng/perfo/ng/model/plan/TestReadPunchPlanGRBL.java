package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import java.io.InputStream;

import org.junit.jupiter.api.Test;


public class TestReadPunchPlanGRBL {

  public TestReadPunchPlanGRBL() {
  }
  
  @Test
  public void testRead() throws Exception {
	  InputStream is = getClass().getResourceAsStream("testgrbl.gcode");
	  PunchPlan p = PunchPlanIO.readFromGRBL(is);
	  System.out.println(p);
  }
}
