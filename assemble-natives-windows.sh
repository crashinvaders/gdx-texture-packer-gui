#!/bin/sh

# To build Windows natives from under a UNIX environment, you need to have mingw64 installed.
# Please read the instructions from https://github.com/crashinvaders/gdx-basis-universal/tree/master/basisu-wrapper#linux

./gradlew jnigen jnigenBuildWindows64 jnigenJarNativesDesktop

