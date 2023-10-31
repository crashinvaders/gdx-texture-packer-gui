#!/bin/sh
# Linux launcher script.

# Ensure Java is installed.
if [ ! -x "$(command -v java)" ]; then
    echo "Error: \"java\" command is not available. Please make sure Java (JRE 8.0+) is installed on your system." >&2
    exit 1;
fi

# Find the installation dir.
# A good solution that can withstand symbolic links.
# https://stackoverflow.com/a/17744637/3802890
SCRIPT_DIR="$(dirname "$(readlink -f -- "$0")")"

# Launch the app.
java -jar "$SCRIPT_DIR"/gdx-texture-packer.jar "$@"