package com.crashinvaders.texturepackergui.desktoplwjgl3;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfigurationX;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.AppParams;
import com.github.czyzby.autumn.fcs.scanner.DesktopClassScanner;
import org.kohsuke.args4j.*;

public class ApplicationStarter {
    public static void main(final String[] args) {
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

        final Lwjgl3ApplicationConfigurationX configuration = new Lwjgl3ApplicationConfigurationX();
        configuration.setTitle(AppConstants.APP_TITLE);
        configuration.setWindowIcon(Files.FileType.Classpath, "icon128.png", "icon32.png", "icon16.png");
        configuration.setPreferencesConfig(AppConstants.EXTERNAL_DIR, Files.FileType.External);
        configuration.setWindowedMode(1024, 600);
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
}
