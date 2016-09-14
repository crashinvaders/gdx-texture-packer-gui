package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.compression.PngtasticCompressionModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

public class PngtasticCompressingProcessor implements PackProcessor {
    private static final String LOG_LEVEL = "INFO";

    @Override
    public void processPackage(ProjectModel projectModel, PackModel pack, ObjectMap metadata) throws Exception {
        if (!pack.getSettings().outputFormat.equals("png")) return;
        if (projectModel.getPngCompression() == null || projectModel.getPngCompression().getType() != PngCompressionType.PNGTASTIC) return;

        System.out.println("Pngtastic compression started");

        PngtasticCompressionModel compModel = (PngtasticCompressionModel)projectModel.getPngCompression();
        PngOptimizer pngOptimizer = new PngOptimizer(LOG_LEVEL);

        // Compression section
        {
            TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                            Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()),
                            Gdx.files.absolute(pack.getOutputDir()), false);

            for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
                PngImage image = new PngImage(page.textureFile.file().getAbsolutePath(), LOG_LEVEL);
                pngOptimizer.optimize(
                        image,
                        page.textureFile.file().getAbsolutePath(),
                        false,
                        compModel.getLevel());
            }
        }

        // Compute compression rate for metadata
        {
            float compressionRate = 0f;
            for (PngOptimizer.OptimizerResult optimizerResult : pngOptimizer.getResults()) {
                float localCompressionRate = (optimizerResult.getOptimizedFileSize() - optimizerResult.getOriginalFileSize()) / (float) optimizerResult.getOriginalFileSize() * 100f;
                compressionRate += localCompressionRate / pngOptimizer.getResults().size();
            }
            metadata.put(META_COMPRESSION_RATE, compressionRate);
        }

        System.out.println("Pngtastic compression finished");
    }
}
