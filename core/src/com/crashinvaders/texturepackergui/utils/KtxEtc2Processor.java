
package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.crashinvaders.texturepackergui.utils.FileUtils;

import java.io.*;
import java.net.URI;
import java.util.zip.GZIPOutputStream;

public class KtxEtc2Processor {

    private static final TempFileAccessor fileAccessorEtcTools = new TempExecutableFileAccessor(
            SharedLibraryLoader.isWindows ? "etctool/etctool.exe" :
            SharedLibraryLoader.isLinux ? "etctool/etctool-linux" :
            SharedLibraryLoader.isMac ? "etctool/etctool-mac" : null
            , 0);

    public static void process(FileHandle input, FileHandle output, PixelFormat format) {
        try {
            final URI exe = fileAccessorEtcTools.getExecutableFile().toURI();

            System.out.println("Starting etc2comp");
            String[] cmd = {exe.getPath(), input.file().getAbsolutePath(), "-format", format.stringParam, "-output", output.file().getAbsolutePath()};
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            int result = process.waitFor(); // Let the process finish.
            if (result != 0) {
                throw new RuntimeException("Error executing etc2comp command, result code: " + result);
            }
            System.out.println("etc2comp finished");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error executing etc2comp command: ", e);
        }
    }

    public enum PixelFormat {
        RGB8("RGB8"),
        SRGB8("SRGB8"),
        RGBA8("RGBA8"),
        SRGBA8("SRGBA8"),
        RGB8A1("RGB8A1"),
        SRGB8A1("SRGB8A1"),
        R11("R11"),
        RG11("RG11");

        public final String stringParam;

        PixelFormat(String stringParam) {
            this.stringParam = stringParam;
        }
    }

    /** Extracts and stores internal resource file reference. */
    private static class TempFileAccessor {
        protected static final String PREFERENCES_FILE = "ktx_processor.xml";
        protected static final String PREF_REVISION_SUFFIX = "-revision";

        protected final String fileClassPath;
        protected final int fileRevision;

        protected final String prefKeyFilePath;
        protected final String prefKeyFileRevision;

        protected File tempFile = null;

        /**
         *  @param fileClassPath Target file's class path.
         *  @param fileRevision Number that is stored inside preferences and indicates revision of extracted file.
         *                      This is useful when resource file was changed and needs to be overwritten.
         */
        public TempFileAccessor(String fileClassPath, int fileRevision) {
            this.fileClassPath = fileClassPath;
            this.fileRevision = fileRevision;

            prefKeyFilePath = fileClassPath;
            prefKeyFileRevision = fileClassPath + PREF_REVISION_SUFFIX;
        }

        public synchronized File getExecutableFile() {
            // Check if file previously was extracted and ready to be reused
            if (tempFile != null && tempFile.exists()) return tempFile;

            tempFile = loadTempFilePath();
            if (tempFile != null && tempFile.exists()) return tempFile;

            tempFile = copyToTempFile(fileClassPath);
            if (tempFile != null) {
                System.out.println("File " + fileClassPath + " temporary extracted to " + tempFile);
                saveTempFilePath(tempFile);
                return tempFile;
            }

            throw new IllegalStateException("Can't access/extract file: " + fileClassPath);
        }

        protected void saveTempFilePath(File file) {
            try {
                Preferences preferences = Gdx.app.getPreferences(PREFERENCES_FILE);
                preferences
                        .putString(prefKeyFilePath, file.getAbsolutePath())
                        .putInteger(prefKeyFileRevision, fileRevision)
                        .flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected File loadTempFilePath() {
            try {
                Preferences preferences = Gdx.app.getPreferences(PREFERENCES_FILE);
                int revision = preferences.getInteger(prefKeyFileRevision, -1);
                // Check if the revisions are the same
                if (this.fileRevision != revision) return null;

                String absolutePath = preferences.getString(prefKeyFilePath, null);
                if (absolutePath != null) {
                    return new File(absolutePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected File copyToTempFile(String resourcePath) {
            File temp;
            InputStream in = null;
            FileOutputStream fos = null;
            try {
                in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
                byte[] buffer = new byte[1024];
                int read;
                temp = File.createTempFile(resourcePath, "");
                fos = new FileOutputStream(temp);

                while ((read = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignored) {
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return temp;
        }
    }

    private static class TempExecutableFileAccessor extends TempFileAccessor {
        /** @see TempFileAccessor#TempFileAccessor(String, int) */
        public TempExecutableFileAccessor(String fileClassPath, int fileRevision) {
            super(fileClassPath, fileRevision);
        }

        @Override
        protected File copyToTempFile(String resourcePath) {
            File file = super.copyToTempFile(resourcePath);
            if (file == null) return null;

            if (SharedLibraryLoader.isLinux || SharedLibraryLoader.isMac) {
                try {
                    // Change access permission for temp file
                    System.out.println("Call \"chmod\" for a temp file");
                    Process process = Runtime.getRuntime().exec("chmod +x " + file.getAbsolutePath());

                    InputStream is = process.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                    int result = process.waitFor(); // Let the process finish.

                    if (result != 0) {
                        throw new RuntimeException("\"chmod\" call finished with error");
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Error changing file access permission for: " + file.getAbsolutePath(), e);
                }
            }
            return file;
        }
    }
}
