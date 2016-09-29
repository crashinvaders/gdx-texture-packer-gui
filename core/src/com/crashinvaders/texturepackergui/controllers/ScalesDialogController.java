package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ScaleModel;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.util.adapter.ListAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTable;

@ViewDialog(id = "dialog_pack_scales", value = "lml/packdialogs/dialogPackScales.lml")
public class ScalesDialogController implements ActionContainer {
    private static final String TAG = ScalesDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject EventDispatcher eventDispatcher;
    @Inject LocaleService localeService;

    @LmlActor("dialog") VisDialog dialog;
    @LmlActor("listScales") ListView.ListViewTable<ScaleModel> listScales;

    @ViewStage Stage stage;

    private PackModel packModel;

    public void setPackModel(PackModel packModel) {
        this.packModel = packModel;
    }

    @LmlAfter void initView() {
        if (packModel == null) {
            Gdx.app.error(TAG, "Pack model is not set");
            return;
        }

        listScales.getListView().getAdapter().add(new ScaleModel());
        listScales.getListView().getAdapter().add(new ScaleModel());
        listScales.getListView().getAdapter().add(new ScaleModel());
    }

    @LmlAction("obtainListAdapter") ListAdapter obtainListAdapter() {
        return new ScaleListAdapter(interfaceService);
    }

    @LmlAction("saveAndCloseDialog") void saveAndCloseDialog() {
        //TODO save scales to pack
        closeDialog();
    }

    @LmlAction("closeDialog") void closeDialog() {
        dialog.fadeOut();
    }

    private static class ScaleListAdapter extends ArrayAdapter<ScaleModel, ScaleListViewItem> {

        private final InterfaceService interfaceService;

        public ScaleListAdapter(InterfaceService interfaceService) {
            super(new Array<ScaleModel>());
            this.interfaceService = interfaceService;
        }

        @Override
        protected ScaleListViewItem createView(ScaleModel item) {
            VisTable view = (VisTable)interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/packdialogs/packScaleListItem.lml")).first();
            return new ScaleListViewItem(item, view);
        }
    }

    private static class ScaleListViewItem extends Container<VisTable> {
        private final ScaleModel scaleModel;
        private final VisTable view;

        public ScaleListViewItem(ScaleModel scaleModel, VisTable view) {
            this.scaleModel = scaleModel;
            this.view = view;
        }
    }
}
