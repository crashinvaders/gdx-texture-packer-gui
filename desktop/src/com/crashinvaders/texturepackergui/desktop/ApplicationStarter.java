package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfigurationX;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.AppParams;
import com.crashinvaders.texturepackergui.SystemFileOpener;
import com.crashinvaders.texturepackergui.desktop.cli.CliBatchApp;
import com.github.czyzby.autumn.fcs.scanner.DesktopClassScanner;
import org.kohsuke.args4j.*;
import org.lwjgl.system.macosx.LibC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ApplicationStarter {

    private static final ParserProperties argParserProps =
            ParserProperties.defaults()
                    .withShowDefaults(false);

    public static void main(final String[] args) {
        // Print help and exit.
        if (args.length > 0 && "--help".equals(args[0])) {
            printHelpMessage();
            return;
        }

        // Print version and exit.
        if (args.length > 0 && "--version".equals(args[0])) {
            System.out.println(AppConstants.VERSION.toString());
            return;
        }

        // Batch mode.
        if (args.length > 0 && ("--batch".equals(args[0]) || "-b".equals(args[0]))) {
            // Trim the first args element.
            String[] argsBatch = Arrays.copyOfRange(args, 1, args.length);
            CliBatchArguments arguments = new CliBatchArguments();
            parseArguments(argsBatch, arguments);
            startCliBatchApp(arguments);
            return;
        }

        // Regular GUI mode.
        {
            if (startNewJvmIfRequired(args))
                return;

            GuiArguments arguments = new GuiArguments();
            parseArguments(args, arguments);
            startGuiApp(arguments);
        }
    }

    private static void printHelpMessage() {
        System.out.println("A simple utility to pack and manage texture atlases for libGDX game framework.");
        System.out.println("GitHub page: https://github.com/" + AppConstants.GITHUB_OWNER + "/" + AppConstants.GITHUB_REPO);
        System.out.println();
        System.out.println("List of general command line options:");
        System.out.println(" --help\t\t: Prints this message.");
        System.out.println(" --version\t: Prints the application version.");
        System.out.println(" --batch (-b)\t: Starts the application in the batch mode.");
        System.out.println("\t\t  Read about the batch mode below.");
        System.out.println();
        System.out.println("By default the application runs in the GUI mode.");
        System.out.println("Here's the list of supported arguments for the GUI mode:");
        new CmdLineParser(new GuiArguments(), argParserProps).printUsage(System.out);
        System.out.println();
        System.out.println("The application also supports the CLI batch mode (aka \"headless\" mode).");
        System.out.println("Here's the list of supported arguments for the batch mode:");
        new CmdLineParser(new CliBatchArguments(), argParserProps).printUsage(System.out);
        System.out.println();
        System.out.println("EXAMPLES");
        System.out.println();
        System.out.println("To pack all atlases from the project:");
        System.out.println("\tgdx-texture-packer --batch --project \"/path/to/project.tpproj\"");
        System.out.println();
        System.out.println("To pack the specific atlases from the project:");
        System.out.println("\tgdx-texture-packer --batch --project \"/path/to/project.tpproj\" --atlases \"atlas_name\" \"another_atlas_name\"");
        System.out.println();
        System.out.println("To get the list of the available atlases in the project:");
        System.out.println("\tgdx-texture-packer --batch --list-atlases --project \"/path/to/project.tpproj\"");
        System.out.println("or a shorter form:");
        System.out.println("\tgdx-texture-packer -b -l -p \"/path/to/project.tpproj\"");
    }

    private static void parseArguments(String[] args, Object argumentsObject) {
        try {
            new CmdLineParser(argumentsObject, argParserProps).parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("Error: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    public static void startCliBatchApp(CliBatchArguments arguments) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.preferencesDirectory = AppConstants.EXTERNAL_DIR;
        config.updatesPerSecond = -1; // Do not call update method.
        new HeadlessApplication(new CliBatchApp(arguments), config);
    }

    public static void startGuiApp(GuiArguments arguments) {
        AppConstants.logFile = LoggerUtils.setupExternalFileOutput();
        LoggerUtils.printGeneralInfo();

        final Lwjgl3ApplicationConfigurationX configuration = new Lwjgl3ApplicationConfigurationX();
        configuration.setTitle(AppConstants.APP_TITLE);
        configuration.setWindowIcon(Files.FileType.Classpath, "icon128.png", "icon32.png", "icon16.png");
        configuration.setPreferencesConfig(AppConstants.EXTERNAL_DIR, Files.FileType.External);
        configuration.setWindowedMode(1280, 800);
        configuration.setWindowSizeLimits(320, 320, -1, -1);
        configuration.setHdpiMode(HdpiMode.Logical);

        AppParams appParams = new AppParams();
        appParams.startupProject = arguments.project;
        appParams.debug = arguments.debug;

        SystemFileOpener systemFileOpener = new SystemFileOpenerImpl();

        App app = new App(appParams, new DesktopClassScanner(), systemFileOpener);

        if (!arguments.disableNativeFileDialogs) {
            // Setup LWJGL native file dialog support.
            app.setNativeFileDialogService(new LwjglFileDialogService());
        }

        try {
            new Lwjgl3Application(new Lwjgl3AppWrapper(app, configuration), configuration);
        } catch (Exception e) {
            if (appParams.debug) throw e;

            e.printStackTrace();

            ErrorReportFrame errorReport = new ErrorReportFrame(configuration, e);
            errorReport.setVisible(true);
        }
    }

    /**
     * A great solution for the dreaded macOS main thread startup issue.
     * Borrowed from <a href="https://github.com/tommyettinger/gdx-liftoff/blob/430f00f5e9caf3b047a5c51926e0d704e8a2571c/src/main/kotlin/com/github/czyzby/setup/main.kt">...</a>
     * <p>
     * Calling this should be done in an if check; if this returns true, the old program should end because a new JVM will
     * take over, but if it returns false, the program should continue normally. It is meant to allow macOS to start with
     * its required '-XstartOnFirstThread' argument, even if the jar wasn't originally started with it.
     * Taken from <a href="https://github.com/crykn/guacamole/blob/master/gdx-desktop/src/main/java/de/damios/guacamole/gdx/StartOnFirstThreadHelper.java">...</a> .
     * Thanks crykn/damios!
     */
    private static boolean startNewJvmIfRequired(final String[] args) {
        if (!UIUtils.isMac) {
            return false;
        }
        long pid = LibC.getpid();

        // check whether -XstartOnFirstThread is enabled
        if ("1".equals(System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid))) {
            return false;
        }
        System.out.println("Restarting the JVM with \"startOnFirstThread\" parameter.");

        // check whether the JVM was previously restarted
        // avoids looping, but most certainly leads to a crash
        if ("true".equals(System.getProperty("jvmIsRestarted"))) {
            System.err.println("There was a problem evaluating whether the JVM was started with the -XstartOnFirstThread argument");
            return false;
        }

        String joinedArgs = Arrays.stream(args).map(s -> {
            if (s.contains(" ")) {
                return "\"" + s + "\"";
            } else {
                return s;
            }
        }).collect(Collectors.joining(" "));

        // Restart the JVM with -XstartOnFirstThread
        ArrayList<String> jvmArgs = new ArrayList<>();
        String separator = System.getProperty("file.separator");
        jvmArgs.add(System.getProperty("java.home") + separator + "bin" + separator + "java");
        jvmArgs.add("-XstartOnFirstThread");
        jvmArgs.add("-DjvmIsRestarted=true");
        jvmArgs.add("-Djava.awt.headless=true");
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        jvmArgs.add("-cp");
        jvmArgs.add(System.getProperty("java.class.path"));
        jvmArgs.add(System.getenv("JAVA_MAIN_CLASS_" + pid));
        jvmArgs.add(joinedArgs);

        try {
            Process process = new ProcessBuilder(jvmArgs).redirectErrorStream(true).start();
            BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = processOutput.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("There was a problem restarting the JVM");
            e.printStackTrace();
        }
        return true;
    }

    private static class SystemFileOpenerImpl implements SystemFileOpener {

        @Override
        public boolean openFile(FileHandle fileHandle) {
            return DesktopFileOpener.open(fileHandle.file());
//            try {
//                Desktop.getDesktop().open(fileHandle.file());
//                return true;
//            } catch (IOException e) {
//                Gdx.app.error("SystemFileOpener", "Error opening file: " + fileHandle, e);
//                return false;
//            }
        }
    }
}
