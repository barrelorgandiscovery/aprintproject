package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.punchio;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan.PunchPlan;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class PunchFileBag {

  private VirtualBook vb;
  private PunchPlan punchplan;

  public PunchFileBag(VirtualBook vb, PunchPlan punchplan) {
    assert vb != null;
    assert punchplan != null;
    this.vb = vb;
    this.punchplan = punchplan;
  }

  public PunchPlan getPunchplan() {
    return punchplan;
  }

  public VirtualBook getVirtualBook() {
    return vb;
  }
  
  
}
