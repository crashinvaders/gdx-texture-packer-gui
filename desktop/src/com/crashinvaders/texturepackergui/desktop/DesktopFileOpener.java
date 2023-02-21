package com.crashinvaders.texturepackergui.desktop;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Utility methods to call OS default actions for files and folders.
 * <p/>
 * Found <a href="http://forum.lwjgl.org/index.php?topic=7179.0">on the web</a>
 */
public final class DesktopFileOpener {
    /**
     * Opens the file using OS default action.
     * @return true if the operation was successful.
     */
    public static boolean open(File file) {
        if (file == null)
            throw new IllegalArgumentException("File is null.");

        if (openDesktop(file) || openSystem(file.getPath())) {
            return true;
        }

        System.err.println("Unable to open file: \"" + file.getAbsolutePath() + "\"");
        return false;
    }

    private static boolean openSystem(String path) {

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            return (run("explorer", "%s", path));
        }

        if (os.contains("mac")) {
            return (run("open", "%s", path));
        }

        return run("kde-open", "%s", path) || run("gnome-open", "%s", path) || run("xdg-open", "%s", path);
    }

    private static boolean openDesktop(File file) {

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(file);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    private static boolean run(String command, String arg, String file) {

        String[] args = arg.split(" ");
        String[] parts = new String[args.length + 1];
        parts[0] = command;
        for (int i = 0; i < args.length; i++) {
            parts[i + 1] = String.format(args[0], file).trim();
        }

        try {
            Process p = Runtime.getRuntime().exec(parts);
            if (p == null)
                return false;

            try {
                if (p.exitValue() == 0)
                    return true;
                return false;
            } catch (IllegalThreadStateException e) {
                return true;
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
    }

}
