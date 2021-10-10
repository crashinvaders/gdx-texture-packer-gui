package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogicgames.libimagequant.*;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.PngquantCompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.PngFileTypeModel;
import com.crashinvaders.texturepackergui.utils.IndexedPng;
import com.crashinvaders.texturepackergui.utils.SystemUtils;
import com.crashinvaders.texturepackergui.utils.SystemUtils.OperatingSystem;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

import java.io.OutputStream;
import java.util.Locale;

public class PngquantCompressionProcessor implements PackProcessor {

    private static final boolean SYSTEM_SUPPORTED =
            SystemUtils.check(OperatingSystem.Windows, SystemUtils.CpuArch.Amd64, SystemUtils.CpuArch.X86) ||
                    SystemUtils.check(OperatingSystem.Linux, SystemUtils.CpuArch.Amd64, SystemUtils.CpuArch.X86) ||
                    SystemUtils.check(OperatingSystem.MacOS, SystemUtils.CpuArch.Amd64);

    private static boolean nativeLibLoaded = false;

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        if (project.getFileType().getClass() != PngFileTypeModel.class) return;

        PngFileTypeModel fileType = project.getFileType();

        if (fileType.getCompression() == null || fileType.getCompression().getType() != PngCompressionType.PNGQUANT) return;

        if (!isPngquantSupported()) {
            throw new IllegalStateException("Pngquant natives are not supported on the current system: " + SystemUtils.getPrintString());
        }

        System.out.println("Pngquant compression started");

        PngquantCompressionModel compModel = fileType.getCompression();

        float compressionRateSum = 0f;
        {
            TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                            Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()),
                            Gdx.files.absolute(pack.getOutputDir()), false);
            for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
                Pixmap pixmap = null;
                try {
                    long preCompressedSize = page.textureFile.length();
                    pixmap = new Pixmap(page.textureFile);

                    QuantizedData quantizedData = quantizePixmap(
                            pixmap,
                            compModel.getSpeed(),
                            compModel.getMaxColors(),
                            compModel.getMinQuality(),
                            compModel.getMaxQuality(),
                            compModel.getDitheringLevel());
                    System.out.println("Page \"" + page.textureFile.name() + "\" quantized with " + quantizedData.palette.length + " colors.");

                    try (OutputStream os = page.textureFile.write(false)) {
                        IndexedPng.write(
                                os,
                                pixmap.getWidth(),
                                pixmap.getHeight(),
                                quantizedData.palette,
                                quantizedData.pixelIndices,
                                compModel.getDeflateLevel());
                    }

                    long postCompressedSize = page.textureFile.length();
                    float pageCompression = ((postCompressedSize-preCompressedSize) / (float)preCompressedSize);
                    compressionRateSum += pageCompression;

                    System.out.printf(Locale.US, "%s compressed for %+.2f%%%n", page.textureFile.name(), pageCompression*100f);
                } finally {
                    if (pixmap != null) {
                        pixmap.dispose();
                    }
                }
            }
            node.addMetadata(PackProcessingNode.META_COMPRESSION_RATE, compressionRateSum / atlasData.getPages().size);
        }

        System.out.println("PNG8 compression finished");
    }

    public static boolean isPngquantSupported() {
        return SYSTEM_SUPPORTED;
    }

    private static QuantizedData quantizePixmap(Pixmap pixmap, int speed, int maxColors, int minQuality, int maxQuality, float ditheringLevel) {
        if (!nativeLibLoaded) {
            new SharedLibraryLoader().load("imagequant-java");
            nativeLibLoaded = true;
        }

        int width = pixmap.getWidth();
        int height = pixmap.getHeight();
        int size = width * height;

        // Collect all the image pixels written as R0, G0, B0, A0, R1, G1, ... component bytes.
        byte[] rawPixels = new byte[size * 4];
        for (int i = 0; i < size; i++) {
            int x = i % width;
            int y = i / width;
            int rgba = pixmap.getPixel(x, y);
            rawPixels[i * 4 + 0] = (byte) (rgba >>> 24);
            rawPixels[i * 4 + 1] = (byte) (rgba >>> 16);
            rawPixels[i * 4 + 2] = (byte) (rgba >>> 8);
            rawPixels[i * 4 + 3] = (byte) (rgba >>> 0);
        }

        LiqAttribute liqAttribute = new LiqAttribute();
        liqAttribute.setSpeed(speed);
        liqAttribute.setMaxColors(maxColors);
        liqAttribute.setQuality(minQuality, maxQuality);
        LiqImage liqImage = new LiqImage(liqAttribute, rawPixels, width, height, 0);
        LiqResult liqResult = liqImage.quantize();
        liqResult.setDitheringLevel(ditheringLevel);

        byte[] quantizedPixels = new byte[size];
        liqImage.remap(liqResult, quantizedPixels);
        LiqPalette liqPalette = liqResult.getPalette();

        int[] palette = new int[liqPalette.getCount()];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = liqPalette.getColor(i);
        }

        liqResult.destroy();
        liqImage.destroy();
        liqAttribute.destroy();

        return new QuantizedData(palette, quantizedPixels);
    }

    private static class QuantizedData {

        public final int[] palette;
        public final byte[] pixelIndices;

        public QuantizedData(int[] palette, byte[] pixelIndices) {
            this.palette = palette;
            this.pixelIndices = pixelIndices;
        }
    }
}
