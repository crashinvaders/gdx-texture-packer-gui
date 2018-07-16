package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.SortedIntList;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Data that can be used to limit the colors present in a Pixmap or other image, here with the goal of using 256 or less
 * colors in the image (for saving indexed-mode images).
 * <br>
 * Created by Tommy Ettinger on 6/23/2018.
 */
public class PaletteReducer {
    public final byte[] paletteMapping = new byte[0x8000];
    public final int[] paletteArray = new int[256];
    ByteArray curErrorRedBytes, nextErrorRedBytes, curErrorGreenBytes, nextErrorGreenBytes, curErrorBlueBytes, nextErrorBlueBytes;

    /**
     * DawnBringer's 256-color Aurora palette, modified slightly to fit one transparent color by removing one gray.
     * Aurora is available in <a href="http://pixeljoint.com/forum/forum_posts.asp?TID=26080&KW=">this set of tools</a>
     * for a pixel art editor, but it is usable for lots of high-color purposes. This is the default here, but that
     * default value is not used (yet?).
     */
    public static final int[] auroraPalette = {
            0x00000000, 0x010101FF, 0x131313FF, 0x252525FF, 0x373737FF, 0x494949FF, 0x5B5B5BFF, 0x6E6E6EFF,
            0x808080FF, 0x929292FF, 0xA4A4A4FF, 0xB6B6B6FF, 0xC9C9C9FF, 0xDBDBDBFF, 0xEDEDEDFF, 0xFFFFFFFF,
            0x007F7FFF, 0x3FBFBFFF, 0x00FFFFFF, 0xBFFFFFFF, 0x8181FFFF, 0x0000FFFF, 0x3F3FBFFF, 0x00007FFF,
            0x0F0F50FF, 0x7F007FFF, 0xBF3FBFFF, 0xF500F5FF, 0xFD81FFFF, 0xFFC0CBFF, 0xFF8181FF, 0xFF0000FF,
            0xBF3F3FFF, 0x7F0000FF, 0x551414FF, 0x7F3F00FF, 0xBF7F3FFF, 0xFF7F00FF, 0xFFBF81FF, 0xFFFFBFFF,
            0xFFFF00FF, 0xBFBF3FFF, 0x7F7F00FF, 0x007F00FF, 0x3FBF3FFF, 0x00FF00FF, 0xAFFFAFFF, 0x00BFFFFF,
            0x007FFFFF, 0x4B7DC8FF, 0xBCAFC0FF, 0xCBAA89FF, 0xA6A090FF, 0x7E9494FF, 0x6E8287FF, 0x7E6E60FF,
            0xA0695FFF, 0xC07872FF, 0xD08A74FF, 0xE19B7DFF, 0xEBAA8CFF, 0xF5B99BFF, 0xF6C8AFFF, 0xF5E1D2FF,
            0x7F00FFFF, 0x573B3BFF, 0x73413CFF, 0x8E5555FF, 0xAB7373FF, 0xC78F8FFF, 0xE3ABABFF, 0xF8D2DAFF,
            0xE3C7ABFF, 0xC49E73FF, 0x8F7357FF, 0x73573BFF, 0x3B2D1FFF, 0x414123FF, 0x73733BFF, 0x8F8F57FF,
            0xA2A255FF, 0xB5B572FF, 0xC7C78FFF, 0xDADAABFF, 0xEDEDC7FF, 0xC7E3ABFF, 0xABC78FFF, 0x8EBE55FF,
            0x738F57FF, 0x587D3EFF, 0x465032FF, 0x191E0FFF, 0x235037FF, 0x3B573BFF, 0x506450FF, 0x3B7349FF,
            0x578F57FF, 0x73AB73FF, 0x64C082FF, 0x8FC78FFF, 0xA2D8A2FF, 0xE1F8FAFF, 0xB4EECAFF, 0xABE3C5FF,
            0x87B48EFF, 0x507D5FFF, 0x0F6946FF, 0x1E2D23FF, 0x234146FF, 0x3B7373FF, 0x64ABABFF, 0x8FC7C7FF,
            0xABE3E3FF, 0xC7F1F1FF, 0xBED2F0FF, 0xABC7E3FF, 0xA8B9DCFF, 0x8FABC7FF, 0x578FC7FF, 0x57738FFF,
            0x3B5773FF, 0x0F192DFF, 0x1F1F3BFF, 0x3B3B57FF, 0x494973FF, 0x57578FFF, 0x736EAAFF, 0x7676CAFF,
            0x8F8FC7FF, 0xABABE3FF, 0xD0DAF8FF, 0xE3E3FFFF, 0xAB8FC7FF, 0x8F57C7FF, 0x73578FFF, 0x573B73FF,
            0x3C233CFF, 0x463246FF, 0x724072FF, 0x8F578FFF, 0xAB57ABFF, 0xAB73ABFF, 0xEBACE1FF, 0xFFDCF5FF,
            0xE3C7E3FF, 0xE1B9D2FF, 0xD7A0BEFF, 0xC78FB9FF, 0xC87DA0FF, 0xC35A91FF, 0x4B2837FF, 0x321623FF,
            0x280A1EFF, 0x401811FF, 0x621800FF, 0xA5140AFF, 0xDA2010FF, 0xD5524AFF, 0xFF3C0AFF, 0xF55A32FF,
            0xFF6262FF, 0xF6BD31FF, 0xFFA53CFF, 0xD79B0FFF, 0xDA6E0AFF, 0xB45A00FF, 0xA04B05FF, 0x5F3214FF,
            0x53500AFF, 0x626200FF, 0x8C805AFF, 0xAC9400FF, 0xB1B10AFF, 0xE6D55AFF, 0xFFD510FF, 0xFFEA4AFF,
            0xC8FF41FF, 0x9BF046FF, 0x96DC19FF, 0x73C805FF, 0x6AA805FF, 0x3C6E14FF, 0x283405FF, 0x204608FF,
            0x0C5C0CFF, 0x149605FF, 0x0AD70AFF, 0x14E60AFF, 0x7DFF73FF, 0x4BF05AFF, 0x00C514FF, 0x05B450FF,
            0x1C8C4EFF, 0x123832FF, 0x129880FF, 0x06C491FF, 0x00DE6AFF, 0x2DEBA8FF, 0x3CFEA5FF, 0x6AFFCDFF,
            0x91EBFFFF, 0x55E6FFFF, 0x7DD7F0FF, 0x08DED5FF, 0x109CDEFF, 0x055A5CFF, 0x162C52FF, 0x0F377DFF,
            0x004A9CFF, 0x326496FF, 0x0052F6FF, 0x186ABDFF, 0x2378DCFF, 0x699DC3FF, 0x4AA4FFFF, 0x90B0FFFF,
            0x5AC5FFFF, 0xBEB9FAFF, 0x786EF0FF, 0x4A5AFFFF, 0x6241F6FF, 0x3C3CF5FF, 0x101CDAFF, 0x0010BDFF,
            0x231094FF, 0x0C2148FF, 0x5010B0FF, 0x6010D0FF, 0x8732D2FF, 0x9C41FFFF, 0xBD62FFFF, 0xB991FFFF,
            0xD7A5FFFF, 0xD7C3FAFF, 0xF8C6FCFF, 0xE673FFFF, 0xFF52FFFF, 0xDA20E0FF, 0xBD29FFFF, 0xBD10C5FF,
            0x8C14BEFF, 0x5A187BFF, 0x641464FF, 0x410062FF, 0x320A46FF, 0x551937FF, 0xA01982FF, 0xC80078FF,
            0xFF50BFFF, 0xFF6AC5FF, 0xFAA0B9FF, 0xFC3A8CFF, 0xE61E78FF, 0xBD1039FF, 0x98344DFF, 0x911437FF,
    };

    /**
     * Constructs a default PaletteReducer that uses the DawnBringer Aurora palette.
     */
    public PaletteReducer()
    {
        exact(auroraPalette);
    }

    /**
     * Constructs a PaletteReducer that uses the given array of RGBA8888 ints as a palette (see {@link #exact(int[])}
     * for more info).
     * @param rgbaPalette an array of RGBA8888 ints to use as a palette
     */
    public PaletteReducer(int[] rgbaPalette)
    {
        exact(rgbaPalette);
    }
    /**
     * Constructs a PaletteReducer that uses the given array of Color objects as a palette (see {@link #exact(Color[])}
     * for more info).
     * @param colorPalette an array of Color objects to use as a palette
     */
    public PaletteReducer(Color[] colorPalette)
    {
        exact(colorPalette);
    }
    /**
     * Constructs a PaletteReducer that uses the given Array of Color objects as a palette (see {@link #exact(Color[])}
     * for more info).
     * @param colorPalette an array of Color objects to use as a palette
     */
    public PaletteReducer(Array<Color> colorPalette)
    {
        if(colorPalette != null) 
            exact(colorPalette.items, colorPalette.size);
        else 
            exact(auroraPalette);
    }
    /**
     * Constructs a PaletteReducer that analyzes the given Pixmap for color count and frequency to generate a palette
     * (see {@link #analyze(Pixmap)} for more info).
     * @param pixmap a Pixmap to analyze in detail to produce a palette
     */
    public PaletteReducer(Pixmap pixmap)
    {
        analyze(pixmap);
    }
    /**
     * Constructs a PaletteReducer that analyzes the given Pixmap for color count and frequency to generate a palette
     * (see {@link #analyze(Pixmap, int)} for more info).
     * @param pixmap a Pixmap to analyze in detail to produce a palette
     * @param threshold the minimum difference between colors required to put them in the palette (default 400)
     */
    public PaletteReducer(Pixmap pixmap, int threshold)
    {
        analyze(pixmap, threshold);
    }

    /**
     * Color difference metric; returns large numbers even for smallish differences.
     * If this returns 250 or more, the colors may be perceptibly different; 500 or more almost guarantees it.
     * @param color1 an RGBA8888 color as an int
     * @param color2 an RGBA8888 color as an int
     * @return the difference between the given colors, as a positive int
     */
    public static int difference(final int color1, final int color2)
    {
        int rmean = ((color1 >>> 24) + (color2 >>> 24)) >> 1;
        int r = (color1 >>> 24) - (color2 >>> 24);
        int g = (color1 >>> 16 & 0xFF) - (color2 >>> 16 & 0xFF);
        int b = (color1 >>> 8 & 0xFF) - (color2 >>> 8 & 0xFF);
        return (((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8);
    }
    /**
     * Color difference metric; returns large numbers even for smallish differences.
     * If this returns 250 or more, the colors may be perceptibly different; 500 or more almost guarantees it.
     * @param color1 an RGBA8888 color as an int
     * @param r2 red value from 0 to 255, inclusive
     * @param g2 green value from 0 to 255, inclusive
     * @param b2 blue value from 0 to 255, inclusive
     * @return the difference between the given colors, as a positive int
     */
    public static int difference(final int color1, int r2, int g2, int b2)
    {
        r2 = (r2 << 3 | r2 >>> 2);
        g2 = (g2 << 3 | g2 >>> 2);
        b2 = (b2 << 3 | b2 >>> 2);
        final int rmean = ((color1 >>> 24) + r2) >> 1,
                r = (color1 >>> 24) - r2,
                g = (color1 >>> 16 & 0xFF) - g2 << 1,
                b = (color1 >>> 8 & 0xFF) - b2;
        return (((512+rmean)*r*r)>>8) + g*g + (((767-rmean)*b*b)>>8);
    }

    /**
     * Color difference metric; returns large numbers even for smallish differences.
     * If this returns 250 or more, the colors may be perceptibly different; 500 or more almost guarantees it.
     * @param r1 red value from 0 to 255, inclusive
     * @param r2 red value from 0 to 255, inclusive
     * @param g1 green value from 0 to 255, inclusive
     * @param g2 green value from 0 to 255, inclusive
     * @param b1 blue value from 0 to 255, inclusive
     * @param b2 blue value from 0 to 255, inclusive
     * @return the difference between the given colors, as a positive int
     */
    public static int difference(final int r1, final int r2, final int g1, final int g2, final int b1, final int b2)
    {
        final int rmean = (r1 + r2) >> 1,
                r = r1 - r2,
                g = g1 - g2 << 1,
                b = b1 - b2;
        return (((512+rmean)*r*r)>>8) + g*g + (((767-rmean)*b*b)>>8);
    }
    /**
     * Builds the palette information this PNG8 stores from the RGBA8888 ints in {@code rgbaPalette}, up to 256 colors.
     * Alpha is not preserved except for the first item in rgbaPalette, and only if it is {@code 0} (fully transparent
     * black); otherwise all items are treated as opaque. If rgbaPalette is null, empty, or only has one color, then
     * this defaults to DawnBringer's Aurora palette with 256 hand-chosen colors (including transparent).
     * @param rgbaPalette an array of RGBA8888 ints; all will be used up to 256 items or the length of the array
     */
    public void exact(int[] rgbaPalette)
    {
        if(rgbaPalette == null || rgbaPalette.length < 2)
        {
            rgbaPalette = auroraPalette;
        }
        Arrays.fill(paletteArray, 0);
        Arrays.fill(paletteMapping, (byte) 0);
        final int plen = Math.min(256, rgbaPalette.length);
        int color, c2;
        int dist;
        for (int i = 0; i < plen; i++) {
            color = rgbaPalette[i];
            paletteArray[i] = color;
            paletteMapping[(color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F)] = (byte) i;
        }
        for (int r = 0; r < 32; r++) {
            for (int g = 0; g < 32; g++) {
                for (int b = 0; b < 32; b++) {
                    c2 = r << 10 | g << 5 | b;
                    if(paletteMapping[c2] == 0)
                    {
                        dist = 0x7FFFFFFF;
                        for (int i = 1; i < 256; i++) {
                            if(dist > (dist = Math.min(dist, difference(paletteArray[i], r, g, b))))
                                paletteMapping[c2] = (byte)i;
                        }
                    }
                }
            }
        }
    }

    /**
     * Builds the palette information this PNG8 stores from the Color objects in {@code colorPalette}, up to 256 colors.
     * Alpha is not preserved except for the first item in colorPalette, and only if its r, g, b, and a values are all
     * 0f (fully transparent black); otherwise all items are treated as opaque. If rgbaPalette is null, empty, or only
     * has one color, then this defaults to DawnBringer's Aurora palette with 256 hand-chosen colors (including
     * transparent).
     * @param colorPalette an array of Color objects; all will be used up to 256 items or the length of the array
     */
    public void exact(Color[] colorPalette)
    {
        exact(colorPalette, 256);
    }
    /**
     * Builds the palette information this PNG8 stores from the Color objects in {@code colorPalette}, up to 256 colors.
     * Alpha is not preserved except for the first item in colorPalette, and only if its r, g, b, and a values are all
     * 0f (fully transparent black); otherwise all items are treated as opaque. If rgbaPalette is null, empty, only has
     * one color, or limit is less than 2, then this defaults to DawnBringer's Aurora palette with 256 hand-chosen
     * colors (including transparent).
     * @param colorPalette an array of Color objects; all will be used up to 256 items, limit, or the length of the array
     * @param limit a limit on how many Color items to use from colorPalette; useful if colorPalette is from an Array
     */
    public void exact(Color[] colorPalette, int limit)
    {
        if(colorPalette == null || colorPalette.length < 2 || limit < 2)
        {
            exact(auroraPalette);
            return;
        }
        Arrays.fill(paletteArray, 0);
        Arrays.fill(paletteMapping, (byte) 0);
        final int plen = Math.min(Math.min(256, colorPalette.length), limit);
        int color, c2;
        int dist;
        for (int i = 0; i < plen; i++) {
            color = Color.rgba8888(colorPalette[i]);
            paletteArray[i] = color;
            paletteMapping[(color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F)] = (byte) i;
        }
        for (int r = 0; r < 32; r++) {
            for (int g = 0; g < 32; g++) {
                for (int b = 0; b < 32; b++) {
                    c2 = r << 10 | g << 5 | b;
                    if(paletteMapping[c2] == 0)
                    {
                        dist = 0x7FFFFFFF;
                        for (int i = 1; i < 256; i++) {
                            if(dist > (dist = Math.min(dist, difference(paletteArray[i], r, g, b))))
                                paletteMapping[c2] = (byte)i;
                        }
                    }
                }
            }
        }
    }

    /**
     * Analyzes {@code pixmap} for color count and frequency, building a palette with at most 256 colors if there are
     * too many colors to store in a PNG-8 palette. If there are 256 or less colors, this uses the exact colors
     * (although with at most one transparent color, and no alpha for other colors); if there are more than 256 colors
     * or any colors have 50% or less alpha, it will reserve a palette entry for transparent (even if the image has no
     * transparency). Because calling {@link #reduce(Pixmap)} (or any of PNG8's write methods) will dither colors that
     * aren't exact, and dithering works better when the palette can choose colors that are sufficiently different, this
     * uses a threshold value to determine whether it should permit a less-common color into the palette, and if the
     * second color is different enough (as measured by {@link #difference(int, int)}) by a value of at least 400, it is
     * allowed in the palette, otherwise it is kept out for being too similar to existing colors. This doesn't return a
     * value but instead stores the palette info in this object; a PaletteReducer can be assigned to the
     * {@link PNG8#palette} field or can be used directly to {@link #reduce(Pixmap)} a Pixmap.
     * @param pixmap a Pixmap to analyze, making a palette which can be used by this to {@link #reduce(Pixmap)} or by PNG8
     */
    public void analyze(Pixmap pixmap) {
        analyze(pixmap, 400);
    }
    /**
     * Analyzes {@code pixmap} for color count and frequency, building a palette with at most 256 colors if there are
     * too many colors to store in a PNG-8 palette. If there are 256 or less colors, this uses the exact colors
     * (although with at most one transparent color, and no alpha for other colors); if there are more than 256 colors
     * or any colors have 50% or less alpha, it will reserve a palette entry for transparent (even if the image has no
     * transparency). Because calling {@link #reduce(Pixmap)} (or any of PNG8's write methods) will dither colors that
     * aren't exact, and dithering works better when the palette can choose colors that are sufficiently different, this
     * takes a threshold value to determine whether it should permit a less-common color into the palette, and if the
     * second color is different enough (as measured by {@link #difference(int, int)}) by a value of at least
     * {@code threshold}, it is allowed in the palette, otherwise it is kept out for being too similar to existing
     * colors. The threshold is usually between 250 and 1000, and 400 is a good default. This doesn't return a value but
     * instead stores the palette info in this object; a PaletteReducer can be assigned to the {@link PNG8#palette}
     * field or can be used directly to {@link #reduce(Pixmap)} a Pixmap.
     * @param pixmap a Pixmap to analyze, making a palette which can be used by this to {@link #reduce(Pixmap)} or by PNG8
     * @param threshold a minimum color difference as produced by {@link #difference(int, int)}; usually between 250 and 1000, 400 is a good default
     */
    public void analyze(Pixmap pixmap, int threshold) {
        Arrays.fill(paletteArray, 0);
        Arrays.fill(paletteMapping, (byte) 0);
        int color;
        final ByteBuffer pixels = pixmap.getPixels();
        IntIntMap counts = new IntIntMap(256);
        int hasTransparent = 0;
        int[] reds = new int[256], greens = new int[256], blues = new int[256];
        switch (pixmap.getFormat()) {
            case RGBA8888: {
                while (pixels.remaining() >= 4) {
                    color = (pixels.getInt() & 0xF8F8F880);
                    if ((color & 0x80) != 0) {
                        color |= (color >>> 5 & 0x07070700) | 0xFE;
                        counts.getAndIncrement(color, 0, 1);
                    } else {
                        hasTransparent = 1;
                    }
                }
                if (counts.size + hasTransparent <= 256) {
                    int i = hasTransparent;
                    IntIntMap.Keys ks = counts.keys();
                    while (ks.hasNext()) {
                        color = ks.next();
                        paletteArray[i] = color;
                        color = (color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F);
                        paletteMapping[color] = (byte) i;
                        reds[i] = color >>> 10;
                        greens[i] = color >>> 5 & 31;
                        blues[i] = color & 31;
                        i++;
                    }
                } else // reduce color count
                {
                    IntIntMap.Entries es = counts.entries();
                    SortedIntList<IntIntMap.Entry> sil = new SortedIntList<>();
                    while (es.hasNext()) {
                        IntIntMap.Entry ent = es.next(), ent2 = new IntIntMap.Entry();
                        ent2.key = ent.key;
                        ent2.value = ent.value;
                        sil.insert(-ent.value, ent2);
                    }
                    Iterator<SortedIntList.Node<IntIntMap.Entry>> it = sil.iterator();
                    PER_BEST:
                    for (int i = 1; i < 256 && it.hasNext(); ) {
                        color = it.next().value.key;
                        for (int j = 1; j < i; j++) {
                            if (difference(color, paletteArray[j]) < threshold)
                                continue PER_BEST;
                        }
                        paletteArray[i] = color;
                        color = (color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F);
                        paletteMapping[color] = (byte) i;
                        reds[i] = color >>> 10;
                        greens[i] = color >>> 5 & 31;
                        blues[i] = color & 31;
                        i++;
                    }
                }
                int c2, dist;
                for (int r = 0; r < 32; r++) {
                    for (int g = 0; g < 32; g++) {
                        for (int b = 0; b < 32; b++) {
                            c2 = r << 10 | g << 5 | b;
                            if (paletteMapping[c2] == 0) {
                                dist = 0x7FFFFFFF;
                                for (int i = 1; i < 256; i++) {
                                    if (dist > (dist = Math.min(dist, difference(reds[i], r, greens[i], g, blues[i], b))))
                                        paletteMapping[c2] = (byte) i;
                                }
                            }
                        }
                    }
                }
            }
            break;
            case RGB888: {
                while (pixels.remaining() >= 6) {
                    color = (pixels.getInt() & 0xF8F8F800);
                    color |= (color >>> 5 & 0x07070700) | 0xFE;
                    pixels.position(pixels.position() - 1);
                    counts.getAndIncrement(color, 0, 1);
                }
                if (pixels.remaining() >= 3) {
                    color = ((pixels.get() & 0xF8) << 24 | (pixels.get() & 0xF8) << 16 | (pixels.get() & 0xF8) << 8);
                    color |= (color >>> 5 & 0x07070700) | 0xFE;
                    counts.getAndIncrement(color, 0, 1);
                }
                if (counts.size <= 256) {
                    int i = 0;
                    IntIntMap.Keys ks = counts.keys();
                    while (ks.hasNext()) {
                        color = ks.next();
                        paletteArray[i] = color;
                        color = (color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F);
                        paletteMapping[color] = (byte) i;
                        reds[i] = color >>> 10;
                        greens[i] = color >>> 5 & 31;
                        blues[i] = color & 31;
                        i++;
                    }
                } else {
                    IntIntMap.Entries es = counts.entries();
                    SortedIntList<IntIntMap.Entry> sil = new SortedIntList<>();
                    while (es.hasNext()) {
                        IntIntMap.Entry ent = es.next(), ent2 = new IntIntMap.Entry();
                        ent2.key = ent.key;
                        ent2.value = ent.value;
                        sil.insert(-ent.value, ent2);
                    }
                    Iterator<SortedIntList.Node<IntIntMap.Entry>> it = sil.iterator();
                    PER_BEST:
                    for (int i = 1; i < 256 && it.hasNext(); ) {
                        color = it.next().value.key;
                        for (int j = 1; j < i; j++) {
                            if (difference(color, paletteArray[j]) < threshold)
                                continue PER_BEST;
                        }
                        paletteArray[i] = color;
                        color = (color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F);
                        paletteMapping[color] = (byte) i;
                        reds[i] = color >>> 10;
                        greens[i] = color >>> 5 & 31;
                        blues[i] = color & 31;
                        i++;
                    }
                }
                int c2, dist;
                for (int r = 0; r < 32; r++) {
                    for (int g = 0; g < 32; g++) {
                        for (int b = 0; b < 32; b++) {
                            c2 = r << 10 | g << 5 | b;
                            if (paletteMapping[c2] == 0) {
                                dist = 0x7FFFFFFF;
                                for (int i = 1; i < 256; i++) {
                                    if (dist > (dist = Math.min(dist, difference(reds[i], r, greens[i], g, blues[i], b))))
                                        paletteMapping[c2] = (byte) i;
                                }
                            }
                        }
                    }
                }
            }
        }
        pixels.rewind();
    }

    /**
     * Modifies the given Pixmap so it only uses colors present in this PaletteReducer, dithering when it can.
     * If you want to reduce the colors in a Pixmap based on what it currently contains, call
     * {@link #analyze(Pixmap)} with {@code pixmap} as its argument, then call this method with the same
     * Pixmap. You may instead want to use a known palette instead of one computed from a Pixmap;
     * {@link #exact(int[])} is the tool for that job.
     * <br>
     * This method is not incredibly fast because of the extra calculations it has to do for dithering, but if you can
     * compute the PaletteReducer once and reuse it, that will save some time.
     * @param pixmap a Pixmap that will be modified in place
     * @return the given Pixmap, for chaining
     */
    public Pixmap reduce (Pixmap pixmap) {
        boolean hasTransparent = (paletteArray[0] == 0);
        final int lineLen = pixmap.getWidth(), h = pixmap.getHeight();
        byte[] curErrorRed, nextErrorRed, curErrorGreen, nextErrorGreen, curErrorBlue, nextErrorBlue;
        if (curErrorRedBytes == null) {
            curErrorRed = (curErrorRedBytes = new ByteArray(lineLen)).items;
            nextErrorRed = (nextErrorRedBytes = new ByteArray(lineLen)).items;
            curErrorGreen = (curErrorGreenBytes = new ByteArray(lineLen)).items;
            nextErrorGreen = (nextErrorGreenBytes = new ByteArray(lineLen)).items;
            curErrorBlue = (curErrorBlueBytes = new ByteArray(lineLen)).items;
            nextErrorBlue = (nextErrorBlueBytes = new ByteArray(lineLen)).items;
        } else {
            curErrorRed = curErrorRedBytes.ensureCapacity(lineLen);
            nextErrorRed = nextErrorRedBytes.ensureCapacity(lineLen);
            curErrorGreen = curErrorGreenBytes.ensureCapacity(lineLen);
            nextErrorGreen = nextErrorGreenBytes.ensureCapacity(lineLen);
            curErrorBlue = curErrorBlueBytes.ensureCapacity(lineLen);
            nextErrorBlue = nextErrorBlueBytes.ensureCapacity(lineLen);
            for (int i = 0; i < lineLen; i++) {
                nextErrorRed[i] = 0;
                nextErrorGreen[i] = 0;
                nextErrorBlue[i] = 0;
            }

        }
        Pixmap.Blending blending = pixmap.getBlending();
        pixmap.setBlending(Pixmap.Blending.None);
        int color, used, rdiff, gdiff, bdiff;
        byte er, eg, eb, paletteIndex;
        for (int y = 0; y < h; y++) {
            int ny = y + 1;
            for (int i = 0; i < lineLen; i++) {
                curErrorRed[i] = nextErrorRed[i];
                curErrorGreen[i] = nextErrorGreen[i];
                curErrorBlue[i] = nextErrorBlue[i];
                nextErrorRed[i] = 0;
                nextErrorGreen[i] = 0;
                nextErrorBlue[i] = 0;
            }
            for (int px = 0; px < lineLen; px++) {
                color = pixmap.getPixel(px, y) & 0xF8F8F880;
                if ((color & 0x80) == 0 && hasTransparent)
                    pixmap.drawPixel(px, y, 0);
                else {
                    er = curErrorRed[px];
                    eg = curErrorGreen[px];
                    eb = curErrorBlue[px];
                    color |= (color >>> 5 & 0x07070700) | 0xFE;
                    int rr = MathUtils.clamp(((color >>> 24)       ) + (er), 0, 0xFF);
                    int gg = MathUtils.clamp(((color >>> 16) & 0xFF) + (eg), 0, 0xFF);
                    int bb = MathUtils.clamp(((color >>> 8)  & 0xFF) + (eb), 0, 0xFF);
                    paletteIndex =
                            paletteMapping[((rr << 7) & 0x7C00)
                                    | ((gg << 2) & 0x3E0)
                                    | ((bb >>> 3))];
                    used = paletteArray[paletteIndex & 0xFF];
                    pixmap.drawPixel(px, y, used);
                    rdiff = (color>>>24)-    (used>>>24);
                    gdiff = (color>>>16&255)-(used>>>16&255);
                    bdiff = (color>>>8&255)- (used>>>8&255);
                    if(px < lineLen - 1)
                    {
                        curErrorRed[px+1]   += rdiff >> 1;
                        curErrorGreen[px+1] += gdiff >> 1;
                        curErrorBlue[px+1]  += bdiff >> 1;
                    }
                    if(ny < h)
                    {
                        if(px > 0)
                        {
                            nextErrorRed[px-1]   += rdiff >> 2;
                            nextErrorGreen[px-1] += gdiff >> 2;
                            nextErrorBlue[px-1]  += bdiff >> 2;
                        }
                        nextErrorRed[px]   += rdiff >> 2;
                        nextErrorGreen[px] += gdiff >> 2;
                        nextErrorBlue[px]  += bdiff >> 2;
                    }
                }
            }
        }
        pixmap.setBlending(blending);
        return pixmap;
    }

    /**
     * Modifies the given Pixmap so it only uses colors present in this PaletteReducer, without dithering. This produces
     * blocky solid sections of color in most images where the palette isn't exact, instead of checkerboard-like
     * dithering patterns. If you want to reduce the colors in a Pixmap based on what it currently contains, call
     * {@link #analyze(Pixmap)} with {@code pixmap} as its argument, then call this method with the same
     * Pixmap. You may instead want to use a known palette instead of one computed from a Pixmap;
     * {@link #exact(int[])} is the tool for that job.
     * @param pixmap a Pixmap that will be modified in place
     * @return the given Pixmap, for chaining
     */
    public Pixmap reduceSolid (Pixmap pixmap) {
        boolean hasTransparent = (paletteArray[0] == 0);
        final int lineLen = pixmap.getWidth(), h = pixmap.getHeight();
        Pixmap.Blending blending = pixmap.getBlending();
        pixmap.setBlending(Pixmap.Blending.None);
        int color;
        for (int y = 0; y < h; y++) {
            for (int px = 0; px < lineLen; px++) {
                color = pixmap.getPixel(px, y);
                if ((color & 0x80) == 0 && hasTransparent)
                    pixmap.drawPixel(px, y, 0);
                else {
                    int rr = ((color >>> 24)       );
                    int gg = ((color >>> 16) & 0xFF);
                    int bb = ((color >>> 8)  & 0xFF);
                    pixmap.drawPixel(px, y, paletteArray[
                            paletteMapping[((rr << 7) & 0x7C00)
                            | ((gg << 2) & 0x3E0)
                            | ((bb >>> 3))] & 0xFF]);
                }
            }

        }
        pixmap.setBlending(blending);
        return pixmap;
    }

}
