package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

public class CompressingProcessor implements PackProcessor {
    private static final String LOG_LEVEL = "INFO";

    @Override
    public void processPackage(PackModel pack) throws Exception {
        if (!pack.getSettings().outputFormat.equals("png")) return;

        System.out.println("Compression started");

        PngOptimizer pngOptimizer = new PngOptimizer(LOG_LEVEL);

        TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()),
                Gdx.files.absolute(pack.getOutputDir()), false);

        for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
            PngImage image = new PngImage(page.textureFile.file().getAbsolutePath(), "LOG_LEVEL");
            pngOptimizer.optimize(
                    image,
                    page.textureFile.file().getAbsolutePath(),
                    false,
                    5);
        }

        System.out.println("Compression finished");
    }
}
