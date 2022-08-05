package com.crashinvaders.texturepackergui.views;

import com.badlogic.gdx.utils.Null;
import com.kotcrab.vis.ui.widget.VisSelectBox;

//public class LanguageSelectBox extends CustomSelectBox<LanguageSelectBox.Entry> {
public class LanguageSelectBox extends VisSelectBox<LanguageSelectBox.Entry> {

    public static class Entry {
        public final @Null String displayName;
        public final @Null String displayImage;
        public final @Null Object userData;

        public static Entry withText(String displayName, Object userData) {
            return new Entry(displayName, null, userData);
        }

        public static Entry withImage(String displayImage, Object userData) {
            return new Entry(null, displayImage, userData);
        }

        private Entry(String displayName, String displayImage, Object userData) {
            this.displayName = displayName;
            this.displayImage = displayImage;
            this.userData = userData;

            if (displayName == null && displayImage == null)
                throw new IllegalStateException("Either displayName or displayImage must be provided.");
        }

        @Override
        public String toString() {
            return displayName != null ? displayName : displayImage;
        }
    }
}
