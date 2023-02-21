package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.scene2d.visui.Toast;
import com.crashinvaders.common.scene2d.visui.ToastTable;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.packing.processors.*;
import com.crashinvaders.texturepackergui.events.PackAtlasUpdatedEvent;
import com.crashinvaders.texturepackergui.events.RemoveToastEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.controllers.TinifyService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.ScaleFactorModel;
import com.crashinvaders.texturepackergui.utils.WidgetUtils;
import com.crashinvaders.texturepackergui.utils.packprocessing.CompositePackProcessor;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingManager;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.kiwi.util.common.Exceptions;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.adapter.ListAdapter;
import com.kotcrab.vis.ui.widget.*;

@ViewDialog(id = "dialog_packing", value = "lml/dialogPacking.lml")
public class PackDialogController implements ActionContainer {
    private static final String PREF_KEY_AUTO_CLOSE = "auto_close_pack_dialog";

    @Inject InterfaceService interfaceService;
    @Inject EventDispatcher eventDispatcher;
    @Inject LocaleService localeService;
    @Inject TinifyService tinifyService;

    @ViewStage Stage stage;
    private Preferences prefs;

    @LmlActor("window") VisDialog window;
    @LmlActor("scrItems") VisScrollPane scrItems;
    @LmlActor("listItems") ListView.ListViewTable<PackProcessingNode> listItems;
    @LmlActor("cbAutoClose") VisCheckBox cbAutoClose;
    @LmlActor("progressBar") VisProgressBar progressBar;
    private VisImageButton btnClose;

    /** Last show "reopen last packing results" toast notification */
    private Toast prevReopenDialogToast;

    @Initiate
    public void initialize() {
        prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);
    }

    @LmlAfter
    public void initView() {
        btnClose = WidgetUtils.obtainCloseButton(window);
        btnClose.setColor(new Color(0xffffff44));
        btnClose.setDisabled(true);

        stage.setScrollFocus(scrItems);

        cbAutoClose.setChecked(prefs.getBoolean(PREF_KEY_AUTO_CLOSE, false));
    }

    @LmlAction("obtainListAdapter") ListAdapter obtainListAdapter() {
        return new PackProcessingListAdapter(interfaceService);
    }

    @LmlAction("onAutoCloseChecked") void onAutoCloseChecked(VisCheckBox cbAutoClose) {
        prefs.putBoolean(PREF_KEY_AUTO_CLOSE, cbAutoClose.isChecked());
        prefs.flush();
    }

    public void launchPack(ProjectModel project, PackModel pack) {
        launchPack(project, Array.with(pack));
    }

    public void launchPack(ProjectModel project, Array<PackModel> packs) {
        Array<PackProcessingNode> nodes = PackingProcessorUtils.prepareProcessingNodes(project, packs);

        PackProcessingListAdapter adapter = (PackProcessingListAdapter)listItems.getListView().getAdapter();
        adapter.clear();
        for (PackProcessingNode node : nodes) {
            adapter.add(node);
        }

        // Pack and align at center
        window.pack();
        window.setPosition(
                Math.round((stage.getWidth() - window.getWidth()) / 2),
                Math.round((stage.getHeight() - window.getHeight()) / 2));

        PackProcessingManager packProcessingManager = new PackProcessingManager(
                PackingProcessorUtils.prepareRegularProcessorSequence(tinifyService),
                new PackProcessingManager.GdxSyncListenerWrapper(new PackWorkerListener()),
                4);

        for (int i = 0; i < nodes.size; i++) {
            PackProcessingNode node = nodes.get(i);
            packProcessingManager.postProcessingNode(node);
        }
        packProcessingManager.execute();
    }

    private void showReopenLastDialogNotification(boolean hasErrors, boolean hasWarnings) {
        final LmlParser parser = interfaceService.getParser();
        final ToastTable toastTable = new ToastTable();
        final String actionName = "showLastDialog";

        if (prevReopenDialogToast != null) {
            eventDispatcher.postEvent(new RemoveToastEvent().toast(prevReopenDialogToast));
            prevReopenDialogToast = null;
        }

        parser.getData().addActorConsumer(actionName,
                (ActorConsumer<Void, Object>) actor -> {
                    if (window != null) {
                        window.show(stage);

                        PackProcessingListAdapter adapter = (PackProcessingListAdapter) listItems.getListView().getAdapter();
                        if ((hasErrors || hasWarnings) && adapter.size() == 1) {
                            adapter.getView(adapter.get(0)).showLogWindow();
                        }
                    }
                    toastTable.fadeOut();
                    return null;
                });

        Group content = (Group)parser.parseTemplate(Gdx.files.internal("lml/toastReopenLastPackingDialog.lml")).first();

        if (hasWarnings) {
            content.findActor("lnkWarningHint").setVisible(true);
        }

        parser.getData().removeActorConsumer(actionName);
        toastTable.add(content).grow();

        eventDispatcher.postEvent(new ShowToastEvent()
                .content(toastTable)
                .duration(10f));

        prevReopenDialogToast = toastTable.getToast();
    }

    /** @return localized string */
    private String getString(String key) {
        return localeService.getI18nBundle().get(key);
    }

    /** @return localized string */
    private String getString(String key, Object... args) {
        return localeService.getI18nBundle().format(key, args);
    }

    private class PackWorkerListener implements PackProcessingManager.Listener {
        final PackProcessingListAdapter adapter;
        boolean hasErrors = false;
        boolean hasWarnings = false;
        int finishedCounter = 0;

        public PackWorkerListener() {
            adapter = (PackProcessingListAdapter)listItems.getListView().getAdapter();
        }

        @Override
        public void onProcessingStarted() {
            progressBar.setRange(0, adapter.size());
            progressBar.setValue(0);
        }

        @Override
        public void onProcessingFinished() {
            btnClose.setDisabled(false);
            btnClose.setColor(Color.WHITE);

            FocusManager.switchFocus(stage, btnClose);

            window.closeOnEscape();

            if (!hasErrors && cbAutoClose.isChecked()) {
                window.hide();
                showReopenLastDialogNotification(hasErrors, hasWarnings);
            }

            // If there is only one pack, show log on error.
            if (hasErrors && adapter.size() == 1) {
                adapter.getView(adapter.get(0)).showLogWindow();
            }

            // Indicate total result by changing progress bar color.
            {
                ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle(progressBar.getStyle());
                Drawable fill = hasErrors ?
                        VisUI.getSkin().getDrawable("progressBarErr") :
                        VisUI.getSkin().getDrawable("progressBarSucc");
                style.knob = fill;
                style.knobBefore = fill;
                progressBar.setStyle(style);
            }
        }

        @Override
        public void onBegin(PackProcessingNode node) {
        }

        @Override
        public void onError(final PackProcessingNode node, Exception e) {
            onFinished(node);

            adapter.getView(node).setToError(e);
            hasErrors = true;
        }

        @Override
        public void onSuccess(PackProcessingNode node) {
            onFinished(node);

            this.hasWarnings |= node.getMetadata(PackProcessingNode.META_HAS_WARNINGS, false);

            adapter.getView(node).setToSuccess(hasWarnings);
        }

        private void onFinished(final PackProcessingNode node) {
            adapter.getView(node).onFinishProcessing();

            finishedCounter += 1;
            progressBar.setValue(finishedCounter);

            // Notification
            Gdx.app.postRunnable(() -> {
                eventDispatcher.postEvent(new PackAtlasUpdatedEvent(node.getOrigPack()));

                // Since the atlas packing operation is pretty memory consuming,
                // it is reasonable to manually cleanup memory a bit.
                System.gc();
            });
        }
    }

    private static class TestProcessor implements PackProcessor {
        @Override
        public void processPackage(PackProcessingNode node) {
            try {
                System.out.println("start processing");
                Thread.sleep(MathUtils.random(500, 2500));
                if (MathUtils.randomBoolean()) throw new RuntimeException();
                System.out.println("finish processing");
            } catch (InterruptedException e) {
                Exceptions.throwRuntimeException(e);
            }
        }
    }
}