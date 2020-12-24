package org.barrelorgandiscovery.gui.aprintng;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ChildFirstClassLoader;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;

public class APrintApplicationBootStrap {

  public static final String MAINFOLDER_SYSPROP = "mainfolder";

  public static void main(String[] args) throws Exception {

    boolean isbeta = false;
    boolean isdevelop = false;

    if (args.length > 0) //$NON-NLS-1$
    {
      if (args[0].equals("develop")) { //$NON-NLS-1$
        isdevelop = true;
        isbeta = true;
      }

      if (args[0].equals("beta")) //$NON-NLS-1$
      isbeta = true;
    }

    // setQuaquaLnf();

    Properties sysprop = System.getProperties();

    Logger.getRootLogger().setLevel(Level.INFO);

    // Logger logger = Logger.getLogger(APrintApplicationBootStrap.class);
    // if (logger.isInfoEnabled()) {
    // sysprop.list(System.out);
    // try {
    // Set<Entry<Object, Object>> entrySet = sysprop.entrySet();
    // for (Iterator iterator = entrySet.iterator(); iterator
    // .hasNext();) {
    // Entry<Object, Object> entry = (Entry<Object, Object>) iterator
    // .next();
    //
    ////					logger.info("" + entry.getKey() + "=" //$NON-NLS-1$ //$NON-NLS-2$
    // // + entry.getValue());
    // }
    // } catch (Exception ex) {
    //				logger.error(ex.getMessage(), ex); //$NON-NLS-1$
    // }
    //
    // }

    String mainFolder = sysprop.getProperty(MAINFOLDER_SYSPROP, null);
   
    final APrintProperties prop =
        new APrintProperties(
            "aprintstudio", //$NON-NLS-1$
            isbeta,
            mainFolder);

    // get all the jar in the aprintstudiofolder

    String cp = sysprop.getProperty("java.class.path");
    String[] cplist = cp.split(";");

    ArrayList<File> syslist = new ArrayList<File>();
    for (int i = 0; i < cplist.length; i++) {
      String p = cplist[i];
      syslist.add(new File(p));
    }

    File[] libraryJars =
        prop.getAprintFolder()
            .listFiles(
                new FilenameFilter() {

                  public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                  }
                });

    syslist.addAll(Arrays.asList(libraryJars));

    ArrayList<URL> urls = new ArrayList<URL>();
    
    
    for (Iterator iterator = syslist.iterator(); iterator.hasNext(); ) {
      File file = (File) iterator.next();
      urls.add(file.toURL());
    }

    // logger.debug("Main class loader :"
    // + APrintApplicationBootStrap.class.getClassLoader());

    // isolation of the classes in a specific class loader
    // only Messages , lnf and aprint properties are loaded in he
    // main class loader
    URLClassLoader cl =
        new ChildFirstClassLoader(
            urls.toArray(new URL[0]), APrintApplicationBootStrap.class.getClass().getClassLoader());

    // logger.debug("App Class Loader :" + cl);

    Class aprintApplication =
        cl.loadClass("org.barrelorgandiscovery.gui.aprintng.APrintApplication");
    Method main = aprintApplication.getMethod("main", String[].class);

    Thread.currentThread().setContextClassLoader(cl);

    main.invoke(null, new Object[] {args});
  }
}
