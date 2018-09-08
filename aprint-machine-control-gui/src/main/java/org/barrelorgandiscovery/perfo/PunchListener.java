package org.barrelorgandiscovery.perfo;

import java.io.File;

/**
 * interface information listener
 * 
 * @author pfreydiere
 */
public interface PunchListener {

  public void startFile(File punchFile);

  public void informCurrentPunchPosition(File currentFile, int currentPunchIndex, int totalPunchNumber);
  
  public void finishedFile(File file);
  
  public void initializing(String status);
  
  public void message(String message);
  
}
