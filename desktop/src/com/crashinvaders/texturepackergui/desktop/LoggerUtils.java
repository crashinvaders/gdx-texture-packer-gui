package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.crashinvaders.common.OutputStreamMultiplexer;
import com.crashinvaders.texturepackergui.AppConstants;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerUtils {
    private static final long EXPIRATION_THRESHOLD = 7 * 24 * 60 * 60 * 1000; // 7 days
    private static final String LOG_FILE_EXTENSION = ".log";

    public static void setupExternalFileOutput() {
        try {
            Date currentDate = new Date();
            long currentTime = currentDate.getTime();
            String fileName = new SimpleDateFormat("yyMMddhhmm").format(currentDate) + ".log";
            File logFile = new File(LwjglFiles.externalPath + AppConstants.LOGS_DIR + File.separator + fileName);
            logFile.getParentFile().mkdirs();
            for (File oldLogFile : logFile.getParentFile().listFiles((FileFilter)new SuffixFileFilter(LOG_FILE_EXTENSION))) {
                long lastModified = oldLogFile.lastModified();
                if (currentTime - lastModified > EXPIRATION_THRESHOLD) {
                    try { oldLogFile.delete(); } catch (SecurityException ignored) { }
                }
            }
            FileOutputStream logFileOutputStream = new FileOutputStream(logFile);
            System.setOut(new PrintStream(new OutputStreamMultiplexer(
                    new FileOutputStream(FileDescriptor.out),
                    logFileOutputStream
            )));
            System.setErr(new PrintStream(new OutputStreamMultiplexer(
                    new FileOutputStream(FileDescriptor.err),
                    logFileOutputStream
            )));
            System.out.println("Version: " + AppConstants.version);
            System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
            System.out.println("JRE: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
            System.out.println("External log file: " + logFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            System.err.println("Can't setup logging to external file.");
            e.printStackTrace();
        }
    }
}
