package com.crashinvaders.texturepackergui.desktop.launchers.awt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.AppParams;
import com.crashinvaders.texturepackergui.desktop.Arguments;
import com.crashinvaders.texturepackergui.desktop.LoggerUtils;
import com.github.czyzby.autumn.fcs.scanner.DesktopClassScanner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.awt.*;

public class AwtFrameLauncher {
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

	public static void start(final Arguments arguments) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				AppConstants.logFile = LoggerUtils.setupExternalFileOutput();

				final LwjglCanvasConfiguration config = new LwjglCanvasConfiguration();
				config.title = AppConstants.APP_TITLE;
				config.preferencesDirectory = AppConstants.EXTERNAL_DIR;
				config.iconFilePath = "icon128.png";
				config.width = 1024;
				config.height = 600;
				config.allowSoftwareMode = arguments.softOpenGL;
				config.useHDPI = true;

				AppParams appParams = new AppParams();
				appParams.startupProject = arguments.project;

				App app = new App(new DesktopClassScanner(), appParams);
				CustomLwjglCanvas canvas = new CustomLwjglCanvas(app, config);

				MainFrame mainFrame = new MainFrame(app, canvas, config);
				mainFrame.setVisible(true);
			}
		});

	}
}