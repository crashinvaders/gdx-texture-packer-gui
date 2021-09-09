package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.crashinvaders.common.scene2d.Scene2dUtils;
import com.crashinvaders.common.scene2d.actions.ActionsExt;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.adapter.ListSelectionAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

@Component
public class PackListActors implements ActionContainer {

    @LmlActor("btnMenuNewPack") Button btnMenuNewPack;
    @LmlActor("btnMenuRenamePack") Button btnMenuRenamePack;
    @LmlActor("btnMenuCopyPack") Button btnMenuCopyPack;
    @LmlActor("btnMenuDeletePack") Button btnMenuDeletePack;
    @LmlActor("btnMenuPackUp") Button btnMenuPackUp;
    @LmlActor("btnMenuPackDown") Button btnMenuPackDown;
    @LmlActor("btnMenuOpenProject") Button btnMenuOpenProject;
    @LmlActor("btnMenuCloseProject") Button btnMenuCloseProject;
    @LmlActor("edtOutputDir") VisTextField edtOutputDir;
    @LmlActor("btnPickOutputDir") Button btnPickOutputDir;
    @LmlActor("edtFileName") VisTextField edtFileName;
    @LmlActor("btnPackAll") Button btnPackAll;
    @LmlActor("btnPackSelected") Button btnPackSelected;

    @LmlActor("plOnboardingRoot") Group plOnboardingRoot;
    @LmlActor("plOnboardingBackground") Image plOnboardingBackground;
    @LmlActor("plOnboardingContent") Group plOnboardingContent;
    @LmlActor("plOnboardingBtnNew") Button plOnboardingBtnNew;

    // Icon panel ribbon.
    @LmlActor("btnSpNew") Button btnSpNew;
    @LmlActor("btnSpDelete") Button btnSpDelete;
    @LmlActor("btnSpRename") Button btnSpRename;
    @LmlActor("btnSpCopy") Button btnSpCopy;
    @LmlActor("btnSpMoveUp") Button btnSpMoveUp;
    @LmlActor("btnSpMoveDown") Button btnSpMoveDown;
    @LmlActor("btnSpPackSelected") Button btnSpPackSelected;
    @LmlActor("btnSpPackAll") Button btnSpPackAll;

    @LmlActor("lvPacks") ListView.ListViewTable<PackModel> packListTable;
    ListView<PackModel> packList;
    PackListAdapter packListAdapter;

    @Inject ModelService modelService;

    public void onViewCreated(Stage stage) {
        ProjectModel project = modelService.getProject();

        packList = packListTable.getListView();
        packListAdapter = ((PackListAdapter) packList.getAdapter());
        packListAdapter.getSelectionManager().setListener(new ListSelectionAdapter<PackModel, VisTable>() {
            @Override
            public void selected(PackModel pack, VisTable view) {
                project.setSelectedPack(pack);

                // Normalize pack list scroll.
                Gdx.app.postRunnable(() -> {
                    PackModel selectedPack = modelService.getProject().getSelectedPack();
                    if (selectedPack != null) {
                        Scene2dUtils.scrollDownToSelectedListItem(packList, selectedPack);
                    }
                });
            }
        });

        refreshIconPanelState();
        refreshOnboardingView();
    }

    private void refreshIconPanelState() {
        boolean anyPackExists = modelService.getProject().getPacks().size > 0;

        btnSpDelete.setDisabled(!anyPackExists);
        btnSpRename.setDisabled(!anyPackExists);
        btnSpCopy.setDisabled(!anyPackExists);
        btnSpMoveUp.setDisabled(!anyPackExists);
        btnSpMoveDown.setDisabled(!anyPackExists);
        btnSpPackSelected.setDisabled(!anyPackExists);
        btnSpPackAll.setDisabled(!anyPackExists);
    }

    private void refreshOnboardingView() {
        boolean visible = modelService.getProject().getPacks().size == 0;

        if (visible) {
            plOnboardingRoot.setVisible(true);
            plOnboardingBackground.getColor().a = 0f;
            plOnboardingBtnNew.setTransform(true);
            plOnboardingBtnNew.addAction(Actions.forever(Actions.sequence(
                    Actions.delay(3f),
                    ActionsExt.origin(Align.center),
                    Actions.scaleTo(1.1f, 0.95f),
                    Actions.targeting(plOnboardingBackground, Actions.alpha(0.25f)),
                    Actions.parallel(
                            Actions.scaleTo(1.0f, 1.0f, 0.5f, Interpolation.elasticOut),
                            Actions.targeting(plOnboardingBackground, Actions.fadeOut(0.5f))
                    )
            )));
        } else {
            plOnboardingRoot.setVisible(false);
            plOnboardingBtnNew.setTransform(false);
            plOnboardingBtnNew.clearActions();
        }
    }

    @OnEvent(ProjectPropertyChangedEvent.class) void OnEvent(ProjectPropertyChangedEvent event) {
        switch (event.getProperty()) {
            case PACKS:
                refreshIconPanelState();
                refreshOnboardingView();
                break;
        }
    }
}
