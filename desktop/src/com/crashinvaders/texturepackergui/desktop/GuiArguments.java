package com.crashinvaders.texturepackergui.desktop;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

public class GuiArguments {

    @Option(name = "--no-native-files",
            usage = "Use custom VisUI dialogs for all the file picker dialogs (instead of the native file dialogs).")
    public boolean disableNativeFileDialogs;

    @Option(name = "--debug",
            usage = "Enables debug functionality. Prints some verbose log messages.")
    public boolean debug = false;

    @Argument(usage = "Path to the project file to load.")
    public File project;
}
