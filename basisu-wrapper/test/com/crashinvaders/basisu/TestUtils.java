package com.crashinvaders.basisu;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestUtils {
    private static final String OUTPUT_ROOT = "test-output/";

    public static void saveImagePng(BufferedImage image, String fileName) {
        File outFile = new File(OUTPUT_ROOT + fileName + ".png");
        outFile.getParentFile().mkdirs();
        try {
            ImageIO.write(image, "PNG", outFile);
            System.out.println("Image has been saved to " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save image " + outFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static void saveFile(ByteBuffer data, String fileName) {
        File outFile = new File(OUTPUT_ROOT + fileName);
        outFile.getParentFile().mkdirs();

        data.position(0);
        data.limit(data.capacity());
        try (FileOutputStream os = new FileOutputStream(outFile)) {
            while (data.hasRemaining()) {
                os.write(data.get());
            }
            System.out.println("File has been saved to " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save image " + outFile.getAbsolutePath());
            e.printStackTrace();
        }
        data.position(0);
    }

    // RGBA layout. 2 bytes per pixel.
    public static BufferedImage fromRgba4444(byte[] rgba, int width, int height) {
        if (rgba.length != width * height * 2) {
            throw new IllegalArgumentException("RGBA image data doesn't match the required size for the specified width & height.");
        }

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            final int rowStartIdx = y * bufferedImage.getWidth() * 2;
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int pixelIdx = rowStartIdx + x * 2;
                int a = (rgba[pixelIdx + 0] >> 0) & 0x0f; a = a | a << 4;
                int b = (rgba[pixelIdx + 0] >> 4) & 0x0f; b = b | b << 4;
                int g = (rgba[pixelIdx + 1] >> 0) & 0x0f; g = g | g << 4;
                int r = (rgba[pixelIdx + 1] >> 4) & 0x0f; r = r | r << 4;
                int argb = a << 24 | r << 16 | g << 8 | b;
                bufferedImage.setRGB(x, y, argb);
            }
        }
        return bufferedImage;
    }

    // RGBA layout. 4 bytes per pixel.
    public static BufferedImage fromRgba8888(ByteBuffer rgba, int width, int height) {
        if (rgba.capacity() != width * height * 4) {
            throw new IllegalArgumentException("RGBA image data doesn't match the required size for the specified width & height.");
        }

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            final int rowStartIdx = y * bufferedImage.getWidth() * 4;
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int pixelIdx = rowStartIdx + x * 4;
                int r = rgba.get(pixelIdx + 0) & 0xff;
                int g = rgba.get(pixelIdx + 1) & 0xff;
                int b = rgba.get(pixelIdx + 2) & 0xff;
                int a = rgba.get(pixelIdx + 3) & 0xff;
                int argb = a << 24 | r << 16 | g << 8 | b;
                bufferedImage.setRGB(x, y, argb);
            }
        }
        return bufferedImage;
    }

    public static byte[] readToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    public static ByteBuffer asByteBuffer(byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(data);
        buffer.limit(buffer.capacity());
        buffer.position(0);
        return buffer;
    }

    /** Checks if the dimensions are equal and are power of two. */
    public static boolean isSquareAndPowerOfTwo(int width, int height) {
        return width == height && (width != 0 && (width & width - 1) == 0);
    }

    public static void writeBytesToTextFile(ByteBuffer bytes, String path) {
        try (PrintWriter pw = new PrintWriter(path, "UTF-8")) {

            bytes.position(0);
            while (bytes.hasRemaining()) {
                pw.append("0x");

                String value = Integer.toHexString(bytes.get());
                if (value.length() > 2) {
                    value = value.substring(value.length() - 2);
                }
                while (value.length() < 2) {
                    value = "0" + value;
                }

                pw.append(value);

                if (bytes.hasRemaining()) {
                    pw.append(", ");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] pngToRgbaBytes(InputStream is) throws IOException {
        BufferedImage image = ImageIO.read(is);

        // RGBA bytes.
        byte[] bytes = new byte[image.getWidth() * image.getHeight() * 4];

        for (int y = 0; y < image.getWidth(); y++) {
            final int rowStartIdx = y * image.getWidth() * 4;
            for (int x = 0; x < image.getHeight(); x++) {
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
}
