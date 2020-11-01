package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfigurationX;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.AppParams;
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

    public static void main(final String[] args) {
        if(startNewJvmIfRequired(args)) return;

        Arguments arguments = new Arguments();
        try {
            CmdLineParser parser = new CmdLineParser(arguments);
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println("Error: " + e.getLocalizedMessage());
            return;
        }
        start(arguments);
    }

    public static void start(Arguments arguments) {
        AppConstants.logFile = LoggerUtils.setupExternalFileOutput();
        LoggerUtils.printGeneralInfo();

        final Lwjgl3ApplicationConfigurationX configuration = new Lwjgl3ApplicationConfigurationX();
        configuration.setTitle(AppConstants.APP_TITLE);
        configuration.setWindowIcon(Files.FileType.Classpath, "icon128.png", "icon32.png", "icon16.png");
        configuration.setPreferencesConfig(AppConstants.EXTERNAL_DIR, Files.FileType.External);
        configuration.setWindowedMode(1280, 800);
        configuration.setHdpiMode(HdpiMode.Logical);

        AppParams appParams = new AppParams();
        appParams.startupProject = arguments.project;
        appParams.debug = arguments.debug;

        App app = new App(new DesktopClassScanner(), appParams);

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
     * A great solution for the dreaded MacOS main thread startup issue.
     * Borrowed from https://github.com/tommyettinger/gdx-liftoff/blob/430f00f5e9caf3b047a5c51926e0d704e8a2571c/src/main/kotlin/com/github/czyzby/setup/main.kt
     *
     * Calling this should be done in an if check; if this returns true, the old program should end because a new JVM will
     * take over, but if it returns false, the program should continue normally. It is meant to allow MacOS to start with
     * its required '-XstartOnFirstThread' argument, even if the jar wasn't originally started with it.
     * Taken from https://github.com/crykn/guacamole/blob/master/gdx-desktop/src/main/java/de/damios/guacamole/gdx/StartOnFirstThreadHelper.java .
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
            System.out.println("The JVM has successfully restarted.");
        } catch (Exception e) {
            System.err.println("There was a problem restarting the JVM");
            e.printStackTrace();
        }
        return true;
    }
}
