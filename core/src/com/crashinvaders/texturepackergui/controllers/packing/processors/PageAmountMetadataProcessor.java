package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

public class PageAmountMetadataProcessor implements PackProcessor {

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

        int pageAmount = atlasData.getPages().size;
        node.setMetadata(PackProcessingNode.META_ATLAS_PAGES, pageAmount);
        System.out.println(pageAmount + " pages");
    }
}
