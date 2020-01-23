package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.ZopfliCompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.PngFileTypeModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

public class ZopfliCompressingProcessor implements PackProcessor {
    private static final String LOG_LEVEL = "INFO";

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        if (project.getFileType().getClass() != PngFileTypeModel.class) return;

        PngFileTypeModel fileType = (PngFileTypeModel) project.getFileType();

        if (fileType.getCompression() == null || fileType.getCompression().getType() != PngCompressionType.ZOPFLI) return;

        System.out.println("Zopfli compression started");

        ZopfliCompressionModel compModel = fileType.getCompression();
        PngOptimizer pngOptimizer = new PngOptimizer(LOG_LEVEL);
        pngOptimizer.setCompressor("zopfli", compModel.getIterations());

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
                float localCompressionRate = (optimizerResult.getOptimizedFileSize() - optimizerResult.getOriginalFileSize()) / (float) optimizerResult.getOriginalFileSize();
                compressionRate += localCompressionRate / pngOptimizer.getResults().size();
            }
            node.addMetadata(PackProcessingNode.META_COMPRESSION_RATE, compressionRate);
        }

        System.out.println("Zopfli compression finished");
    }
}
