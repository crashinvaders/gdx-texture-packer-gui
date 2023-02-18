package com.crashinvaders.texturepackergui.desktop;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.File;

public class CliBatchArguments {

    @Option(name = "--project", aliases = "-p",
            required = true,
            usage = "Path to the project file to load.")
    public File project;

    @Option(name = "--atlases", aliases = "-a",
            handler = StringArrayOptionHandler.class,
            usage = "Space separated list of atlas names to pack.")
    public String[] packNames = new String[0];

    @Option(name = "--threads", aliases = "-t",
            usage = "Max number of parallel processing threads to use during atlas packing. Default value is 4.")
    public int threads = 4;

    @Option(name = "--list-atlases", aliases = "-l",
            forbids = { "--atlases", "--threads" },
            usage = "Prints the project's atlas names and exit.")
    public boolean listAtlases;

    @Option(name = "--debug",
            usage = "Enables debug functionality. Prints some verbose log messages.")
    public boolean debug = false;
}
