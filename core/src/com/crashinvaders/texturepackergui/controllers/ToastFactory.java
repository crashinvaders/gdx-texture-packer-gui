package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.crashinvaders.common.scene2d.visui.Toast;
import com.crashinvaders.common.scene2d.visui.ToastManager;
import com.crashinvaders.common.scene2d.visui.ToastTable;
import com.crashinvaders.texturepackergui.events.RemoveToastEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.widget.VisImageButton;

/**
 * The main toast app-wide producer.
 * Globally handles {@link com.crashinvaders.texturepackergui.events.ShowToastEvent} and {@link com.crashinvaders.texturepackergui.events.RemoveToastEvent}.
 * Also provides a number of handful notification utility methods.
 */
@Component
public class ToastFactory {

    @Inject InterfaceService interfaceService;

    //region Toast handling.
    private final Array<ShowToastEvent> postponedToastEvents = new Array<>();

    // Shall be setup by the current view controller using #setupToastManager().
    private ToastManager toastManager = null;

    public void setupToastManager(ToastManager toastManager) {
        this.toastManager = toastManager;

        Gdx.app.postRunnable(this::processPostponedToastEvents);
    }

    @OnEvent(ShowToastEvent.class) void showToast(final ShowToastEvent event) {
        // Postpone toast events until the view is shown.
        if (toastManager == null) {
            postponedToastEvents.add(event);
            return;
        }

        //FIXME The very first toast events are not getting shown even so "toastManager" is setup and ready.
        // Thus we post to the next frame...
        Gdx.app.postRunnable(() -> {
            final Toast toast;
            if (event.getContent() != null) {
                toast = toastManager.show(event.getContent(), event.getDuration());
            } else {
                toast = toastManager.show(event.getMessage(), event.getDuration());
            }
            // Setup click listener (if provided).
            if (event.getClickAction() != null) {
                Table mainTable = toast.getMainTable();
                mainTable.setTouchable(Touchable.enabled);
                mainTable.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent e, float x, float y) {
                        if (e.getTarget() == e.getListenerActor()) {
                            event.getClickAction().run();
                            toastManager.remove(toast);
                        }
                    }
                });
            }
            if (event.isHideCloseButton()) {
                // Toast doesn't expose public API to hide close button,
                // thus we have to hack a bit...
                Table mainTable = toast.getMainTable();
                SnapshotArray<Actor> children = mainTable.getChildren();
                for (int i = children.size - 1; i >= 0; i--) {
                    Actor child = children.get(i);
                    if (child instanceof VisImageButton) {
                        mainTable.removeActor(child);
                        break;
                    }
                }
            }
        });
    }

    @OnEvent(RemoveToastEvent.class) void removeToast(RemoveToastEvent event) {
        if (toastManager == null)
            return;

        if (event.getToast() != null) {
            toastManager.remove(event.getToast());
        }
    }

    private void processPostponedToastEvents() {
        for (int i = 0; i < this.postponedToastEvents.size; i++) {
            showToast(this.postponedToastEvents.get(i));
        }
        this.postponedToastEvents.clear();
    }
    //endregion

    //region Common toasts.
    private boolean wasRestartToastShown = false;

    public void showRestartToast() {
        if (wasRestartToastShown)
            return;

        ToastTable toastTable = new ToastTable();
        Actor content = interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/toastRestartRequired.lml")).first();
        toastTable.add(content).grow();
        this.showToast(new ShowToastEvent()
                .content(toastTable)
                .duration(ShowToastEvent.DURATION_INDEFINITELY)
                .hideCloseButton());

        wasRestartToastShown = true;
    }
    //endregion
}
