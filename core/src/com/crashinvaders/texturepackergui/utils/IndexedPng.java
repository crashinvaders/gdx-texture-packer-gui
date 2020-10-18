package com.crashinvaders.texturepackergui.utils;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Utility to work with indexed color mode PNG files.
 * <br>
 * Based of LibGDX's {@link com.badlogic.gdx.graphics.PixmapIO.PNG PixmapIO.PNG}.
 */
public class IndexedPng {
    static private final byte[] SIGNATURE = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
    static private final int IHDR = 0x49484452;
    static private final int IDAT = 0x49444154;
    static private final int IEND = 0x49454E44;
    static private final int PLTE = 0x504C5445;
    static private final int TRNS = 0x74524E53;
    static private final byte COLOR_INDEXED = 3;
    static private final byte COMPRESSION_DEFLATE = 0;
    static private final byte FILTER_NONE = 0;
    static private final byte INTERLACE_NONE = 0;
    static private final byte PAETH = 4;

    /**
     * Writes the indexed color PNG file data to the stream without closing the stream.
     * @param output the stream to write the result to.
     * @param width the width of the image.
     * @param height the height of the image.
     * @param palette an array of colors in int RGBA format.
     *                The array must be sorted so all the values containing transparency (alpha != 0xff) go first.
     * @param colorMap an array of indices pointing to a palette color value.
     *                 Must be of width*height length.
     * @param compressionLevel sets the deflate completion level for packing the image data.
     *                         Acceptable values are 0..9(use constants from {@link Deflater}).
     */
    public static void write(OutputStream output, int width, int height, int[] palette, byte[] colorMap, int compressionLevel) throws IOException {
        if (colorMap.length != width * height) {
            throw new IllegalArgumentException("Color map size doesn't match the target image width & height values.");
        }
        if (palette.length == 0) {
            throw new IllegalArgumentException("Palette is empty.");
        }

        Deflater deflater = new Deflater();
        deflater.setLevel(compressionLevel);

        try (ChunkBuffer buffer = new ChunkBuffer(128 * 128)) {
            DataOutputStream dataOutput = new DataOutputStream(output);
            dataOutput.write(SIGNATURE);

            buffer.writeInt(IHDR);
            buffer.writeInt(width);
            buffer.writeInt(height);
            buffer.writeByte(8); // 8 bits per component.
            buffer.writeByte(COLOR_INDEXED);
            buffer.writeByte(COMPRESSION_DEFLATE);
            buffer.writeByte(FILTER_NONE);
            buffer.writeByte(INTERLACE_NONE);
            buffer.endChunk(dataOutput);

            int alphaValueAmount = palette.length;
            buffer.writeInt(PLTE);
            for (int i = 0; i < palette.length; i++) {
                int rgba = palette[i];
                buffer.write(rgba >>> 24);
                buffer.write(rgba >>> 16);
                buffer.write(rgba >>> 8);

                if (i < alphaValueAmount && (rgba & 0xff) == 0xff) {
                    alphaValueAmount = i;
                }
            }
            buffer.endChunk(dataOutput);

            buffer.writeInt(TRNS);
            for (int i = 0; i < alphaValueAmount; i++) {
                int rgba = palette[i];
                buffer.write(rgba);
            }
            buffer.endChunk(dataOutput);

            buffer.writeInt(IDAT);
            deflater.reset();
            byte[] lineOut = new byte[width];
            byte[] curLine = new byte[width];
            byte[] prevLine = new byte[width];
            DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);

            for (int y = 0; y < height; y++) {
                System.arraycopy(colorMap, y * width , curLine, 0, width);

                lineOut[0] = (byte) (curLine[0] - prevLine[0]);

                //Paeth
                for (int x = 1; x < width; x++) {
                    int a = curLine[x - 1] & 0xff;
                    int b = prevLine[x] & 0xff;
                    int c = prevLine[x - 1] & 0xff;
                    int p = a + b - c;
                    int pa = p - a;
                    if (pa < 0) pa = -pa;
                    int pb = p - b;
                    if (pb < 0) pb = -pb;
                    int pc = p - c;
                    if (pc < 0) pc = -pc;
                    if (pa <= pb && pa <= pc)
                        c = a;
                    else if (pb <= pc)
                        c = b;
                    lineOut[x] = (byte) (curLine[x] - c);
                }

                deflaterOutput.write(PAETH);
                deflaterOutput.write(lineOut, 0, width);

                byte[] temp = curLine;
                curLine = prevLine;
                prevLine = temp;
            }
            deflaterOutput.finish();
            buffer.endChunk(dataOutput);

            buffer.writeInt(IEND);
            buffer.endChunk(dataOutput);

            output.flush();
        } finally {
            deflater.end();
        }
    }

    static class ChunkBuffer extends DataOutputStream {
        final ByteArrayOutputStream buffer;
        final CRC32 crc;

        ChunkBuffer (int initialSize) {
            this(new ByteArrayOutputStream(initialSize), new CRC32());
        }

        private ChunkBuffer (ByteArrayOutputStream buffer, CRC32 crc) {
            super(new CheckedOutputStream(buffer, crc));
            this.buffer = buffer;
            this.crc = crc;
        }

        public void endChunk (DataOutputStream target) throws IOException {
            flush();
            target.writeInt(buffer.size() - 4);
            buffer.writeTo(target);
            target.writeInt((int)crc.getValue());
            buffer.reset();
            crc.reset();
        }
    }
}
