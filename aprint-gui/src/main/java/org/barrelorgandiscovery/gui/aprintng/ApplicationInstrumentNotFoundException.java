package org.barrelorgandiscovery.gui.aprintng;

import org.barrelorgandiscovery.virtualbook.VirtualBook;

public class ApplicationInstrumentNotFoundException extends Exception {

  /** */
  private static final long serialVersionUID = 8911808410913642330L;

  private VirtualBook virtualBook;
  private String optionalPreferredInstrument;

  public ApplicationInstrumentNotFoundException(
      String message, VirtualBook readVirtualBook, String optionalPreferredInstrument) {
    super(message);
    assert readVirtualBook != null;
    this.virtualBook = readVirtualBook;
    
    assert optionalPreferredInstrument != null;
    this.optionalPreferredInstrument = optionalPreferredInstrument;
  }

  public VirtualBook getVirtualBook() {
    return virtualBook;
  }

  public String getOptionalPreferredInstrument() {
    return optionalPreferredInstrument;
  }
}
