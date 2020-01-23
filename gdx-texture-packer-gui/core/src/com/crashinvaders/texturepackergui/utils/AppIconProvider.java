package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.crashinvaders.texturepackergui.AppConstants;
import com.kotcrab.vis.ui.widget.file.FileChooser;

public class AppIconProvider extends FileChooser.DefaultFileIconProvider {
    public AppIconProvider(FileChooser chooser) {
        super(chooser);
    }

    @Override
    protected Drawable getDefaultIcon(FileChooser.FileItem item) {
        String ext = item.getFile().extension().toLowerCase();
        if (ext.equals(AppConstants.PROJECT_FILE_EXT)) {
            return chooser.getSkin().getDrawable("custom/ic-gdx-texture-packer");
        }

        return null;
    }
}
