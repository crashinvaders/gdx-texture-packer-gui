package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.tools.texturepacker.PngPageFileWriter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.BufferUtils;
import com.crashinvaders.basisu.BasisuWrapper;
import com.crashinvaders.common.basisu.BasisuGdxException;
import com.crashinvaders.common.basisu.BasisuNativeLibLoader;
import com.crashinvaders.texturepackergui.controllers.model.FileTypeType;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.BasisuFileTypeModel;
import com.crashinvaders.texturepackergui.utils.SystemUtils;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.crashinvaders.texturepackergui.utils.SystemUtils.CpuArch;
import static com.crashinvaders.texturepackergui.utils.SystemUtils.OperatingSystem;

public class BasisuFileTypeProcessor implements PackProcessor {

    private static final boolean SYSTEM_SUPPORTED =
            SystemUtils.check(OperatingSystem.Windows, CpuArch.Amd64) ||
            SystemUtils.check(OperatingSystem.Linux, CpuArch.X86, CpuArch.Amd64) ||
            SystemUtils.check(OperatingSystem.MacOS, CpuArch.Amd64);

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        if (project.getFileType().getClass() != BasisuFileTypeModel.class) return;

        if (!isBasisuSupported()) {
            throw new IllegalStateException("Basis Universal natives are not supported on the current system: " + SystemUtils.getPrintString());
        }

        BasisuFileTypeModel fileType = project.getFileType();

        pack.getSettings().format = Pixmap.Format.RGBA8888;

        node.setPageFileWriter(new BasisPageFileWriter(
                fileType.isUastc(),
                fileType.getCompressionLevel(),
                fileType.getQualityLevel()
        ));
    }

    public static boolean isBasisuSupported() {
        return SYSTEM_SUPPORTED;
    }

    public static class BasisPageFileWriter extends PngPageFileWriter {

        private final boolean uastc;
        private final int compressionLevel;
        private final int qualityLevel;

        public BasisPageFileWriter(boolean uastc, int compressionLevel, int qualityLevel) {
            this.uastc = uastc;
            this.compressionLevel = compressionLevel;
            this.qualityLevel = qualityLevel;
        }

        @Override
        public String getFileExtension() {
            return FileTypeType.BASIS.key;
        }

        @Override
        public void saveToFile(TexturePacker.Settings settings, BufferedImage image, File file) throws IOException {
            FileHandle output = new FileHandle(file);

            final byte[] rgbaBytes = bufferedImageToRgbaBytes(image);
            ByteBuffer rgbaBuffer = BufferUtils.newUnsafeByteBuffer(rgbaBytes.length);
            rgbaBuffer.put(rgbaBytes);

            BasisuNativeLibLoader.loadIfNeeded();
            ByteBuffer encodedBuffer = BasisuWrapper.encode(rgbaBuffer, image.getWidth(), image.getHeight(),
                    uastc, false, compressionLevel, false, false, false, 2f, qualityLevel, 0, 0);

            BufferUtils.disposeUnsafeByteBuffer(rgbaBuffer);

            saveFile(encodedBuffer, output);
        }

        private static byte[] bufferedImageToRgbaBytes(BufferedImage image) {
            // RGBA bytes.
            byte[] bytes = new byte[image.getWidth() * image.getHeight() * 4];

            for (int y = 0; y < image.getHeight(); y++) {
                final int rowStartIdx = y * image.getWidth() * 4;
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixelIndex = rowStartIdx + x * 4;

                    int argb = image.getRGB(x, y);
                    bytes[pixelIndex + 0] = (byte) ((argb >> 16) & 0xff);   // R
                    bytes[pixelIndex + 1] = (byte) ((argb >> 8) & 0xff);   // G
                    bytes[pixelIndex + 2] = (byte) ((argb >> 0) & 0xff);   // B
                    bytes[pixelIndex + 3] = (byte) ((argb >> 24) & 0xff);   // A
                }
            }

            return bytes;
        }

        private static void saveFile(ByteBuffer data, FileHandle file) {
            file.parent().mkdirs();

            data.position(0);
            data.limit(data.capacity());
            try (FileOutputStream os = new FileOutputStream(file.file())) {
                while (data.hasRemaining()) {
                    os.write(data.get());
                }
            } catch (IOException e) {
                throw new BasisuGdxException("Failed to save image " + file.file().getAbsolutePath(), e);
            }
            data.position(0);
        }
    }
}
