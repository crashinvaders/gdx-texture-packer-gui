package com.crashinvaders.texturepackergui.desktop.launchers.awt;

import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppParams;
import com.crashinvaders.texturepackergui.desktop.Arguments;
import com.github.czyzby.autumn.fcs.scanner.DesktopClassScanner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.awt.*;

public class AwtFrameLauncher {
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Arguments arguments = new Arguments();
				try {
					CmdLineParser parser = new CmdLineParser(arguments);
					parser.parseArgument(args);
				} catch (CmdLineException e) {
					System.out.println("Error: " + e.getLocalizedMessage());
					return;
				}

				final LwjglCanvasConfiguration config = new LwjglCanvasConfiguration();
				config.title = "LibGDX Texture Packer GUI";
				config.preferencesDirectory = ".gdxtexturepackergui";
				config.iconFilePath = "icon128.png";
				config.width = 1024;
				config.height = 600;

				AppParams appParams = new AppParams();
				appParams.startupProject = arguments.project;

				App app = new App(new DesktopClassScanner(), appParams);
				LwjglCanvas canvas = new CustomLwjglCanvas(app, config);

				new MainFrame(app, canvas.getCanvas(), config);
			}
		});
	}

}