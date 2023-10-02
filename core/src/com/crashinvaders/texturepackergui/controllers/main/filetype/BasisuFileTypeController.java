package com.crashinvaders.texturepackergui.controllers.main.filetype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.common.scene2d.visui.ToastTable;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.model.FileTypeType;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.BasisuFileTypeModel;
import com.crashinvaders.texturepackergui.controllers.packing.PackDialogController;
import com.crashinvaders.texturepackergui.controllers.packing.processors.BasisuFileTypeProcessor;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.PackAtlasUpdatedEvent;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.views.seekbar.IntSeekBarModel;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBar;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.LmlData;
import com.kotcrab.vis.ui.widget.VisSelectBox;

@Component
@ViewActionContainer("ftcBasisu")
public class BasisuFileTypeController implements FileTypeController {
    private static final String PREF_KEY_IGNORE_BASISU_COMPAT_NOTIF = "ignore_basisu_compat_notif";

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;
    @Inject EventDispatcher eventDispatcher;
    @Inject LocaleService localeService;
    @Inject PackDialogController packDialogController;

    @LmlActor("ftcBasisu") ShrinkContainer container;
    @LmlActor("basisuFileTypeRoot") Actor basisuFileTypeRoot;
    @LmlActor("basisuNotSupportedHint") Actor basisuNotSupportedHint;
    @LmlActor("cboBasisFileContainer") VisSelectBox<FileContainer> cboFileContainer;
    @LmlActor("cboBasisInterFormat") VisSelectBox<IntermediateFormat> cboInterFormat;
    @LmlActor("sbBasisQualityLevel") SeekBar sbQualityLevel;
    @LmlActor("sbBasisCompLevel") SeekBar sbCompLevel;

    private Preferences prefs;

    private BasisuFileTypeModel model;
    private boolean ignoreViewChangeEvents = false;

    @Initiate(priority = Integer.MIN_VALUE) void init() {
        prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
    }

    @Override
    public void onViewCreated(Stage stage) {
        ignoreViewChangeEvents = true;
        cboFileContainer.setItems(FileContainer.values());
        cboInterFormat.setItems(IntermediateFormat.values());
        ignoreViewChangeEvents = false;
    }

    @Override
    public void activate() {
        model = modelService.getProject().getFileType();
        container.setVisible(true);

        basisuFileTypeRoot.setVisible(BasisuFileTypeProcessor.isBasisuSupported());
        basisuNotSupportedHint.setVisible(!BasisuFileTypeProcessor.isBasisuSupported());

        updateFileContainer();
        updateInterFormat();
        updateQualityLevel();
        updateCompressionLevel();
    }

    @Override
    public void deactivate() {
        model = null;
        container.setVisible(false);
    }

    @OnEvent(ProjectInitializedEvent.class) void onEvent(ProjectInitializedEvent event) {
        updateFileContainer();
        updateInterFormat();
        updateQualityLevel();
        updateCompressionLevel();
    }

    @OnEvent(FileTypePropertyChangedEvent.class) void onEvent(FileTypePropertyChangedEvent event) {
        switch (event.getProperty()) {
            case BASIS_KTX2:
                updateFileContainer();
                break;
            case BASIS_UASTC:
                updateInterFormat();
                break;
            case BASIS_QUALITY_LEVEL:
                updateQualityLevel();
                break;
            case BASIS_COMPRESSION_LEVEL:
                updateCompressionLevel();
                break;
        }
    }

    @LmlAction("onFileContainerChanged") void onFileContainerChanged() {
        if (model == null) return;
        if (ignoreViewChangeEvents) return;

        FileContainer container = cboFileContainer.getSelected();
        model.setKtx2(container == FileContainer.KTX2);
    }

    @LmlAction("onInterFormatChanged") void onInterFormatChanged() {
        if (model == null) return;
        if (ignoreViewChangeEvents) return;

        IntermediateFormat format = cboInterFormat.getSelected();
        model.setUastc(format == IntermediateFormat.UASTC);
    }

    @LmlAction("onQualityLevelChanged") void onQualityLevelChanged() {
        if (model == null) return;
        if (ignoreViewChangeEvents) return;

        int qualityLevel = ((IntSeekBarModel) sbQualityLevel.getModel()).getValue();
        model.setQualityLevel(qualityLevel);
    }

    @LmlAction("onCompLevelChanged") void onCompLevelChanged() {
        if (model == null) return;
        if (ignoreViewChangeEvents) return;

        int compLevel = ((IntSeekBarModel) sbCompLevel.getModel()).getValue();
        model.setCompressionLevel(compLevel);
    }

    //region Basis Universal compatibility notifications.
    @OnEvent(PackAtlasUpdatedEvent.class) void onEvent(PackAtlasUpdatedEvent event) {
        if (modelService.getProject().getFileType().getType() != FileTypeType.BASIS) return;
        if (prefs.getBoolean(PREF_KEY_IGNORE_BASISU_COMPAT_NOTIF, false)) return;

        PackModel pack = event.getPack();
        TexturePacker.Settings settings = pack.getSettings();
        if (!settings.pot || !settings.square || !settings.multipleOfFour) {

            final ToastTable toastTable = new ToastTable();

            LmlData lmlData = interfaceService.getParser().getData();
            lmlData.addArgument("toastBasisuPackName", pack.getName());
            lmlData.addActorConsumer("toastBasisuUpdateRepack", actor -> {
                Gdx.app.postRunnable(() -> {
                    toastTable.fadeOut();

                    settings.pot = true;
                    settings.square = true;
                    settings.multipleOfFour = true;
                    pack.fireSettingsChangedEvent();

                    interfaceService.showDialog(packDialogController.getClass());
                    packDialogController.launchPack(modelService.getProject(), pack);
                });
                return null;
            });
            lmlData.addActorConsumer("toastBasisuMuteCompatNotif", actor -> {
                toastTable.fadeOut();

                prefs.putBoolean(PREF_KEY_IGNORE_BASISU_COMPAT_NOTIF, true);
                prefs.flush();

                return null;
            });
            Actor toastContent = interfaceService.getParser()
                    .parseTemplate(Gdx.files.internal("lml/toastBasisuAtlasSettingsPrompt.lml")).first();
            lmlData.removeArgument("toastBasisuPackName");
            lmlData.removeActorConsumer("toastBasisuUpdateRepack");
            lmlData.removeActorConsumer("toastBasisuMuteCompatNotif");

            toastTable.add(toastContent).grow();

            eventDispatcher.postEvent(new ShowToastEvent()
                    .content(toastTable)
                    .duration(ShowToastEvent.DURATION_INDEFINITELY));
        }
    }
    //endregion

    private void updateInterFormat() {
        if (model == null) return;

        IntermediateFormat format = model.isUastc() ? IntermediateFormat.UASTC : IntermediateFormat.ETC1S;
        cboInterFormat.setSelected(format);
    }

    private void updateFileContainer() {
        if (model == null) return;

        FileContainer container = model.isKtx2() ? FileContainer.KTX2 : FileContainer.BASIS;
        cboFileContainer.setSelected(container);
    }

    private void updateQualityLevel() {
        if (model == null) return;

        int qualityLevel = model.getQualityLevel();
        ((IntSeekBarModel) sbQualityLevel.getModel()).setValue(qualityLevel);
    }

    private void updateCompressionLevel() {
        if (model == null) return;

        int compLevel = model.getCompressionLevel();
        ((IntSeekBarModel) sbCompLevel.getModel()).setValue(compLevel);
    }

    public enum IntermediateFormat {
        ETC1S, UASTC
    }

    public enum FileContainer {
        KTX2, BASIS
    }
}
