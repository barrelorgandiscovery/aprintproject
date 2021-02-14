# APrint Studio Project

*Barrel organ discovery - 2004-2020*



This repository contains the **full source** of APrint Studio, more informations can be found on the web site : [http://www.barrel-organ-discovery.org](http://www.barrel-organ-discovery.org)  



![](doc/main_screenshot.png)



APrint Studio is composed of a constellation of **tools** for creating **mechanical books** , for **street organs**, **fair organs**, **musicbox** .. 

### Discussions / Forum

for issues, improvements, this github repository is the right place. 
for discussions use freddy's forum, [https://orguedebarbarie.vraiforum.com/](https://orguedebarbarie.vraiforum.com/)


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

for creating the Installer with updated extensions launch the following command :

```
gradlew createBundles
```





