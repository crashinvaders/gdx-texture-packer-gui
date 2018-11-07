package com.crashinvaders.texturepackergui.views.canvas.model;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class RegionModel {

    private final PageModel page;
    private final TextureAtlas.TextureAtlasData.Region regionData;
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public RegionModel(PageModel page, TextureAtlas.TextureAtlasData.Region regionData) {
        this.page = page;
        this.regionData = regionData;

        this.x = regionData.left;
        this.y = regionData.top;
        this.width = regionData.rotate ? regionData.height : regionData.width;
        this.height = regionData.rotate ? regionData.width : regionData.height;
    }

    public PageModel getPage() {
        return page;
    }

    public TextureAtlas.TextureAtlasData.Region getRegionData() {
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
