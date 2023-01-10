package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.packing.PackDialogController;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.layout.GridGroup;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisDialog;

@ViewDialog(id = "dialog_pack_multiple_atlases", value = "lml/packdialogs/dialogPackMultipleAtlases.lml")
public class PackMultipleAtlasesDialogController implements ActionContainer {
    private static final String TAG = PackMultipleAtlasesDialogController.class.getSimpleName();

    private static final ObjectSet<String> lastSelectedItems = new ObjectSet<>();

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject ModelService modelService;
    @Inject PackDialogController packDialogController;

    @LmlActor("dialogRoot") VisDialog dialogRoot;
    @LmlActor("gridGroup") GridGroup gridGroup;
    @LmlActor("btnPackSelected") TextButton btnPackSelected;
    @LmlActor("btnSelectAll") TextButton btnSelectAll;
    @LmlActor("btnDeselectAll") TextButton btnDeselectAll;

    private boolean isDirty = false;

    private final Array<AtlasListItemView> atlasItemViews = new Array<>();

    @LmlAfter
    void initView() {
        Array<PackModel> packModels = modelService.getProject().getPacks();

        dialogRoot.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (dialogRoot == null || dialogRoot.getStage() == null) {
                    return false;
                }

                switch (keycode) {
                    case Input.Keys.ENTER: {
                        onPackSelectedClick();
                        return true;
                    }
                    case Input.Keys.A: {
                        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                                Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                            onSelectAllClick();
                        }
                        return false;
                    }
                    case Input.Keys.D: {
                        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                                Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                            onDeselectAllClick();
                        }
                        return false;
                    }
                }
                return false;
            }
        });

        // Populate atlas items.
        {
            boolean selectionStateRecovered = false;

            atlasItemViews.clear();
            for (int i = 0; i < packModels.size; i++) {
    //        for (int n = 0; n < 8; n++) {
    //            int i = n % packModels.size;
                PackModel packModel = packModels.get(i);
                AtlasListEntry atlasEntry = new AtlasListEntry(packModel);
                if (lastSelectedItems.contains(packModel.getName())) {
                    atlasEntry.setShouldPackAtlas(true);
                    selectionStateRecovered = true;
                }
                AtlasListItemView atlasItemView = new AtlasListItemView(interfaceService.getParser(), atlasEntry, i);
                atlasItemView.setChangeListener(itemView -> setStateDirty());
                atlasItemViews.add(atlasItemView);
            }

            float itemWidth = 128f; // Min item width.
            for (int i = 0; i < atlasItemViews.size; i++) {
                itemWidth = Math.min(256f, Math.max(itemWidth, atlasItemViews.get(i).getPrefWidth()));
            }
            gridGroup.setItemWidth(itemWidth);

            gridGroup.clearChildren();
            for (int i = 0; i < atlasItemViews.size; i++) {
                AtlasListItemView itemView = atlasItemViews.get(i);
                gridGroup.addActor(itemView);
            }

            if (!selectionStateRecovered) {
                onSelectAllClick();
            }
        }

        validateViewState();
    }

    @LmlAction("onSelectAllClick")
    void onSelectAllClick() {
        for (int i = 0; i < atlasItemViews.size; i++) {
            AtlasListItemView itemView = atlasItemViews.get(i);
            itemView.setSelected(true);
        }
        setStateDirty();
    }

    @LmlAction("onDeselectAllClick")
    void onDeselectAllClick() {
        for (int i = 0; i < atlasItemViews.size; i++) {
            AtlasListItemView itemView = atlasItemViews.get(i);
            itemView.setSelected(false);
        }
        setStateDirty();
    }

    @LmlAction("onPackSelectedClick")
    void onPackSelectedClick() {
        // Collect selected atlases.
        Array<PackModel> packModels = new Array<>();
        for (int i = 0; i < atlasItemViews.size; i++) {
            AtlasListEntry atlasEntry = atlasItemViews.get(i).atlasEntry;
            if (atlasEntry.shouldPackAtlas) {
                packModels.add(atlasEntry.packModel);
            }
        }
        if (packModels.size == 0) {
            return;
        }

        hideDialog();

        interfaceService.showDialog(packDialogController.getClass());
        packDialogController.launchPack(modelService.getProject(), packModels);
    }

    @LmlAction("onCloseClick")
    void onCloseClick() {
        hideDialog();
    }

    private void hideDialog() {
        dialogRoot.hide();
        dialogRoot = null;
    }

    private void setStateDirty() {
        if (this.isDirty) return;
        this.isDirty = true;

        Gdx.app.postRunnable(() -> {
            validateViewState();
            this.isDirty = false;
        });
    }

    private void validateViewState() {
        this.isDirty = false;

        lastSelectedItems.clear();

        int selectedAtlasCount = 0;
        for (int i = 0; i < atlasItemViews.size; i++) {
            AtlasListItemView itemView = atlasItemViews.get(i);
            AtlasListEntry atlasEntry = itemView.getAtlasEntry();

            if (atlasEntry.shouldPackAtlas) {
                selectedAtlasCount++;
            }

            // Populate last selected items.
            if (atlasEntry.shouldPackAtlas) {
                lastSelectedItems.add(atlasEntry.getPackModel().getName());
            }
        }

        // Discard last selected items if everything is selected (the default state).
        if (selectedAtlasCount == atlasItemViews.size) {
            lastSelectedItems.clear();
        }

        btnPackSelected.setDisabled(selectedAtlasCount == 0);
        String packAtlasesText = localeService.getI18nBundle().format("dPmPackNAtlases", selectedAtlasCount);
        btnPackSelected.setText(packAtlasesText);

        btnSelectAll.setDisabled(selectedAtlasCount == atlasItemViews.size);
        btnDeselectAll.setDisabled(selectedAtlasCount == 0);
    }

    private static class AtlasListEntry {

        private final PackModel packModel;
        private boolean shouldPackAtlas;

        public AtlasListEntry(PackModel packModel) {
            this.packModel = packModel;
        }

        public PackModel getPackModel() {
            return packModel;
        }

        public boolean isShouldPackAtlas() {
            return shouldPackAtlas;
        }

        public void setShouldPackAtlas(boolean shouldPackAtlas) {
            this.shouldPackAtlas = shouldPackAtlas;
        }
    }

    private static class AtlasListItemView extends Container implements ActionContainer {

        private static final String TAG = AtlasListItemView.class.getSimpleName();

        private final AtlasListEntry atlasEntry;
        private final Skin skin;
        private final Group viewRoot;

        @LmlActor("cbxShouldPackAtlas") VisCheckBox cbxShouldPackAtlas;
        @LmlActor("lblAtlasName") Label lblAtlasName;
        @LmlActor("selectedIndicator") Actor selectedIndicator;

        private ChangeListener changeListener;

        public AtlasListItemView(LmlParser parser, AtlasListEntry atlasEntry, int orderNum) {
            this.atlasEntry = atlasEntry;

            this.skin = parser.getData().getDefaultSkin();

            this.viewRoot = (Group) parser.createView(
                    this,
                    Gdx.files.internal("lml/packdialogs/dialogPackMultipleAtlasesListItem.lml"))
                    .first();

            setActor(viewRoot);
            fill();

            viewRoot.addListener(cbxShouldPackAtlas.getClickListener());

            updateViewFromModel();
        }

        public void setChangeListener(ChangeListener listener) {
            this.changeListener = listener;
        }

        public AtlasListEntry getAtlasEntry() {
            return atlasEntry;
        }

        public void setSelected(boolean selected) {
            atlasEntry.shouldPackAtlas = selected;
            updateViewFromModel();
        }

        private void updateViewFromModel() {
            lblAtlasName.setText(atlasEntry.packModel.getName());
            lblAtlasName.setColor(atlasEntry.shouldPackAtlas ? skin.getColor("text-white") : skin.getColor("text-dim-grey"));

            cbxShouldPackAtlas.setProgrammaticChangeEvents(false);
            cbxShouldPackAtlas.setChecked(atlasEntry.shouldPackAtlas);
            cbxShouldPackAtlas.setProgrammaticChangeEvents(true);

            selectedIndicator.setVisible(atlasEntry.shouldPackAtlas);
        }

        @LmlAction("onShouldPackChange") void onShouldPackChange(VisCheckBox checkBox) {
            atlasEntry.setShouldPackAtlas(checkBox.isChecked());
            updateViewFromModel();

            if (changeListener != null) {
                changeListener.onChange(this);
            }
        }

        public interface ChangeListener {
            void onChange(AtlasListItemView itemView);
        }
    }
}
