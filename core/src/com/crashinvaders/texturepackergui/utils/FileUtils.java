package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.StreamUtils;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    public static void saveTextToFile(FileHandle file, String text) throws IOException {
        org.apache.commons.io.FileUtils.write(file.file(), text, "UTF-8");
    }
    public static void saveTextToFileSilent(FileHandle file, String text) {
        try {
            saveTextToFile(file, text);
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

    public static void packGzip(FileHandle file) throws IOException {
        packGzip(file, file);
    }

    /** @see File#createTempFile(String, String) */
    public static FileHandle createTempFile(String baseName) throws IOException {
        return Gdx.files.absolute(File.createTempFile(baseName, null).getAbsolutePath());
    }

    /** Synchronously downloads file by URL*/
    public static void downloadFile(FileHandle output, String urlString) throws IOException {
//        ReadableByteChannel rbc = null;
//        FileOutputStream fos = null;
//        try {
//            URL url = new URL(urlString);
//            rbc = Channels.newChannel(url.openStream());
//            fos = new FileOutputStream(output.file());
//            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//        } finally {
//            StreamUtils.closeQuietly(rbc);
//            StreamUtils.closeQuietly(fos);
//        }
        InputStream in = null;
        FileOutputStream out = null;
        try {
            URL url = new URL(urlString);
            in = url.openStream();
            out = new FileOutputStream(output.file());
            StreamUtils.copyStream(in, out);
        } finally {
            StreamUtils.closeQuietly(in);
            StreamUtils.closeQuietly(out);
        }
    }

    public static void packGzip(FileHandle input, FileHandle output) throws IOException {
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

    public static void unpackZip(FileHandle input, FileHandle output) throws IOException {
        ZipFile zipFile = new ZipFile(input.file());
        File outputDir = output.file();
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDir, entry.getName());
                if (!entryDestination.toPath().normalize().startsWith(outputDir.toPath().normalize())) {
                    throw new RuntimeException("Bad zip entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    StreamUtils.copyStream(in, out);
                    StreamUtils.closeQuietly(in);
                    out.close();
                }
            }
        } finally {
            StreamUtils.closeQuietly(zipFile);
        }
    }

    public static @Null FileHandle findFirstExistentParent(FileHandle fileHandle) {
        FileHandle parent = fileHandle.parent();
        if (parent == null)
            return null;

        if (parent.exists())
            return parent;

        return findFirstExistentParent(parent);
    }
}
