cd /d %~dp0
start javaw -Xms64m -Xmx1024m -XX:+UseG1GC -XX:MinHeapFreeRatio=15 -XX:MaxHeapFreeRatio=30 -jar gdx-texturepacker.jar %*