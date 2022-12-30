# APrint Studio Project

*Barrel organ discovery - 2004-2022*

CI 2020 q2 : ![](https://travis-ci.org/barrelorgandiscovery/aprintproject.svg?branch=aprint_2020_q2)

This repository contains the **full source** of APrint Studio, more informations can be found on the web site : [http://www.barrel-organ-discovery.org](http://www.barrel-organ-discovery.org)  


![](doc/main_screenshot.png)


APrint Studio is composed of a constellation of **tools** for creating **mechanical books** , for **street organs**, **fair organs**, **musicbox** .. 

### Discussions / Forum

for issues, improvements, this github repository is the right place. 
for discussions use freddy's forum, [https://orguedebarbarie.vraiforum.com/](https://orguedebarbarie.vraiforum.com/)


### How to Build main project

Note on 2022 version, this version switch to 13 to 17 java version mainly. This java version can be downloaded from Oracle Download Center or adoptjdk website.


from the root folder, launch :

```
gradlew createAllJars
```

compilation results will be located in bundle\build
extensions (that are installed in the personal home folder) are generated in bundle\offlineinstall-extensions 


### Running the project from command line

```
java -Xmx2g -server -Dmainfolder="C:\Users\use\Documents\.." -cp aprint.jar org.barrelorgandiscovery.gui.aprintng.APrintApplicationBootStrap
```


### Building the windows installer (only on windows platefoms)

Windows installer is available, using the NSIS project, **NSIS** must be installed to create the windows installer.

for creating the Installer with updated extensions launch the following command :

```
gradlew createAllInstaller
```

### for DEVS : Launch automatic tests and global test report

```
gradlew check testReport jacocoTestReport
```

