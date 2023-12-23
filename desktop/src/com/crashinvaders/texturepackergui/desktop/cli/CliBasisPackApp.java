package com.crashinvaders.texturepackergui.desktop.cli;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.TimeUtils;
import com.crashinvaders.basisu.BasisuWrapper;
import com.crashinvaders.common.basisu.BasisuNativeLibLoader;
import com.crashinvaders.texturepackergui.desktop.CliBasisPackArguments;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class CliBasisPackApp extends ApplicationAdapter {
    private static final String TAG = CliBasisPackApp.class.getSimpleName();

    private final CliBasisPackArguments args;

    public CliBasisPackApp(CliBasisPackArguments args) {
        this.args = args;
    }

    @Override
    public void create() {
        super.create();

        long startTime = TimeUtils.millis();

        FileHandle inputFile = Gdx.files.absolute(args.inputFile.getAbsolutePath());

        if (!inputFile.exists()) {
            Gdx.app.error(TAG, "Input file doesn't exist: " + inputFile.path());
            System.exit(1);
        }
        String inputFileExtension = inputFile.extension();
        if (inputFile.isDirectory() || !(
                Objects.equals(inputFileExtension, "png") ||
                Objects.equals(inputFileExtension, "jpg") ||
                Objects.equals(inputFileExtension, "jpeg")
        )) {
            Gdx.app.error(TAG, "Input file is not a PNG or JPEG image: " + inputFile.path());
            System.exit(1);
        }

        FileHandle outputFile;
        if (args.outputFile != null) {
            outputFile = Gdx.files.absolute(args.outputFile.getAbsolutePath());
        } else {
            String extension;
            switch (args.container) {
                case KTX2:
                    extension = "ktx2";
                    break;
                case BASIS:
                    extension = "png";
                    break;
                default:
                    throw new IllegalStateException("Unexpected Basis container: " + args.container);
            }
            outputFile = inputFile.sibling(inputFile.nameWithoutExtension() + "." + extension);
        }

        ByteBuffer rgbaBuffer;
        int width;
        int height;
        try (InputStream is = inputFile.read()) {
            BufferedImage image = ImageIO.read(is);
            width = image.getWidth();
            height = image.getHeight();
            byte[] bytes = pngToRgbaBytes(image);
            rgbaBuffer = asByteBuffer(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input file.", e);
        }

        Gdx.app.log(TAG, String.format("Begin Basis compression using container: \"%s\", and intermediate format: \"%s\".",
                args.container.toString(), args.format.toString()));

        BasisuNativeLibLoader.loadIfNeeded();
        ByteBuffer basisBuffer = BasisuWrapper.encode(
                rgbaBuffer, width, height,
                args.format == CliBasisPackArguments.BasisFormat.UASTC,
                args.container == CliBasisPackArguments.BasisContainer.KTX2,
                false, args.etcCompression, false, false,
                args.mipmaps, 0.5f,
                args.etcQuality, 0, 0);

        try {
            saveFile(basisBuffer, outputFile.file());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write output file.", e);
        }

        BasisuWrapper.disposeNativeBuffer(basisBuffer);

        long totalTime = TimeUtils.timeSinceMillis(startTime);

        Gdx.app.log(TAG, String.format("Total time: %.1f seconds.", totalTime/1000f));
        Gdx.app.log(TAG, String.format("File size: %.0f KiB.", outputFile.length()/(1024f)));
        Gdx.app.log(TAG, "Texture written to " + outputFile.file().getAbsolutePath());
    }

    private static byte[] pngToRgbaBytes(BufferedImage image) throws IOException {
        // RGBA bytes.
        byte[] bytes = new byte[image.getWidth() * image.getHeight() * 4];

        for (int y = 0; y < image.getHeight(); y++) {
            final int rowStartIdx = y * image.getWidth() * 4;
            for (int x = 0; x < image.getWidth(); x++) {
                int pixelIndex = rowStartIdx + x * 4;

                int argb = image.getRGB(x, y);
                bytes[pixelIndex + 0] = (byte)((argb >> 16) & 0xff);   // R
                bytes[pixelIndex + 1] = (byte)((argb >> 8)  & 0xff);   // G
                bytes[pixelIndex + 2] = (byte)((argb >> 0)  & 0xff);   // B
                bytes[pixelIndex + 3] = (byte)((argb >> 24) & 0xff);   // A
            }
        }

        return bytes;
    }

    private static ByteBuffer asByteBuffer(byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(data);
        buffer.limit(buffer.capacity());
        buffer.position(0);
        return buffer;
    }

    public static void saveFile(ByteBuffer data, File outFile) throws IOException {
        outFile.getParentFile().mkdirs();

        data.position(0);
        data.limit(data.capacity());
        try (FileOutputStream os = new FileOutputStream(outFile)) {
            while (data.hasRemaining()) {
                os.write(data.get());
            }
        }
        data.position(0);
    }
}
