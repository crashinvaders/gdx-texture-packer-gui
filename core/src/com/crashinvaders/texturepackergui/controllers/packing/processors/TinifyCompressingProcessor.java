package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.texturepackergui.services.TinifyService;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

public class TinifyCompressingProcessor implements PackProcessor {

    private final TinifyService tinifyService;

    public TinifyCompressingProcessor(TinifyService tinifyService) {
        this.tinifyService = tinifyService;
    }

    @Override
    public void processPackage(ProjectModel projectModel, PackModel pack, ObjectMap metadata) throws Exception {
        //TODO it should support jpg, right?
        if (!pack.getSettings().outputFormat.equals("png")) return;
        if (projectModel.getPngCompression() == null || projectModel.getPngCompression().getType() != PngCompressionType.TINY_PNG) return;

        System.out.println("Tinify compression started");

        TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                        Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()),
                        Gdx.files.absolute(pack.getOutputDir()), false);

        float compressionRateSum = 0f;
        for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
            long preCompressedSize = page.textureFile.length();

            tinifyService.compressImageSync(page.textureFile);

            long postCompressedSize = page.textureFile.length();
            float pageCompression = ((postCompressedSize-preCompressedSize) / (float)preCompressedSize);
            compressionRateSum += pageCompression;

            System.out.println(String.format("%s compressed for %+5.2f%%", page.textureFile.name(), pageCompression*100f));
        }
        metadata.put(META_COMPRESSION_RATE, compressionRateSum / atlasData.getPages().size);

        System.out.println("Tinify compression finished");
    }
}
