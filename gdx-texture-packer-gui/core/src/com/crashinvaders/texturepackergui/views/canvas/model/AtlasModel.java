package com.crashinvaders.texturepackergui.views.canvas.model;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class AtlasModel implements Disposable {
    private static final String TAG = AtlasModel.class.getSimpleName();

    private final FileHandle atlasFile;
    private final Array<PageModel> pages = new Array<>(true, 8);
    private final String atlasPath;
    private final TextureAtlasData atlasData;

    public AtlasModel(FileHandle atlasFile) {
        this.atlasFile = atlasFile;
        this.atlasData = new TextureAtlasData(atlasFile, atlasFile.parent(), false);
        atlasPath = atlasFile.file().getAbsolutePath();

        generatePages(atlasData);
    }

    private void generatePages(TextureAtlasData atlasData) {
        this.pages.clear();
        Array<TextureAtlasData.Page> pageDataArray = atlasData.getPages();
        for (int i = 0; i < pageDataArray.size; i++) {
            TextureAtlasData.Page pageData = pageDataArray.get(i);
            PageModel page = new PageModel(this, pageData, i);
            this.pages.add(page);
        }
    }

    @Override
    public void dispose() {
        for (int i = 0; i < pages.size; i++) {
            pages.get(i).dispose();
        }
        pages.clear();
    }

    public FileHandle getAtlasFile() {
        return atlasFile;
    }

    public String getAtlasPath() {
        return atlasPath;
    }

    public TextureAtlasData getAtlasData() {
        return atlasData;
    }

    public Array<PageModel> getPages() {
        return pages;
    }

}
