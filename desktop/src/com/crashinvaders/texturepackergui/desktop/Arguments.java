package com.crashinvaders.texturepackergui.desktop;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

public class Arguments {
    @Argument
    public File project;

    @Option(name = "--batch")
    public boolean batch;

    @Option(name = "--debug")
    public boolean debug;

    @Option(name = "--no-native-files")
    public boolean disableNativeFileDialogs;
}
