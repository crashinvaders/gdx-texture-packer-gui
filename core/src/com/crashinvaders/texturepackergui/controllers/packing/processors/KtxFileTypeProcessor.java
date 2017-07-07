package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.crashinvaders.texturepackergui.services.model.FileTypeType;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.filetype.KtxFileTypeModel;
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
	    			String fileName = page.textureFile.file().getAbsolutePath();
	    			String outFileName = fileName.substring(0, fileName.lastIndexOf('.'))
	    					+ (fileType.isZipping() ? ".zktx" : ".ktx");

	    			//TODO perform conversion here
//	    			KTXProcessor.convert(fileName, outFileName, compModel.getEtc1Comp(), compModel.getEtc2Comp());
	    		}
            }

            System.out.println("ETC compression finished");
        }
    }
}
