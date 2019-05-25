package org.barrelorgandiscovery.perfo;

import java.io.File;

public class ConfigFactory {

  private ConfigFactory() {}

  private static Config createUnix() {
    Config c = new Config();
    c.usbPort = "/dev/ttyUSB0"; 
    		// "/dev/ttyACM0";
    		// "/dev/ttyUSB0"; // chinois
    // uno normal : /dev/ttyACM0 // non validé pour l'instant
    c.fileFolderPath = new File("/media");
    return c;
  }
  
  private static Config createTest() {
	  Config c = new Config();
	  c.usbPort = "COM7";
	  c.fileFolderPath = new File("c:\\temp\\testfreddy");
	  c.isDebug = true;
	  return c;
  }

  public static Config getInstance() {
    return createUnix();
    		// createTest();
  }
  
}
