#!/bin/sh

# Should executed on MacOS only.
./gradlew jnigen jnigenBuildMacOsX64 jnigenBuildMacOsXARM64 jnigenJarNativesDesktop
