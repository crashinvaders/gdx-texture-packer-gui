package com.crashinvaders.texturepackergui.desktop;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

public class Arguments {
    public static final String LAUNCHER_AWT = "awt";
    public static final String LAUNCHER_GL_SURFACE = "glsurface";

    @Argument
    public File project;

    @Option(name = "-launcher")
    public String launcher = LAUNCHER_AWT;
}
