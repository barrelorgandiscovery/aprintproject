# APrint Studio Project

*Barrel organ discovery - 2004-2022*

CI 2020 q2 : ![](https://travis-ci.org/barrelorgandiscovery/aprintproject.svg?branch=aprint_2020_q2)

This repository contains the **full source** of APrint Studio, more informations can be found on the web site : [http://www.barrel-organ-discovery.org](http://www.barrel-organ-discovery.org)  


![](doc/main_screenshot.png)


APrint Studio is composed of a constellation of **tools** for creating **mechanical books** , for **street organs**, **fair organs**, **musicbox** .. 

### Discussions / Forum

for issues, improvements, this github repository is the right place. 
for discussions , [https://github.com/barrelorgandiscovery/aprintproject/discussions](https://github.com/barrelorgandiscovery/aprintproject/discussions) 

### How to Build main project

Note on 2022 version, this version switch to 13 to 17 java version mainly. This java version can be downloaded from Oracle Download Center or adoptjdk website.
The binaries are now provided on the github website, and everybody is able to build the app-image. The adoptjdk 17 is needed.

#### Building the app image


for creating the Installer with updated extensions launch the following command :

```
gradlew createjpackage
```

building only the jars :
```
gradlew createMacOsAndLinuxBundle
```

#### Running the project from command line

```
java -Xmx2g -server -Dmainfolder="C:\Users\use\Documents\.." -cp aprint.jar org.barrelorgandiscovery.gui.aprintng.APrintApplicationBootStrap
```

#### for DEVS : Launch automatic tests and global test report

```
gradlew check testReport jacocoTestReport
```

