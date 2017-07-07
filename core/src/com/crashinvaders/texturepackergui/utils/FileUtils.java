package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class FileUtils {

    public static boolean fileExists(FileHandle fileHandle) {
        return fileHandle != null && fileHandle.exists();
    }

    public static boolean fileExists(String absolutePath) {
        return Gdx.files.absolute(absolutePath).exists();
    }

    public static FileHandle obtainIfExists(String absolutePath) {
        FileHandle fileHandle = Gdx.files.absolute(absolutePath);
        return fileHandle.exists() ? fileHandle : null;
    }

    public static class FileTypeFilterBuilder {
        private final FileTypeFilter filter;

        public FileTypeFilterBuilder(boolean allTypeAllowed) {
            filter = new FileTypeFilter(allTypeAllowed);
        }

        public FileTypeFilterBuilder rule(String description, String... extensions) {
            filter.addRule(description, extensions);
            return this;
        }

        public FileTypeFilter get() {
            return filter;
        }
    }

    public static void saveTextToFile(String text, FileHandle file) throws IOException {
        org.apache.commons.io.FileUtils.write(file.file(), text, "UTF-8");
    }
    public static void saveTextToFileSilent(String text, FileHandle file) {
        try {
            saveTextToFile(text, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadTextFromFile(FileHandle file) throws IOException {
        return org.apache.commons.io.FileUtils.readFileToString(file.file(), "UTF-8");
    }
    public static String loadTextFromFileSilent(FileHandle file) {
        try {
            return loadTextFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void gzip(FileHandle file) throws IOException {
        gzip(file, file);
    }

    public static void gzip(FileHandle input, FileHandle output) throws IOException {
        FileInputStream read = null;
        DataOutputStream write = null;
        FileHandle tempFile = null;

        // In case input and output files are the same, we have to remove input file and place it in temporal storage
        if (input.equals(output)) {
            tempFile = new FileHandle(File.createTempFile(input.nameWithoutExtension(), null));
            input.copyTo(tempFile);
            input.delete();
            input = tempFile;
        }

        try {
            File in = input.file(), out = output.file();
            int writtenBytes = 0, length = (int) in.length();
            byte[] buffer = new byte[10 * 1024];
            read = new FileInputStream(in);
            write = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(out)));
            write.writeInt(length);
            while (writtenBytes != length) {
                int nBytesRead = read.read(buffer);
                write.write(buffer, 0, nBytesRead);
                writtenBytes += nBytesRead;
            }
        } finally {
            if (write != null) write.close();
            if (read != null) read.close();
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
