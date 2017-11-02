package com.crashinvaders.texturepackergui.controllers.test;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.crashinvaders.common.async.AsyncJobTask;
import com.crashinvaders.common.async.JobTask;
import com.crashinvaders.common.async.JobTaskQueue;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewDialogShower;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;

@ViewDialog(id = ModalTaskDialogController.VIEW_ID, value = "lml/modalTaskDialog.lml")
public class ModalTaskDialogController implements ActionContainer, ViewDialogShower {
    public static final String VIEW_ID = "ModalTaskDialog";
    public static final String TAG = ModalTaskDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;

    @ViewStage Stage stage;

    @LmlActor("dialog") VisDialog dialog;
    @LmlActor("contentTable") WidgetGroup contentTable;
    @LmlActor("lblMessage") VisLabel lblMessage;
    @LmlActor("cancelContainer") ShrinkContainer cancelContainer;

    private final TaskListenerWrapper listenerWrapper = new TaskListenerWrapper();
    private DialogData data;

    public void showDialog(DialogData data) {
        this.data = data;
        interfaceService.showDialog(ModalTaskDialogController.class);
    }

    public void hideDialog() {
        if (data.taskQueue.isRunning()) {
            data.taskQueue.cancel();
        }
        data.taskQueue.setListener(null);
        this.data = null;
        interfaceService.destroyDialog(ModalTaskDialogController.class);
    }

    @Override
    public void doBeforeShow(Window dialog) {

    }

    @LmlAfter() void initView() {
        if (data == null) {
            throw new IllegalStateException("DialogData wasn't set. You should display this dialog by calling ModalTaskDialogController#showDialog(DialogData).");
        }

        cancelContainer.setVisible(data.cancelable);
        lblMessage.setText(data.message);
        data.taskQueue.setListener(listenerWrapper);
        data.taskQueue.execute();
    }

    @LmlAction("onCancelClicked") void onCancelClicked() {
        if (data != null) {
            data.taskQueue.cancel();
        }
    }

    private class TaskListenerWrapper implements JobTask.Listener {
        @Override
        public void onSucceed() {
            if (data.listener != null) {
                data.listener.onSucceed();
            }
            hideDialog();
        }

        @Override
        public void onFailed(String failMessage, Exception failException) {
            if (data.listener != null) {
                data.listener.onFailed(failMessage, failException);
            }
            hideDialog();
        }

        @Override
        public void onCanceled() {
            if (data.listener != null) {
                data.listener.onCanceled();
            }
            hideDialog();
        }
    }

    public static class DialogData {
        final JobTaskQueue taskQueue = new JobTaskQueue("ModalTaskDialogQueue");
        String message = "";
        boolean cancelable = false;
        JobTask.Listener listener;

        public DialogData cancelable() {
            cancelable = true;
            return this;
        }

        public DialogData message(String message) {
            this.message = message;
            return this;
        }

        public DialogData task(JobTask task) {
            taskQueue.addTask(task);
            return this;
        }

        public DialogData listener(JobTask.Listener listener) {
            this.listener = listener;
            return this;
        }
    }
}
