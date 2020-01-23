package com.crashinvaders.texturepackergui.views.canvas.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.common.AsyncTextureLoader;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;

public class PageModel implements Disposable {
    private static final String TAG = PageModel.class.getSimpleName();

    private final AtlasModel atlasModel;
    private final TextureAtlas.TextureAtlasData.Page pageData;
    private final int pageIndex;
    private final Array<RegionModel> regions = new Array<>(true, 32);

    private Texture texture;
    private TextureStatus textureStatus = TextureStatus.NOT_LOADED;
    private AsyncTextureLoader asyncTextureLoader;

    PageModel(AtlasModel atlasModel, TextureAtlas.TextureAtlasData.Page pageData, int pageIndex) {
        this.atlasModel = atlasModel;
        this.pageData = pageData;
        this.pageIndex = pageIndex;

        // Populate regions.
        Array<TextureAtlas.TextureAtlasData.Region> regionDataArray = atlasModel.getAtlasData().getRegions();
        for (int i = 0; i < regionDataArray.size; i++) {
            TextureAtlas.TextureAtlasData.Region regionData = regionDataArray.get(i);
            if (regionData.page == pageData) {
                RegionModel region = new RegionModel(this, regionData);
                this.regions.add(region);
            }
        }
    }

    @Override
    public void dispose() {
        if (asyncTextureLoader != null) {
            asyncTextureLoader.dispose();
            asyncTextureLoader = null;
        }
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }

    public AtlasModel getAtlasModel() {
        return atlasModel;
    }

    public TextureAtlas.TextureAtlasData.Page getPageData() {
        return pageData;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public Array<RegionModel> getRegions() {
        return regions;
    }

    public int getWidth() {
        return (int)pageData.width;
    }

    public int getHeight() {
        return (int)pageData.height;
    }

    /** @return Texture associated with the current page.
     * May be null if the texture is not loaded/loading/error loading. */
    public Texture getTexture() {
        if (texture == null) {
            requestTextureLoading();
        }
        return texture;
    }

    private void requestTextureLoading() {
        if (textureStatus != TextureStatus.NOT_LOADED) return;

        if (asyncTextureLoader != null) {
            throw new IllegalStateException("Texture status is NOT_LOADED, but async texture loader was already been created.");
        }

        textureStatus = TextureStatus.LOADING;
        asyncTextureLoader = new AsyncTextureLoader(pageData.textureFile, new AsyncTextureLoader.CompletionListener() {
            @Override
            public void onTextureLoaded(Texture loadedTexture) {
                if (texture != null) {
                    throw new IllegalStateException("Texture is already set for the page: " + pageData.textureFile);
                }
                texture = loadedTexture;
                asyncTextureLoader = null;
                textureStatus = TextureStatus.LOADED;
            }

            @Override
            public void onTextureLoadFailed(Exception error) {
                if (error != null) {
                    Gdx.app.error(TAG, "Error loading page texture: " + pageData.textureFile, error);
                    showPageLoadErrorToast(error);
                }
                asyncTextureLoader = null;
                textureStatus = TextureStatus.ERROR;
            }
        });
    }

    private void showPageLoadErrorToast(final Exception error) {
        String messageText = App.inst().getI18n().get("toastPageTextureLoadError");

        App.inst().getEventDispatcher().postEvent(new ShowToastEvent()
                .message(messageText)
                .duration(ShowToastEvent.DURATION_LONG)
                .click(new Runnable() {
                    @Override
                    public void run() {
                        ErrorDialogController errorDialogController = (ErrorDialogController)
                                App.inst().getContext().getComponent(ErrorDialogController.class);
                        errorDialogController.setError(error);
                        App.inst().getInterfaceService().showDialog(ErrorDialogController.class);
                    }
                }));
    }

    private enum TextureStatus {
        NOT_LOADED,
        LOADING,
        LOADED,
        ERROR
    }
}
