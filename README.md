# APrint Studio Project

*Patrice Freydiere - 2004-2020*



This repository contains the full source of APrint Studio, more informations can be found on the web site : [http://www.barrel-organ-discovery.org](http://www.barrel-organ-discovery.org)  



APrint Studio consist in  tools for creating mechanical books , for street organs, barrel organs, musicbox .. 



### How to Build main project

For building the project, **Java 8** SDK is needed.

from the root folder, launch :

```
gradlew fatJar
```

the main application result is then located in **aprint-gui\build\libs\aprint.jar**



### Running the project from command line

```
java -Xmx2g -server -Dmainfolder="C:\Users\use\Documents\.." -cp aprint.jar org.barrelorgandiscovery.gui.aprintng.APrintApplicationBootStrap
```



### Building the windows installer

Windows installer is available, using the NSIS project, **NSIS** must be installed to create the windows installer.

### 



