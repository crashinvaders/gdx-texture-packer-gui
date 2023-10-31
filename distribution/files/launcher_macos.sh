#!/bin/sh
# macOS launcher script.

# Ensure Java is installed.
if [ ! -x "$(command -v java)" ]; then
    echo "Error: \"java\" command is not available. Please make sure Java (JRE 8.0+) is installed on your system." >&2
    exit 1;
fi

SCRIPT_DIR=$(dirname "$0")

# Launch the app.
java -Xms64m -Xmx2048m \
-XstartOnFirstThread -Djava.awt.headless=true \
-jar "$SCRIPT_DIR"/gdx-texture-packer.jar "$@"