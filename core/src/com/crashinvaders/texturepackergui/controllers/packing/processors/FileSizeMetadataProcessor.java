package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.crashinvaders.texturepackergui.services.TinifyService;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

public class FileSizeMetadataProcessor implements PackProcessor {

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        FileHandle packFileHandle = Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename());
        FileHandle imagesDirFileHandle = Gdx.files.absolute(pack.getOutputDir());
        TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                packFileHandle,
                imagesDirFileHandle,
                false);

        long totalSize = 0; // Bytes
        totalSize += packFileHandle.length();
        for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
            long pageSize = page.textureFile.length();
            totalSize += pageSize;
        }
        node.addMetadata(PackProcessingNode.META_FILE_SIZE, totalSize);

        System.out.println("Total pack files size is " + totalSize + " bytes");
    }
}
