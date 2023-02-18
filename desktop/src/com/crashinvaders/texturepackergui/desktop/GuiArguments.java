package com.crashinvaders.texturepackergui.desktop;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

public class GuiArguments {

    @Option(name = "--no-native-files",
            usage = "Use cross-platform unified VisUI dialogs instead of the native file picker dialogs.")
    public boolean disableNativeFileDialogs = false;

    @Option(name = "--debug",
            usage = "Enables debug functionality. Prints verbose log messages.")
    public boolean debug = false;

    @Argument(usage = "Path to the project file to load.",
            metaVar = "PROJECT_FILE",
            hidden = true)
    public File project;
}
