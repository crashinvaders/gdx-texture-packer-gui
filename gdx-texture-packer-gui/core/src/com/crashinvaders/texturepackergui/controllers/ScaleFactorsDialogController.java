package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Resampling;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ScaleFactorModel;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.util.adapter.ListAdapter;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.spinner.FloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@ViewDialog(id = "dialog_pack_scales", value = "lml/packdialogs/dialogPackScaleFactors.lml")
public class ScaleFactorsDialogController implements ActionContainer {
    private static final String TAG = ScaleFactorsDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject EventDispatcher eventDispatcher;
    @Inject LocaleService localeService;

    @LmlActor("dialog") VisDialog dialog;
    @LmlActor("listScales") ListView.ListViewTable<ScaleFactorModel> listScales;
    @LmlActor("errorContainer")
    ShrinkContainer<com.badlogic.gdx.scenes.scene2d.Actor> errorContainer;
    @LmlActor("lblError") VisLabel lblError;

    @ViewStage Stage stage;

    private PackModel packModel;
    private ScaleListAdapter listAdapter;

    public void setPackModel(PackModel packModel) {
        this.packModel = packModel;
    }

    @LmlAfter void initView() {
        if (packModel == null) {
            Gdx.app.error(TAG, "Pack model is not set");
            return;
        }

        listAdapter = ((ScaleListAdapter) listScales.getListView().getAdapter());

        for (ScaleFactorModel model : packModel.getScaleFactors()) {
            listAdapter.add(model);
        }

        // Focus scroll pane
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                stage.setScrollFocus(listScales.getListView().getScrollPane());
            }
        });
    }

    @LmlAction("obtainListAdapter")
    ListAdapter<ScaleFactorModel> obtainListAdapter() {
        return new ScaleListAdapter(interfaceService);
    }

    @LmlAction("createScaleRecord") void createScaleRecord() {
        listScales.getListView().getAdapter().add(new ScaleFactorModel("", 1f, Resampling.bicubic));
    }

    @LmlAction("saveAndCloseDialog") void saveAndCloseDialog() {
        Array<ScaleFactorModel> scaleFactors = new Array<>();
        for (int i = 0; i < listAdapter.size(); i++) {
            ScaleListViewItem view = listAdapter.getView(listAdapter.get(i));
            ScaleFactorModel model = new ScaleFactorModel(view.getSuffix(), view.getFactor(), view.getResampling());
            scaleFactors.add(model);
        }

        String error = validate(scaleFactors);
        errorContainer.setVisible(error != null);
        if (error != null) {
            lblError.setText(error);
            return;
        }

        packModel.setScaleFactors(scaleFactors);

        closeDialog();
    }

    @LmlAction("closeDialog") void closeDialog() {
        dialog.fadeOut();
    }

    /** @return non null value in case there is an error */
    private String validate(Array<ScaleFactorModel> scaleFactors) {
        if (scaleFactors.size == 0) {
            return localeService.getI18nBundle().get("dSfErrNoEntries");
        }

        // Unique suffixes check
        Set<String> suffixes = new HashSet<>(scaleFactors.size);
        for (ScaleFactorModel scaleFactor : scaleFactors) {
            if (!suffixes.add(scaleFactor.getSuffix())) {
                return localeService.getI18nBundle().get("dSfErrNonUniqueSuffixes");
            }
        }

        return null;
    }

    private static class ScaleListAdapter extends ArrayAdapter<ScaleFactorModel, ScaleListViewItem> {

        private final InterfaceService interfaceService;

        public ScaleListAdapter(InterfaceService interfaceService) {
            super(new Array<ScaleFactorModel>());
            this.interfaceService = interfaceService;
        }

        @Override
        protected ScaleListViewItem createView(ScaleFactorModel item) {
            LmlParser lmlParser = interfaceService.getParser();
            ScaleListViewItem view = new ScaleListViewItem(this, item);
            lmlParser.getData().addArgument(ScaleListViewItem.ARG_EVEN_ROW, indexOf(item) % 2 == 0);
            lmlParser.createView(view, Gdx.files.internal("lml/packdialogs/packScaleFactorListItem.lml"));
            return view;
        }
    }

    static class ScaleListViewItem extends Container<VisTable> implements ActionContainer {
        static final String ARG_EVEN_ROW = "evenRow";

        private final ScaleListAdapter adapter;
        private final ScaleFactorModel model;

        @LmlActor("content") VisTable content;
        @LmlActor("edtSuffix") VisTextField edtSuffix;
        @LmlActor("spnScale") Spinner spnScale;
        @LmlActor("cmbResampling")
        VisSelectBox<ResamplingOption> cmbResampling;

        private FloatSpinnerModel spnScaleModel;

        public ScaleListViewItem(ScaleListAdapter adapter, ScaleFactorModel model) {
            this.adapter = adapter;
            this.model = model;
        }

        @LmlAfter void init() {
            setActor(content);
            fill();

            edtSuffix.setText(model.getSuffix());
            spnScaleModel = (FloatSpinnerModel) spnScale.getModel();
            spnScaleModel.setValue(BigDecimal.valueOf(model.getFactor()));

            cmbResampling.setItems(ResamplingOption.values());
            cmbResampling.setSelected(ResamplingOption.find(model.getResampling()));
        }

        @LmlAction("deleteRecord") void deleteRecord() {
            adapter.removeValue(model, true);
        }

        public String getSuffix() {
            return edtSuffix.getText();
        }

        public float getFactor() {
            return spnScaleModel.getValue().floatValue();
        }

        public Resampling getResampling() {
            return (cmbResampling.getSelected()).resampling;
        }

        public enum ResamplingOption {
            NEAREST(Resampling.nearest, "Nearest"),
            BILINEAR(Resampling.bilinear, "Bilinear"),
            BICUBIC(Resampling.bicubic, "Bicubic"),;

            final Resampling resampling;
            final String displayName;

            ResamplingOption(Resampling resampling, String displayName) {
                this.resampling = resampling;
                this.displayName = displayName;
            }

            @Override
            public String toString() {
                return displayName;
            }

            public static ResamplingOption find(Resampling resampling) {
                for (ResamplingOption value : values()) {
                    if (value.resampling == resampling) {
                        return value;
                    }
                }
                return null;
            }
        }
    }
}
