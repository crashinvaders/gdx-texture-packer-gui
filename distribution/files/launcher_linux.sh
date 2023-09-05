#!/bin/sh
# Linux launcher script.

# Ensure Java is installed.
if [ ! -x "$(command -v java)" ]; then
    echo "Error: \"java\" command is not available. Please make sure Java (JRE 8.0+) is installed on your system." >&2
    exit 1;
fi

# cd to the installation dir.
# A good solution that can withstand symbolic links.
# https://stackoverflow.com/a/17744637/3802890
SCRIPT_DIR="$(dirname "$(readlink -f -- "$0")")"
cd "$SCRIPT_DIR" || exit 1

# Launch the app.
java -Xms64m -Xmx1024m -XX:+UseG1GC -XX:MinHeapFreeRatio=15 -XX:MaxHeapFreeRatio=30 \
-jar ./gdx-texture-packer.jar "$@"