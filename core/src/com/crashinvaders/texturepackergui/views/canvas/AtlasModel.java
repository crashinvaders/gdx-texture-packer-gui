package com.crashinvaders.texturepackergui.views.canvas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;

/** This is simple TextureAtlas wrapper that holds some extra info for utility use. */
public class AtlasModel implements Disposable {
    private static final String TAG = AtlasModel.class.getSimpleName();

    private final FileHandle atlasFile;
//    private final TextureAtlas atlas;
    private final Array<Page> pages = new Array<>(true, 8);
    private final String atlasPath;
    private final TextureAtlasData atlasData;

    public AtlasModel(FileHandle atlasFile) {
        this.atlasFile = atlasFile;
        this.atlasData = new TextureAtlasData(atlasFile, atlasFile.parent(), false);
//        this.atlas = new TextureAtlas(atlasData);
        atlasPath = atlasFile.file().getAbsolutePath();

        generatePages(atlasData);
    }

    private void generatePages(TextureAtlasData atlasData) {
        this.pages.clear();
        Array<TextureAtlasData.Page> pageDataArray = atlasData.getPages();
        for (int i = 0; i < pageDataArray.size; i++) {
            TextureAtlasData.Page pageData = pageDataArray.get(i);
            Page page = new Page(this, pageData, i);
            this.pages.add(page);
        }
    }

//    private void generatePages(TextureAtlas atlas) {
//        Array<Texture> textures = new Array<>();
//
//        // We could use simple atlas.getTextures(), but it returns them in random order...
//        for (TextureRegion region : atlas.getRegions()) {
//            if (!textures.contains(region.getTexture(), true)) {
//                textures.add(region.getTexture());
//            }
//        }
//        pages.clear();
//        for (int i = 0; i < textures.size; i++) {
//            Page page = new Page(this, textures.get(i), i);
//            pages.add(page);
//        }
//    }

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

//    public TextureAtlas getAtlas() {
//        return atlas;
//    }

    public TextureAtlasData getAtlasData() {
        return atlasData;
    }

    public Array<Page> getPages() {
        return pages;
    }

    public static class Page implements Disposable {
        private final AtlasModel atlasModel;
        private final TextureAtlasData.Page pageData;
        private final int pageIndex;
        private final Array<Region> regions = new Array<>(true, 32);

        private Texture texture;

        private Page(AtlasModel atlasModel, TextureAtlasData.Page pageData, int pageIndex) {
            this.atlasModel = atlasModel;
            this.pageData = pageData;
            this.pageIndex = pageIndex;

            // Populate regions.
            Array<TextureAtlasData.Region> regionDataArray = atlasModel.getAtlasData().getRegions();
            for (int i = 0; i < regionDataArray.size; i++) {
                TextureAtlasData.Region regionData = regionDataArray.get(i);
                if (regionData.page == pageData) {
                    Region region = new Region(this, regionData);
                    this.regions.add(region);
                }
            }
        }

        @Override
        public void dispose() {
            if (texture != null) {
                texture.dispose();
                texture = null;
            }
        }

        public AtlasModel getAtlasModel() {
            return atlasModel;
        }

        public TextureAtlasData.Page getPageData() {
            return pageData;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public Array<Region> getRegions() {
            return regions;
        }

        public int getWidth() {
            return (int)pageData.width;
        }

        public int getHeight() {
            return (int)pageData.height;
        }

        /** @return Texture associated with the current page. Maybe null if texture is not loaded yet/loading. */
        public Texture getTexture() {
            if (texture == null) {
                //TODO Load texture in async manner.
                texture = new Texture(pageData.textureFile);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            return texture;
        }
    }

    public static class Region {

        private final Page page;
        private final TextureAtlasData.Region regionData;
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        public Region(Page page, TextureAtlasData.Region regionData) {
            this.page = page;
            this.regionData = regionData;

            this.x = regionData.left;
            this.y = regionData.top;
            this.width = regionData.rotate ? regionData.height : regionData.width;
            this.height = regionData.rotate ? regionData.width : regionData.height;
        }

        public Page getPage() {
            return page;
        }

        public TextureAtlasData.Region getRegionData() {
            return regionData;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }
    }
}
