package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.crashinvaders.texturepackergui.services.model.EtcCompressionType;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.compression.EtcCompressionModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

public class EtcCompressingProcessor implements PackProcessor {

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        if (project.getEtcCompression() == null || project.getEtcCompression().getType() != EtcCompressionType.KTX) return;

        System.out.println("ETC compression started");

        EtcCompressionModel compModel = (EtcCompressionModel)project.getEtcCompression();

        // Compression section
        {
            TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                            Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()),
                            Gdx.files.absolute(pack.getOutputDir()), false);

            for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
				String fileName = page.textureFile.file().getAbsolutePath();
				String outFileName = fileName.substring(0, fileName.lastIndexOf('.'))
						+ (compModel.isCompressed() ? ".ktx" : ".zktx");

				KTXProcessor.convert(fileName, outFileName, compModel.getEtc1Comp(), compModel.getEtc2Comp());
			}
        }

        System.out.println("ETC compression finished");
    }
}
