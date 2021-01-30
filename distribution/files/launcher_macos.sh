#!/usr/bin/env bash
SCRIPTDIR=$(dirname "$0")
cd $SCRIPTDIR
java -Xms64m -Xmx1024m -XX:+UseG1GC -XX:MinHeapFreeRatio=15 -XX:MaxHeapFreeRatio=30 -XstartOnFirstThread -Djava.awt.headless=true -jar ./gdx-texturepacker.jar