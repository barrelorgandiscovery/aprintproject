package org.barrelorgandiscovery.model;

import java.util.ArrayList;
import java.util.List;

import org.barrelorgandiscovery.model.steps.book.VirtualBookDemultiplexer;
import org.barrelorgandiscovery.model.steps.book.VirtualBookMultiplexer;
import org.barrelorgandiscovery.model.steps.impl.NewBookFrame;
import org.barrelorgandiscovery.model.steps.midi.MidiDemultiplexer;
import org.barrelorgandiscovery.model.steps.midi.MidiFileInput;
import org.barrelorgandiscovery.model.steps.scripts.GroovyScriptModelStep;

public class DefaultModelStepRegistry extends ModelStepRegistry {
  @Override
  public List<ModelStep> getRegisteredModelStepList() throws Exception {
    ArrayList<ModelStep> ms = new ArrayList<ModelStep>();
    ms.add(new VirtualBookMultiplexer());
    ms.add(new VirtualBookDemultiplexer());
    ms.add(new MidiDemultiplexer());
    ms.add(new NewBookFrame()); // open a new Virtual
    ms.add(new MidiFileInput()); // midi Input
    ms.add(new TerminalParameterModelStep());
    ms.add(new GroovyScriptModelStep());

    return ms;
  }
}
