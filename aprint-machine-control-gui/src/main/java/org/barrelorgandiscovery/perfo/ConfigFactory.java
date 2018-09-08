package org.barrelorgandiscovery.perfo;

import java.io.File;

public class ConfigFactory {

  private ConfigFactory() {}

  private static Config createUnix() {
    Config c = new Config();
    c.usbPort = "/dev/ttyUSB0";
    c.fileFolderPath = new File("/media");

    return c;
  }
  
  private static Config createTest() {
	  Config c = new Config();
	  c.usbPort = "COM4";
	  c.fileFolderPath = new File("c:\\temp");
	  return c;
  }

  public static Config getInstance() {
    return createTest();
  }
  
}
