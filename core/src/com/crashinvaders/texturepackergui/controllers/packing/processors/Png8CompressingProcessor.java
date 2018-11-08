package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.Png8CompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.PngFileTypeModel;
import com.crashinvaders.texturepackergui.utils.PNG8;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

import java.util.Locale;

public class Png8CompressingProcessor implements PackProcessor {

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        if (project.getFileType().getClass() != PngFileTypeModel.class) return;

        PngFileTypeModel fileType = (PngFileTypeModel) project.getFileType();

        if (fileType.getCompression() == null || fileType.getCompression().getType() != PngCompressionType.PNG8) return;

        System.out.println("PNG8 compression started");

        Png8CompressionModel compModel = fileType.getCompression();
        PNG8 png8 = new PNG8();

        float compressionRateSum = 0f;
        {
            TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                            Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()),
                            Gdx.files.absolute(pack.getOutputDir()), false);
            for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
                Pixmap pm = null;
                try {
                    long preCompressedSize = page.textureFile.length();
                    pm = new Pixmap(page.textureFile);
                    png8.setCompression(compModel.getLevel());
                    png8.writePrecisely(page.textureFile, pm, compModel.isDithering(), compModel.getThreshold());
                    long postCompressedSize = page.textureFile.length();
                    float pageCompression = ((postCompressedSize-preCompressedSize) / (float)preCompressedSize);
                    compressionRateSum += pageCompression;

                    System.out.println(String.format(Locale.US, "%s compressed for %+.2f%%", page.textureFile.name(), pageCompression*100f));
                } finally {
                    if (pm != null) {
                        pm.dispose();
                    }
                }
            }
            node.addMetadata(PackProcessingNode.META_COMPRESSION_RATE, compressionRateSum / atlasData.getPages().size);
        }
        png8.dispose();

        System.out.println("PNG8 compression finished");
    }
}
