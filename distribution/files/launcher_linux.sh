#!/usr/bin/env bash
# Linux launcher
SCRIPTDIR=$(dirname "$0")
cd $SCRIPTDIR
java -Xms64m -Xmx1024m -XX:+UseG1GC -XX:MinHeapFreeRatio=15 -XX:MaxHeapFreeRatio=30 -jar ./gdx-texturepacker-linux.jar $1