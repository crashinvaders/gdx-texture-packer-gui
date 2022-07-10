package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.graphics.GL20;
import com.crashinvaders.common.OutputStreamMultiplexer;
import com.crashinvaders.texturepackergui.AppConstants;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerUtils {
    private static final long EXPIRATION_THRESHOLD = 7 * 24 * 60 * 60 * 1000; // 7 days
    private static final String LOG_FILE_EXTENSION = ".txt";

    public static File setupExternalFileOutput() {

        PrintStream origOut = System.out;
        PrintStream origErr = System.out;

        try {
            Date currentDate = new Date();
            long currentTime = currentDate.getTime();
            String fileName = new SimpleDateFormat("yyMMddhhmm").format(currentDate) + ".log";
            File logFile = new File(Lwjgl3Files.externalPath + AppConstants.LOGS_DIR + File.separator + fileName);
            logFile.getParentFile().mkdirs();
            // Try delete old log files.
            for (File oldLogFile : logFile.getParentFile().listFiles((FileFilter)new SuffixFileFilter(LOG_FILE_EXTENSION))) {
                long lastModified = oldLogFile.lastModified();
                if (currentTime - lastModified > EXPIRATION_THRESHOLD) {
                    try { oldLogFile.delete(); } catch (SecurityException ignored) { }
                }
            }
            System.out.println("External log file: " + logFile.getAbsolutePath());
            FileOutputStream logFileOutputStream = new FileOutputStream(logFile);
            System.setOut(new PrintStream(new OutputStreamMultiplexer(
                    System.out,
                    logFileOutputStream
            )));
            System.setErr(new PrintStream(new OutputStreamMultiplexer(
                    System.err,
                    logFileOutputStream
            )));
            return logFile;
        } catch (Exception e) {
            System.setOut(origOut);
            System.setErr(origErr);

            System.err.println("Can't setup logging to external file.");
            e.printStackTrace();
            return null;
        }
    }

    public static void printGeneralInfo() {
        System.out.println("Version: " + AppConstants.version);
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        System.out.println("JRE: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
    }

    public static void printGpuInfo() {
        if (Gdx.gl == null) throw new IllegalStateException("libGDX is not initialized yet.");

        System.out.println("GPU: " + Gdx.gl.glGetString( GL20.GL_RENDERER));
        System.out.println("OpenGL vendor: " + Gdx.gl.glGetString( GL20.GL_VENDOR));
        System.out.println("OpenGL version: " + Gdx.gl.glGetString( GL20.GL_VERSION));
    }
}