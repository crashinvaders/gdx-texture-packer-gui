package com.crashinvaders.texturepackergui.desktop;

import com.crashinvaders.texturepackergui.desktop.launchers.awt.AwtFrameLauncher;
import com.crashinvaders.texturepackergui.desktop.launchers.glsurface.GLSurfaceLauncher;
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

        switch (arguments.launcher) {
            case Arguments.LAUNCHER_AWT: {
                System.out.println("AWT launcher is starting");
                AwtFrameLauncher.main(args);
                break;
            }
            case Arguments.LAUNCHER_GL_SURFACE: {
                System.out.println("GL surface launcher is starting");
                GLSurfaceLauncher.main(args);
                break;
            }
            default:
                throw new IllegalStateException("Unknown launcher name: " + arguments.launcher);
        }
    }

}
