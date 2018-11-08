package com.crashinvaders.texturepackergui.utils;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static com.crashinvaders.texturepackergui.utils.PaletteReducer.randomXi;

/** PNG-8 encoder with compression. An instance can be reused to encode multiple PNGs with minimal allocation.
 * You can configure the target palette and how this can dither colors via the {@link #palette} field, which is a
 * {@link PaletteReducer} object that is allowed to be null and can be reused. The methods
 * {@link PaletteReducer#exact(Color[])} or {@link PaletteReducer#analyze(Pixmap)} can be used to make the target
 * palette match a specific set of colors or the colors in an existing image. You can use
 * {@link PaletteReducer#setDitherStrength(float)} to reduce (or increase) dither strength; the dithering algorithm used
 * here is a modified version of the algorithm presented in "Simple gradient-based error-diffusion method" by Xaingyu Y.
 * Hu in the Journal of Electronic Imaging, 2016. This algorithm uses pseudo-randomly-generated noise (it is
 * deterministic, and is seeded using the color information) to adjust Floyd-Steinberg dithering. It yields
 * surprisingly non-random-looking dithers, but still manages to break up artificial patterns most of the time.
 * Note that much of the time you will want to use {@link #writePrecisely(FileHandle, Pixmap, boolean)} instead of
 * {@link #write(FileHandle, Pixmap, boolean, boolean)}, since writePrecisely will attempt to reproduce the exact colors
 * if there are 256 colors or less in the Pixmap, and will automatically change to calling write() if there are more
 * than 256 colors.
 * <br>
 * From LibGDX in the class PixmapIO, with modifications to support indexed-mode files, dithering, and other features.
 * <pre>
 * Copyright (c) 2007 Matthias Mann - www.matthiasmann.de
 * Copyright (c) 2014 Nathan Sweet
 * Copyright (c) 2018 Tommy Ettinger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * </pre>
 * @author Matthias Mann
 * @author Nathan Sweet
 * @author Tommy Ettinger (PNG-8 parts only) */
public class PNG8 implements Disposable {
    static private final byte[] SIGNATURE = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
    static private final int IHDR = 0x49484452, IDAT = 0x49444154, IEND = 0x49454E44,
            PLTE = 0x504C5445, TRNS = 0x74524E53;
    static private final byte COLOR_INDEXED = 3;
    static private final byte COMPRESSION_DEFLATE = 0;
    static private final byte FILTER_NONE = 0;
    static private final byte INTERLACE_NONE = 0;
    static private final byte PAETH = 4;

    private final ChunkBuffer buffer;
    private final Deflater deflater;
    private ByteArray lineOutBytes, curLineBytes, prevLineBytes;
    private boolean flipY = false;
    private int lastLineLen;

    public PaletteReducer palette;

    public PNG8() {
        this(128 * 128);
    }

    public PNG8(int initialBufferSize) {
        buffer = new ChunkBuffer(initialBufferSize);
        deflater = new Deflater();
    }

    /** If true, the resulting PNG is flipped vertically. Default is true. */
    public void setFlipY (boolean flipY) {
        this.flipY = flipY;
    }

    /** Sets the deflate compression level. Default is {@link Deflater#DEFAULT_COMPRESSION}. */
    public void setCompression (int level) {
        deflater.setLevel(level);
    }

    /**
     * Writes the given Pixmap to the requested FileHandle, computing an 8-bit palette from the most common colors in
     * pixmap. If there are 256 or less colors and none are transparent, this will use 256 colors in its palette exactly
     * with no transparent entry, but if there are more than 256 colors or any are transparent, then one color will be
     * used for "fully transparent" and 255 opaque colors will be used.
     * @param file a FileHandle that must be writable, and will have the given Pixmap written as a PNG-8 image
     * @param pixmap a Pixmap to write to the given file
     * @throws IOException if file writing fails for any reason
     */
    public void write (FileHandle file, Pixmap pixmap) throws IOException {
        write(file, pixmap, true);
    }

    /**
     * Writes the given Pixmap to the requested FileHandle, optionally computing an 8-bit palette from the most common
     * colors in pixmap. When computePalette is true, if there are 256 or less colors and none are transparent, this
     * will use 256 colors in its palette exactly with no transparent entry, but if there are more than 256 colors or
     * any are transparent, then one color will be used for "fully transparent" and 255 opaque colors will be used. When
     * computePalette is false, this uses the last palette this had computed, or a 253-color bold palette with one
     * fully-transparent color if no palette had been computed yet.
     * @param file a FileHandle that must be writable, and will have the given Pixmap written as a PNG-8 image
     * @param pixmap a Pixmap to write to the given file
     * @param computePalette if true, this will analyze the Pixmap and use the most common colors
     * @throws IOException if file writing fails for any reason
     */
    public void write (FileHandle file, Pixmap pixmap, boolean computePalette) throws IOException {
        OutputStream output = file.write(false);
        try {
            write(output, pixmap, computePalette);
        } finally {
            StreamUtils.closeQuietly(output);
        }
    }
    /**
     * Writes the pixmap to the stream without closing the stream, optionally computing an 8-bit palette from the given
     * Pixmap. If {@link #palette} is null (the default unless it has been assigned a PaletteReducer value), this will
     * compute a palette from the given Pixmap regardless of computePalette. Optionally dithers the result if
     * {@code dither} is true.
     * @param file a FileHandle that must be writable, and will have the given Pixmap written as a PNG-8 image
     * @param pixmap a Pixmap to write to the given output stream
     * @param computePalette if true, this will analyze the Pixmap and use the most common colors
     * @param dither true if this should dither colors that can't be represented exactly
     * @throws IOException if file writing fails for any reason
     */
    public void write (FileHandle file, Pixmap pixmap, boolean computePalette, boolean dither) throws IOException {
        OutputStream output = file.write(false);
        try {
            write(output, pixmap, computePalette, dither);
        } finally {
            StreamUtils.closeQuietly(output);
        }
    }
    /**
     * Writes the pixmap to the stream without closing the stream, optionally computing an 8-bit palette from the given
     * Pixmap. If {@link #palette} is null (the default unless it has been assigned a PaletteReducer value), this will
     * compute a palette from the given Pixmap regardless of computePalette. Uses the given threshold while analyzing
     * the palette if this needs to compute a palette; threshold values can be as low as 0 to try to use as many colors
     * as possible (prefer {@link  #writePrecisely(FileHandle, Pixmap, boolean, int)} for that, though) and can range up
     * to very high numbers if very few colors should be used; usually threshold is from 100 to 800. Optionally dithers
     * the result if {@code dither} is true.
     * @param file a FileHandle that must be writable, and will have the given Pixmap written as a PNG-8 image
     * @param pixmap a Pixmap to write to the given output stream
     * @param computePalette if true, this will analyze the Pixmap and use the most common colors
     * @param dither true if this should dither colors that can't be represented exactly
     * @param threshold the analysis threshold to use if computePalette is true (min 0, practical max is over 100000)
     * @throws IOException if file writing fails for any reason
     */
    public void write (FileHandle file, Pixmap pixmap, boolean computePalette, boolean dither, int threshold) throws IOException {
        OutputStream output = file.write(false);
        try {
            write(output, pixmap, computePalette, dither, threshold);
        } finally {
            StreamUtils.closeQuietly(output);
        }
    }

    /** Writes the pixmap to the stream without closing the stream and computes an 8-bit palette from the Pixmap.
     * @param output an OutputStream that will not be closed
     * @param pixmap a Pixmap to write to the given output stream
     */
    public void write (OutputStream output, Pixmap pixmap) throws IOException {
        writePrecisely(output, pixmap, true);
    }

    /**
     * Writes the pixmap to the stream without closing the stream, optionally computing an 8-bit palette from the given
     * Pixmap. If {@link #palette} is null (the default unless it has been assigned a PaletteReducer value), this will
     * compute a palette from the given Pixmap regardless of computePalette.
     * @param output an OutputStream that will not be closed
     * @param pixmap a Pixmap to write to the given output stream
     * @param computePalette if true, this will analyze the Pixmap and use the most common colors
     */
    public void write (OutputStream output, Pixmap pixmap, boolean computePalette) throws IOException
    {
        if(computePalette)
            writePrecisely(output, pixmap, true);
        else
            write(output, pixmap, false, true);
    }

    /**
     * Writes the pixmap to the stream without closing the stream, optionally computing an 8-bit palette from the given
     * Pixmap. If {@link #palette} is null (the default unless it has been assigned a PaletteReducer value), this will
     * compute a palette from the given Pixmap regardless of computePalette.
     * @param output an OutputStream that will not be closed
     * @param pixmap a Pixmap to write to the given output stream
     * @param computePalette if true, this will analyze the Pixmap and use the most common colors
     * @param dither true if this should dither colors that can't be represented exactly
     */
    public void write (OutputStream output, Pixmap pixmap, boolean computePalette, boolean dither) throws IOException
    {
        write(output, pixmap, computePalette, dither, 400);
    }
    /**
     * Writes the pixmap to the stream without closing the stream, optionally computing an 8-bit palette from the given
     * Pixmap. If {@link #palette} is null (the default unless it has been assigned a PaletteReducer value), this will
     * compute a palette from the given Pixmap regardless of computePalette.
     * @param output an OutputStream that will not be closed
     * @param pixmap a Pixmap to write to the given output stream
     * @param computePalette if true, this will analyze the Pixmap and use the most common colors
     * @param dither true if this should dither colors that can't be represented exactly
     * @param threshold the analysis threshold to use if computePalette is true (min 0, practical max is over 100000)
     */
    public void write (OutputStream output, Pixmap pixmap, boolean computePalette, boolean dither, int threshold) throws IOException
    {
        if(palette == null)
        {
            palette = new PaletteReducer(pixmap, threshold);
        }
        else if(computePalette)
        {
            palette.analyze(pixmap, threshold);
        }

        if(dither) writeDithered(output, pixmap);
        else writeSolid(output, pixmap);
    }
    /**
     * Attempts to write the given Pixmap exactly as a PNG-8 image to file; this attempt will only succeed if there
     * are no more than 256 colors in the Pixmap (treating all partially transparent colors as fully transparent).
     * If the attempt fails, this falls back to calling {@link #write(FileHandle, Pixmap, boolean, boolean)}, which
     * can dither the image to use no more than 255 colors (plus fully transparent) based on ditherFallback and will
     * always analyze the Pixmap to get an accurate-enough palette. All other write() methods in this class will
     * reduce the color depth somewhat, but as long as the color count stays at 256 or less, this will keep the
     * non-alpha components of colors exactly.
     * @param file a FileHandle that must be writable, and will have the given Pixmap written as a PNG-8 image
     * @param pixmap a Pixmap to write to the given output stream
     * @param ditherFallback if the Pixmap contains too many colors, this determines whether it will dither the output
     * @throws IOException if file writing fails for any reason
     */
    public void writePrecisely (FileHandle file, Pixmap pixmap, boolean ditherFallback) throws IOException {
        writePrecisely(file, pixmap, ditherFallback, 400);
    }
    /**
     * Attempts to write the given Pixmap exactly as a PNG-8 image to file; this attempt will only succeed if there
     * are no more than 256 colors in the Pixmap (treating all partially transparent colors as fully transparent).
     * If the attempt fails, this falls back to calling {@link #write(FileHandle, Pixmap, boolean, boolean)}, which
     * can dither the image to use no more than 255 colors (plus fully transparent) based on ditherFallback and will
     * always analyze the Pixmap to get an accurate-enough palette, using the given threshold for analysis (which is
     * typically between 1 and 1000, and most often near 200-400). All other write() methods in this class will
     * reduce the color depth somewhat, but as long as the color count stays at 256 or less, this will keep the
     * non-alpha components of colors exactly.
     * @param file a FileHandle that must be writable, and will have the given Pixmap written as a PNG-8 image
     * @param pixmap a Pixmap to write to the given output stream
     * @param ditherFallback if the Pixmap contains too many colors, this determines whether it will dither the output
     * @param threshold the analysis threshold to use if there are too many colors (min 0, practical max is over 100000)
     * @throws IOException if file writing fails for any reason
     */
    public void writePrecisely (FileHandle file, Pixmap pixmap, boolean ditherFallback, int threshold) throws IOException {
        OutputStream output = file.write(false);
        try {
            writePrecisely(output, pixmap, ditherFallback, threshold);
        } finally {
            StreamUtils.closeQuietly(output);
        }
    }

    /**
     * Attempts to write the given Pixmap exactly as a PNG-8 image to output; this attempt will only succeed if there
     * are no more than 256 colors in the Pixmap (treating all partially transparent colors as fully transparent).
     * If the attempt fails, this falls back to calling {@link #write(OutputStream, Pixmap, boolean, boolean)}, which
     * can dither the image to use no more than 255 colors (plus fully transparent) based on ditherFallback and will
     * always analyze the Pixmap to get an accurate-enough palette. All other write() methods in this class will
     * reduce the color depth somewhat, but as long as the color count stays at 256 or less, this will keep the
     * non-alpha components of colors exactly.
     * @param output an OutputStream that will not be closed
     * @param pixmap a Pixmap to write to the given output stream
     * @param ditherFallback if the Pixmap contains too many colors, this determines whether it will dither the output
     * @throws IOException if OutputStream things fail for any reason
     */
    public void writePrecisely(OutputStream output, Pixmap pixmap, boolean ditherFallback) throws IOException {
        writePrecisely(output, pixmap, ditherFallback, 400);
    }

    /**
     * Attempts to write the given Pixmap exactly as a PNG-8 image to output; this attempt will only succeed if there
     * are no more than 256 colors in the Pixmap (treating all partially transparent colors as fully transparent).
     * If the attempt fails, this falls back to calling {@link #write(OutputStream, Pixmap, boolean, boolean)}, which
     * can dither the image to use no more than 255 colors (plus fully transparent) based on ditherFallback and will
     * always analyze the Pixmap to get an accurate-enough palette, using the given threshold for analysis (which is
     * typically between 1 and 1000, and most often near 200-400). All other write() methods in this class will
     * reduce the color depth somewhat, but as long as the color count stays at 256 or less, this will keep the
     * non-alpha components of colors exactly.
     * @param output an OutputStream that will not be closed
     * @param pixmap a Pixmap to write to the given output stream
     * @param ditherFallback if the Pixmap contains too many colors, this determines whether it will dither the output
     * @param threshold the analysis threshold to use if there are too many colors (min 0, practical max is over 100000)
     * @throws IOException if OutputStream things fail for any reason
     */
    public void writePrecisely(OutputStream output, Pixmap pixmap, boolean ditherFallback, int threshold) throws IOException {
        IntIntMap colorToIndex = new IntIntMap(256);
        colorToIndex.put(0, 0);
        int color;
        int hasTransparent = 0;
        final int w = pixmap.getWidth(), h = pixmap.getHeight();
        for (int y = 0; y < h; y++) {
            int py = flipY ? (h - y - 1) : y;
            for (int px = 0; px < w; px++) {
                color = pixmap.getPixel(px, py);
                if((color & 0xFE) != 0xFE) {
                    if(hasTransparent == 0 && colorToIndex.size >= 256)
                    {
                        write(output, pixmap, true, ditherFallback, threshold);
                        return;
                    }
                    hasTransparent = 1;
                }
                else if(!colorToIndex.containsKey(color))
                {
                    colorToIndex.put(color, colorToIndex.size & 255);
                    if(colorToIndex.size == 257 && hasTransparent == 0)
                    {
                        colorToIndex.remove(0, 0);
                    }
                    if(colorToIndex.size > 256)
                    {
                        write(output, pixmap, true, ditherFallback, threshold);
                        return;
                    }
                }
            }
        }
        int[] paletteArray = new int[colorToIndex.size];
        for(IntIntMap.Entry ent : colorToIndex)
        {
            paletteArray[ent.value] = ent.key;
        }
        DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);
        DataOutputStream dataOutput = new DataOutputStream(output);
        dataOutput.write(SIGNATURE);

        buffer.writeInt(IHDR);
        buffer.writeInt(pixmap.getWidth());
        buffer.writeInt(pixmap.getHeight());
        buffer.writeByte(8); // 8 bits per component.
        buffer.writeByte(COLOR_INDEXED);
        buffer.writeByte(COMPRESSION_DEFLATE);
        buffer.writeByte(FILTER_NONE);
        buffer.writeByte(INTERLACE_NONE);
        buffer.endChunk(dataOutput);

        buffer.writeInt(PLTE);
        for (int i = 0; i < paletteArray.length; i++) {
            int p = paletteArray[i];
            buffer.write(p>>>24);
            buffer.write(p>>>16);
            buffer.write(p>>>8);
        }
        buffer.endChunk(dataOutput);

        if(hasTransparent == 1) {
            buffer.writeInt(TRNS);
            buffer.write(0);
            buffer.endChunk(dataOutput);
        }
        buffer.writeInt(IDAT);
        deflater.reset();

        int lineLen = pixmap.getWidth();
        byte[] lineOut, curLine, prevLine;
        if (lineOutBytes == null) {
            lineOut = (lineOutBytes = new ByteArray(lineLen)).items;
            curLine = (curLineBytes = new ByteArray(lineLen)).items;
            prevLine = (prevLineBytes = new ByteArray(lineLen)).items;
        } else {
            lineOut = lineOutBytes.ensureCapacity(lineLen);
            curLine = curLineBytes.ensureCapacity(lineLen);
            prevLine = prevLineBytes.ensureCapacity(lineLen);
            for (int i = 0, n = lastLineLen; i < n; i++)
            {
                prevLine[i] = 0;
            }
        }

        lastLineLen = lineLen;

        for (int y = 0; y < h; y++) {
            int py = flipY ? (h - y - 1) : y;
            for (int px = 0; px < w; px++) {
                color = pixmap.getPixel(px, py);
                curLine[px] = (byte) colorToIndex.get(color, 0);
            }

            lineOut[0] = (byte)(curLine[0] - prevLine[0]);

            //Paeth
            for (int x = 1; x < lineLen; x++) {
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
                lineOut[x] = (byte)(curLine[x] - c);
            }

            deflaterOutput.write(PAETH);
            deflaterOutput.write(lineOut, 0, lineLen);

            byte[] temp = curLine;
            curLine = prevLine;
            prevLine = temp;
        }
        deflaterOutput.finish();
        buffer.endChunk(dataOutput);

        buffer.writeInt(IEND);
        buffer.endChunk(dataOutput);

        output.flush();

    }

    private void writeSolid (OutputStream output, Pixmap pixmap) throws IOException{
        final int[] paletteArray = palette.paletteArray;
        final byte[] paletteMapping = palette.paletteMapping;

        DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);
        DataOutputStream dataOutput = new DataOutputStream(output);
        dataOutput.write(SIGNATURE);

        buffer.writeInt(IHDR);
        buffer.writeInt(pixmap.getWidth());
        buffer.writeInt(pixmap.getHeight());
        buffer.writeByte(8); // 8 bits per component.
        buffer.writeByte(COLOR_INDEXED);
        buffer.writeByte(COMPRESSION_DEFLATE);
        buffer.writeByte(FILTER_NONE);
        buffer.writeByte(INTERLACE_NONE);
        buffer.endChunk(dataOutput);

        buffer.writeInt(PLTE);
        for (int i = 0; i < paletteArray.length; i++) {
            int p = paletteArray[i];
            buffer.write(p>>>24);
            buffer.write(p>>>16);
            buffer.write(p>>>8);
        }
        buffer.endChunk(dataOutput);

        boolean hasTransparent = false;
        if(paletteArray[0] == 0) {
            hasTransparent = true;
            buffer.writeInt(TRNS);
            buffer.write(0);
            buffer.endChunk(dataOutput);
        }
        buffer.writeInt(IDAT);
        deflater.reset();

        int lineLen = pixmap.getWidth();
        byte[] lineOut, curLine, prevLine;
        if (lineOutBytes == null) {
            lineOut = (lineOutBytes = new ByteArray(lineLen)).items;
            curLine = (curLineBytes = new ByteArray(lineLen)).items;
            prevLine = (prevLineBytes = new ByteArray(lineLen)).items;
        } else {
            lineOut = lineOutBytes.ensureCapacity(lineLen);
            curLine = curLineBytes.ensureCapacity(lineLen);
            prevLine = prevLineBytes.ensureCapacity(lineLen);
            for (int i = 0, n = lastLineLen; i < n; i++)
            {
                prevLine[i] = 0;
            }
        }

        lastLineLen = lineLen;

        int color;
        final int w = pixmap.getWidth(), h = pixmap.getHeight();
        for (int y = 0; y < h; y++) {
            int py = flipY ? (h - y - 1) : y;
            for (int px = 0; px < w; px++) {
                color = pixmap.getPixel(px, py);
                if ((color & 0x80) == 0 && hasTransparent)
                    curLine[px] = 0;
                else {
                    int rr = ((color >>> 24)       );
                    int gg = ((color >>> 16) & 0xFF);
                    int bb = ((color >>> 8)  & 0xFF);
                    curLine[px] = paletteMapping[((rr << 7) & 0x7C00)
                            | ((gg << 2) & 0x3E0)
                            | ((bb >>> 3))];
                }
            }

            lineOut[0] = (byte)(curLine[0] - prevLine[0]);

            //Paeth
            for (int x = 1; x < lineLen; x++) {
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
                lineOut[x] = (byte)(curLine[x] - c);
            }

            deflaterOutput.write(PAETH);
            deflaterOutput.write(lineOut, 0, lineLen);

            byte[] temp = curLine;
            curLine = prevLine;
            prevLine = temp;
        }
        deflaterOutput.finish();
        buffer.endChunk(dataOutput);

        buffer.writeInt(IEND);
        buffer.endChunk(dataOutput);

        output.flush();
    }

    private void writeDithered (OutputStream output, Pixmap pixmap) throws IOException{
        DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);
        final int[] paletteArray = palette.paletteArray;
        final byte[] paletteMapping = palette.paletteMapping;


        DataOutputStream dataOutput = new DataOutputStream(output);
        dataOutput.write(SIGNATURE);

        buffer.writeInt(IHDR);
        buffer.writeInt(pixmap.getWidth());
        buffer.writeInt(pixmap.getHeight());
        buffer.writeByte(8); // 8 bits per component.
        buffer.writeByte(COLOR_INDEXED);
        buffer.writeByte(COMPRESSION_DEFLATE);
        buffer.writeByte(FILTER_NONE);
        buffer.writeByte(INTERLACE_NONE);
        buffer.endChunk(dataOutput);

        buffer.writeInt(PLTE);
        for (int i = 0; i < paletteArray.length; i++) {
            int p = paletteArray[i];
            buffer.write(p>>>24);
            buffer.write(p>>>16);
            buffer.write(p>>>8);
        }
        buffer.endChunk(dataOutput);

        boolean hasTransparent = false;
        if(paletteArray[0] == 0) {
            hasTransparent = true;
            buffer.writeInt(TRNS);
            buffer.write(0);
            buffer.endChunk(dataOutput);
        }
        buffer.writeInt(IDAT);
        deflater.reset();

        final int w = pixmap.getWidth(), h = pixmap.getHeight();
        byte[] lineOut, curLine, prevLine;
        byte[] curErrorRed, nextErrorRed, curErrorGreen, nextErrorGreen, curErrorBlue, nextErrorBlue;
        if (lineOutBytes == null) {
            lineOut = (lineOutBytes = new ByteArray(w)).items;
            curLine = (curLineBytes = new ByteArray(w)).items;
            prevLine = (prevLineBytes = new ByteArray(w)).items;
        } else {
            lineOut = lineOutBytes.ensureCapacity(w);
            curLine = curLineBytes.ensureCapacity(w);
            prevLine = prevLineBytes.ensureCapacity(w);
            for (int i = 0, n = lastLineLen; i < n; i++)
            {
                prevLine[i] = 0;
            }
        }
        if(palette.curErrorRedBytes == null)
        {
            curErrorRed = (palette.curErrorRedBytes = new ByteArray(w)).items;
            nextErrorRed = (palette.nextErrorRedBytes = new ByteArray(w)).items;
            curErrorGreen = (palette.curErrorGreenBytes = new ByteArray(w)).items;
            nextErrorGreen = (palette.nextErrorGreenBytes = new ByteArray(w)).items;
            curErrorBlue = (palette.curErrorBlueBytes = new ByteArray(w)).items;
            nextErrorBlue = (palette.nextErrorBlueBytes = new ByteArray(w)).items;
        } else {
            curErrorRed = palette.curErrorRedBytes.ensureCapacity(w);
            nextErrorRed = palette.nextErrorRedBytes.ensureCapacity(w);
            curErrorGreen = palette.curErrorGreenBytes.ensureCapacity(w);
            nextErrorGreen = palette.nextErrorGreenBytes.ensureCapacity(w);
            curErrorBlue = palette.curErrorBlueBytes.ensureCapacity(w);
            nextErrorBlue = palette.nextErrorBlueBytes.ensureCapacity(w);
            for (int i = 0; i < w; i++) {
                nextErrorRed[i] = 0;
                nextErrorGreen[i] = 0;
                nextErrorBlue[i] = 0;
            }

        }


        lastLineLen = w;

        int color, used, rdiff, gdiff, bdiff, state = 0xFEEDBEEF;
        byte er, eg, eb, paletteIndex;
        float xi1, xi2, w1 = palette.ditherStrength * 0.125f, w3 = w1 * 3f, w5 = w1 * 5f, w7 = w1 * 7f;
        for (int y = 0; y < h; y++) {
            int py = flipY ? (h - y - 1) : y;
            int ny = flipY ? (h - y - 2) : y + 1;
            for (int i = 0; i < w; i++) {
                curErrorRed[i] = nextErrorRed[i];
                curErrorGreen[i] = nextErrorGreen[i];
                curErrorBlue[i] = nextErrorBlue[i];
                nextErrorRed[i] = 0;
                nextErrorGreen[i] = 0;
                nextErrorBlue[i] = 0;
            }
            for (int px = 0; px < w; px++) {
                color = pixmap.getPixel(px, py) & 0xF8F8F880;
                if ((color & 0x80) == 0 && hasTransparent)
                    curLine[px] = 0;
                else {
                    er = curErrorRed[px];
                    eg = curErrorGreen[px];
                    eb = curErrorBlue[px];
                    color |= (color >>> 5 & 0x07070700) | 0xFE;
                    int rr = MathUtils.clamp(((color >>> 24)       ) + (er), 0, 0xFF);
                    int gg = MathUtils.clamp(((color >>> 16) & 0xFF) + (eg), 0, 0xFF);
                    int bb = MathUtils.clamp(((color >>> 8)  & 0xFF) + (eb), 0, 0xFF);
                    curLine[px] = paletteIndex =
                            paletteMapping[((rr << 7) & 0x7C00)
                                    | ((gg << 2) & 0x3E0)
                                    | ((bb >>> 3))];
                    used = paletteArray[paletteIndex & 0xFF];
                    rdiff = (color>>>24)-    (used>>>24);
                    gdiff = (color>>>16&255)-(used>>>16&255);
                    bdiff = (color>>>8&255)- (used>>>8&255);
                    state += (color + 0x41C64E6D) ^ color >>> 7;
                    state = (state << 21 | state >>> 11);
                    xi1 = randomXi(state);
                    state = (state << 15 | state >>> 17) ^ 0x9E3779B9;
                    xi2 = randomXi(state);
                    if(px < w - 1)
                    {
                        curErrorRed[px+1]   += rdiff * w7 * (1f + xi1);
                        curErrorGreen[px+1] += gdiff * w7 * (1f + xi1);
                        curErrorBlue[px+1]  += bdiff * w7 * (1f + xi1);
                    }
                    if(ny < h)
                    {
                        if(px > 0)
                        {
                            nextErrorRed[px-1]   += rdiff * w3 * (1f + xi2);
                            nextErrorGreen[px-1] += gdiff * w3 * (1f + xi2);
                            nextErrorBlue[px-1]  += bdiff * w3 * (1f + xi2);
                        }
                        if(px < w - 1)
                        {
                            nextErrorRed[px+1]   += rdiff * w1 * (1f - xi2);
                            nextErrorGreen[px+1] += gdiff * w1 * (1f - xi2);
                            nextErrorBlue[px+1]  += bdiff * w1 * (1f - xi2);
                        }
                        nextErrorRed[px]   += rdiff * w5 * (1f - xi1);
                        nextErrorGreen[px] += gdiff * w5 * (1f - xi1);
                        nextErrorBlue[px]  += bdiff * w5 * (1f - xi1);
                    }
                }
            }

            lineOut[0] = (byte)(curLine[0] - prevLine[0]);

            //Paeth
            for (int x = 1; x < w; x++) {
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
                lineOut[x] = (byte)(curLine[x] - c);
            }

            deflaterOutput.write(PAETH);
            deflaterOutput.write(lineOut, 0, w);

            byte[] temp = curLine;
            curLine = prevLine;
            prevLine = temp;
        }
        deflaterOutput.finish();
        buffer.endChunk(dataOutput);

        buffer.writeInt(IEND);
        buffer.endChunk(dataOutput);

        output.flush();
    }

    /** Disposal will happen automatically in {@link #finalize()} but can be done explicitly if desired. */
    public void dispose () {
        deflater.end();
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
