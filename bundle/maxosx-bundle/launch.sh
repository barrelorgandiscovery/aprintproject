#!/bin/bash

export LD_LIBRARY_PATH=/home/use/aprintstudio/pluginnativelibraries/
java -Xmx6g --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.desktop/javax.swing.text=ALL-UNNAMED --add-opens java.desktop/javax.swing=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED -jar aprint.jar

