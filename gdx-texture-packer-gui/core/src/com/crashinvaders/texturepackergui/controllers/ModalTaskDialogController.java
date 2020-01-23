package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
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

/** Universal modal dialog to perform {@link JobTask}s. Call {@link #showDialog(DialogData)} to launch process. */
@ViewDialog(id = ModalTaskDialogController.VIEW_ID, value = "lml/dialogModalTask.lml")
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
        if (data == null) return;

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

        cancelContainer.setVisible(data.cancelBehavior != CancelBehavior.NON_CANCELABLE);
        lblMessage.setText(data.message);
        data.taskQueue.setListener(listenerWrapper);
        data.taskQueue.execute();
    }

    @LmlAction("onCancelClicked") void onCancelClicked() {
        if (data != null) {
            data.taskQueue.cancel();
            if (data.cancelBehavior == CancelBehavior.CANCEL_BACKGROUND) {
                hideDialog();
            }
        }
    }

    private class TaskListenerWrapper implements JobTask.Listener {
        @Override
        public void onSucceed() {
            if (data != null && data.listener != null) {
                data.listener.onSucceed();
            }
            hideDialog();
        }

        @Override
        public void onFailed(String failMessage, Exception failException) {
            if (data != null && data.listener != null) {
                data.listener.onFailed(failMessage, failException);
            }
            hideDialog();
        }

        @Override
        public void onCanceled() {
            if (data != null && data.listener != null) {
                data.listener.onCanceled();
            }
            hideDialog();
        }
    }

    public enum CancelBehavior {
        /** Cancel button is hidden, there is no way to close dialog before the job gets done. */
        NON_CANCELABLE,
        /** Cancel will be requested for the job, but dialog will stay until the job returns. */
        CANCEL_AWAIT,
        /** Cancel will be requested for the job, the dialog will be hidden, and the job will be returned in the background. */
        CANCEL_BACKGROUND
    }

    public static class DialogData {
        final JobTaskQueue taskQueue = new JobTaskQueue("ModalTaskDialogQueue");
        String message = "";
        CancelBehavior cancelBehavior = CancelBehavior.NON_CANCELABLE;
        JobTask.Listener listener;

        public JobTaskQueue getTaskQueue() {
            return taskQueue;
        }

        public DialogData cancelBehavior(CancelBehavior cancelBehavior) {
            this.cancelBehavior = cancelBehavior;
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
