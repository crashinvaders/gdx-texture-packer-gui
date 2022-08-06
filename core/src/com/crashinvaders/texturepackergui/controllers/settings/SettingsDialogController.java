package com.crashinvaders.texturepackergui.controllers.settings;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.common.autumn.DependencyInjectionService;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.ApplicationLogDialogController;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.controllers.ToastFactory;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewDialogShower;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.kiwi.util.gdx.reflection.Reflection;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.HorizontalCollapsibleWidget;
import com.kotcrab.vis.ui.widget.VisDialog;

@ViewDialog(id = SettingsDialogController.VIEW_ID, value = "lml/settings/dialogSettings.lml")
public class SettingsDialogController implements ViewDialogShower, ActionContainer {

    public static final String VIEW_ID = "dialog_settings";
    private static final String TAG = SettingsDialogController.class.getSimpleName();

    public static final String SECTION_ID_GENERAL = "siGeneral";
    public static final String SECTION_ID_HOTKEYS = "siHotkeys";
    public static final String SECTION_ID_EXTENSIONS = "siExtensionModules";

    private static String SECTION_ID_INIT_OVERRIDE = null;

    @Inject InterfaceService interfaceService;
    @Inject DependencyInjectionService injectionService;
    @Inject ToastFactory toastFactory;
    @Inject Stage stage;

    @LmlActor({SECTION_ID_GENERAL, SECTION_ID_HOTKEYS, SECTION_ID_EXTENSIONS})
    ObjectMap<String, Button> sectionButtons;

    @LmlActor Container sectionContentContainer;

    @LmlActor HorizontalCollapsibleWidget cpsRestartApp;
    @LmlActor Button btnRestartApp;

    private VisDialog dialog;

    private String currentSectionId = SECTION_ID_GENERAL;
    private SectionContentController currentSectionController = null;

    private boolean isAppRestartRequired = false;

    private boolean isDialogSeeThrough = false;

    @Override
    public void doBeforeShow(Window dialog) {
        this.dialog = (VisDialog) dialog;

        String initialSectionId = currentSectionId;

        if (SECTION_ID_INIT_OVERRIDE != null) {
            initialSectionId = SECTION_ID_INIT_OVERRIDE;
            SECTION_ID_INIT_OVERRIDE = null;
        }

        toggleSection(initialSectionId, true);

        this.isDialogSeeThrough = false;
    }

    @LmlAfter
    void initView() {
        btnRestartApp.setOrigin(Align.right);
        btnRestartApp.addAction(Actions.repeat(-1, Actions.sequence(
                Actions.delay(1f),
                Actions.scaleTo(1.07f, 1.15f),
                Actions.scaleTo(1f, 1f, 0.5f, Interpolation.exp10Out)
        )));

        if (isAppRestartRequired) {
            cpsRestartApp.setCollapsed(false);

        }
    }

    public void toggleSection(String sectionId, boolean force) {
        if (Strings.isBlank(sectionId))
            throw new IllegalArgumentException("sectionId cannot be empty.");

        // Do not switch to the same section.
        if (!force && sectionId.equals(currentSectionId))
            return;

        if (currentSectionController != null) {
            currentSectionController.hide();
            currentSectionController = null;
        }

        this.currentSectionId = sectionId;

        SectionContentController controller = createSectionController(sectionId);
        this.currentSectionController = controller;
        controller.show(sectionContentContainer);

        Button sectionButton = sectionButtons.get(sectionId, null);
        if (sectionButton != null)
            sectionButton.setChecked(true);
    }

    public void hide() {
        if (dialog == null)
            return;

        if (currentSectionController != null) {
            currentSectionController.hide();
            currentSectionController = null;
        }

        dialog.hide();
        dialog = null;
    }

    public boolean isShown() {
        return dialog != null && dialog.getStage() != null;
    }

    public void requestAppRestart() {
        if (isAppRestartRequired)
            return;

        isAppRestartRequired = true;
        cpsRestartApp.setCollapsed(false);

        toastFactory.showRestartToast();
    }

    public void setDialogSeeThrough(boolean enabled) {
        if (this.isDialogSeeThrough == enabled) return;
        this.isDialogSeeThrough = enabled;

        if (enabled) {
            dialog.addAction(Actions.alpha(0.25f, 0.25f));
        } else {
            dialog.addAction(Actions.alpha(1f, 0.25f));
        }
    }

    public void centerWindow() {
        dialog.centerWindow();
    }

    @LmlAction("onBackPressed")
    void onBackPressed() {
        //TODO Hide dialog.
    }

    @LmlAction("onSelectedSectionChanged")
    void onSelectedSectionChanged(Button sectionButton) {
        // Ignore the "uncheck" events.
        if (!sectionButton.isChecked()) return;

        String sectionId = sectionButton.getName();
        toggleSection(sectionId, false);
    }

    @LmlAction("showAppLog")
    void locateLogFile() {
        ApplicationLogDialogController.show();
    }

    private SectionContentController createSectionController(String sectionId) {
        SectionContentController controller;
        switch (sectionId) {
            case SECTION_ID_GENERAL:
                controller = new GeneralSectionController();
                break;
            case SECTION_ID_HOTKEYS:
                controller = new HotkeysSectionController();
                break;
            case SECTION_ID_EXTENSIONS:
                controller = new ExtensionModuleSectionController();
                break;
            default:
                throw new IllegalArgumentException("Unexpected section ID: " + sectionId);
        }

        injectionService.process(controller);
        return controller;
    }

    /** Displays the settings dialog with the default/last activated section. */
    public static void show() {
        InterfaceService interfaceService = App.inst().getInterfaceService();
        if (interfaceService == null) {
            return;
        }

        interfaceService.showDialog(SettingsDialogController.class);
    }

    /** Displays the settings dialog with the specific section activated. */
    public static void show(String sectionId) {
        InterfaceService interfaceService = App.inst().getInterfaceService();
        if (interfaceService == null) {
            return;
        }

        SettingsDialogController dialogController = (SettingsDialogController)
                App.inst().getContext().getComponent(SettingsDialogController.class);

        if (dialogController.isShown()) {
            dialogController.toggleSection(sectionId, false);
            return;
        }

        SettingsDialogController.SECTION_ID_INIT_OVERRIDE = sectionId;
        interfaceService.showDialog(SettingsDialogController.class);
    }
}
