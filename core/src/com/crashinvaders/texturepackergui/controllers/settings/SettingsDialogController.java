package com.crashinvaders.texturepackergui.controllers.settings;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.common.autumn.DependencyInjectionService;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.ApplicationLogDialogController;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewDialogShower;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.kiwi.util.gdx.reflection.Reflection;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.HorizontalCollapsibleWidget;
import com.kotcrab.vis.ui.widget.VisDialog;

@ViewDialog(id = SettingsDialogController.VIEW_ID, value = "lml/settings/dialogSettings.lml")
public class SettingsDialogController implements ViewDialogShower, ActionContainer {

    public static final String VIEW_ID = "dialog_settings";
    private static final String TAG = SettingsDialogController.class.getSimpleName();

    public static final String SECTION_ID_GENERAL = "siGeneral";
    public static final String SECTION_ID_HOTKEYS = "siHotkeys";
    private static final ObjectMap<String, Class<? extends SectionContentController>> sectionControllerMap = new ObjectMap<>();
    static {
        sectionControllerMap.put(SECTION_ID_GENERAL, GeneralSectionController.class);
        sectionControllerMap.put(SECTION_ID_HOTKEYS, HotkeysSectionController.class);
    }

    private static String SECTION_ID_INIT_OVERRIDE = null;

    @Inject InterfaceService interfaceService;
    @Inject DependencyInjectionService injectionService;
    @Inject Stage stage;

    @LmlActor({SECTION_ID_GENERAL, SECTION_ID_HOTKEYS})
    ObjectMap<String, Button> sectionButtons;

    @LmlActor Container sectionContentContainer;

    @LmlActor HorizontalCollapsibleWidget cpsRestartApp;

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

    public void toggleSection(String sectionId, boolean force) {
        if (Strings.isBlank(sectionId))
            throw new IllegalArgumentException("sectionId cannot be empty.");

        // Do not switch to the same section.
        if (!force && sectionId.equals(currentSectionId))
            return;

        Class<? extends SectionContentController> controllerType = sectionControllerMap.get(sectionId);

        if (controllerType == null)
            throw new IllegalArgumentException("No defined section content controller for ID: " + sectionId);

        if (currentSectionController != null) {
            currentSectionController.hide();
            currentSectionController = null;
        }

        this.currentSectionId = sectionId;

        SectionContentController controller = createSectionController(controllerType);
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

    public void requestAppRestart() {
        if (isAppRestartRequired)
            return;

        isAppRestartRequired = true;
        cpsRestartApp.setCollapsed(false);
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

    private SectionContentController createSectionController(Class<? extends SectionContentController> type) {
        SectionContentController controller = Reflection.newInstance(type);
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

        SettingsDialogController.SECTION_ID_INIT_OVERRIDE = sectionId;
        interfaceService.showDialog(SettingsDialogController.class);
    }
}
