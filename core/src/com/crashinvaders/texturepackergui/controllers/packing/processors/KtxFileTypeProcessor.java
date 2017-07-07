package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.crashinvaders.texturepackergui.services.model.FileTypeType;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.filetype.KtxFileTypeModel;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.crashinvaders.texturepackergui.utils.KtxEtc1Processor;
import com.crashinvaders.texturepackergui.utils.KtxEtc2Processor;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

public class KtxFileTypeProcessor {

    private KtxFileTypeProcessor() { }

    private static boolean checkCondition(PackProcessingNode node) {
        ProjectModel project = node.getProject();

        if (project.getFileType().getType() != FileTypeType.KTX) return false;

        return true;
    }

    /** Preparation phase. We should configure packer to pack pages as plain PNGs here. */
    public static class Pre implements PackProcessor {
        @Override
        public void processPackage(PackProcessingNode node) throws Exception {
            if (!checkCondition(node)) return;

            PackModel pack = node.getPack();
            pack.getSettings().outputFormat = "png";
            pack.getSettings().format = Pixmap.Format.RGBA8888;
        }
    }

    /** Processing phase. Here we should take page PNG images and convert them into KTX images. */
    public static class Post implements PackProcessor {
        @Override
        public void processPackage(PackProcessingNode node) throws Exception {
            if (!checkCondition(node)) return;

            System.out.println("ETC compression started");

            PackModel pack = node.getPack();
            ProjectModel project = node.getProject();
            KtxFileTypeModel fileType = project.getFileType();

            // Compression section
            {
                TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                                Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()),
                                Gdx.files.absolute(pack.getOutputDir()), false);

                for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
	    			FileHandle input = page.textureFile;
	    			FileHandle output = Gdx.files.getFileHandle(input.path().substring(0, input.path().lastIndexOf('.'))
                            + (fileType.isZipping() ? ".zktx" : ".ktx"), input.type());

                    switch (fileType.getFormat()) {
                        case ETC1:
                            boolean alphaChanel = fileType.getEncodingEtc1() == KtxFileTypeModel.EncodingETC1.RGBA;
                            KtxEtc1Processor.process(input, output, alphaChanel);
                            break;
                        case ETC2:
                            KtxEtc2Processor.process(input, output, fileType.getEncodingEtc2().format);
                            break;
                    }
                    input.delete();
                    if (fileType.isZipping()) {
                        FileUtils.gzip(output);
                    }
	    		}

	    		// Replace page image names in atlas markup file with new ktx/zktx files
                {
                    String atlasText = Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()).readString();
                    for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
                        atlasText = atlasText.replace(page.textureFile.name(),
                                page.textureFile.nameWithoutExtension() + (fileType.isZipping() ? ".zktx" : ".ktx"));
                    }
                    Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()).writeString(atlasText, false);
                }
            }

            System.out.println("ETC compression finished");
        }
    }
}
