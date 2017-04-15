package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.crashinvaders.common.awt.ImageTools;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppParams;
import com.github.czyzby.autumn.fcs.scanner.DesktopClassScanner;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class AwtDesktopLauncher {
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

				final LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
				configuration.title = "LibGDX Texture Packer GUI";
				configuration.addIcon("icon128.png", Files.FileType.Internal);
				configuration.addIcon("icon32.png", Files.FileType.Internal);
				configuration.addIcon("icon16.png", Files.FileType.Internal);
				configuration.preferencesDirectory = ".gdxtexturepackergui";
				configuration.width = 1024;
				configuration.height = 600;

				AppParams appParams = new AppParams();
				appParams.startupProject = arguments.project;

				App app = new App(new DesktopClassScanner(), appParams);
//				LwjglCanvas canvas = new LwjglCanvas(new WindowParamsPersistingApplicationWrapper(app, configuration), configuration);
				LwjglCanvas canvas = new LwjglCanvas(app, configuration);

				new MainFrame(canvas);
			}
		});
	}

	private static class MainFrame extends JFrame {

		public MainFrame(LwjglCanvas lwjglCanvas) {
			super("LibGDX Texture Packer GUI");

			setIconImage(ImageTools.loadImage("icon128.png"));

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					System.exit(0);
				}
			});

			// Frame layout
			{
				setSize(1024, 768);
				setLocation(0, 0);
//				setExtendedState(JFrame.MAXIMIZED_BOTH);
				setLocationRelativeTo(null);
				setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				setVisible(true);

				getContentPane().add(lwjglCanvas.getCanvas(), BorderLayout.CENTER);
			}
		}
	}

	private static class Arguments {
		@Argument
		File project;
	}
}