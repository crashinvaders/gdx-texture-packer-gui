package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Null;
import com.crashinvaders.texturepackergui.views.ContentDialog;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import com.kotcrab.vis.ui.widget.*;

public class WidgetUtils {
    /**
     * @param lmlTemplate lml template with content that will be placed in dialog
     */
    public static ContentDialog showContentDialog(InterfaceService interfaceService, String title, FileHandle lmlTemplate) {
        Actor content = interfaceService.getParser().parseTemplate(lmlTemplate).first();
        ContentDialog dialog = new ContentDialog(title, content);
        dialog.show(interfaceService.getCurrentController().getStage());
        return dialog;
    }

    /** Obtains close button from {@link VisWindow} */
    public static VisImageButton obtainCloseButton(VisWindow window) {
        return  (VisImageButton) window.getTitleTable().getChildren().peek();
    }

    /**
     * A bit stylized drop-in replacement for {@link Dialogs.InputDialog}
     */
    public static ContentDialog createInputDialog(String title, @Null String fieldTitle, @Null String fieldText, boolean cancelable, InputDialogListener listener) {

        VisTextButton cancelButton;
        VisTextButton okButton;

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setIgnoreSpacing(true);
        buttonBar.setOrder(ButtonBar.WINDOWS_ORDER);
        buttonBar.setButton(ButtonBar.ButtonType.CANCEL, cancelButton = new VisTextButton(ButtonBar.ButtonType.CANCEL.getText()));
        buttonBar.setButton(ButtonBar.ButtonType.OK, okButton = new VisTextButton(ButtonBar.ButtonType.OK.getText()));

        VisTable fieldTable = new VisTable(true);

        VisTextField field;
//        if (validator == null)
            field = new VisTextField();
//        else
//            field = new VisValidatableTextField(validator);

        if (fieldTitle != null) fieldTable.add(new VisLabel(fieldTitle));

        fieldTable.add(field).expand().fill().minWidth(280f);

        ContentDialog dialog = new ContentDialog(title, fieldTable);
        dialog.setupButtons(buttonBar);

        TableUtils.setSpacingDefaults(dialog);
        dialog.setModal(true);

        if (cancelable) {
            dialog.addCloseButton();
            dialog.closeOnEscape();
        }

        // Setup listeners.
        {
            okButton.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    listener.finished(field.getText());
                    dialog.fadeOut();
                }
            });

            cancelButton.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    listener.canceled();
                    dialog.fadeOut();
                }
            });

            field.addListener(new InputListener() {
                @Override
                public boolean keyDown (InputEvent event, int keycode) {
                    if (keycode == Input.Keys.ENTER && okButton.isDisabled() == false) {
                        listener.finished(field.getText());
                        dialog.fadeOut();
                    }

                    return super.keyDown(event, keycode);
                }
            });
        }

//        if (validator != null) {
//            addValidatableFieldListener(field);
//            okButton.setDisabled(!field.isInputValid());
//        }

        dialog.pack();
        dialog.centerWindow();

        field.focusField();
        if (fieldText != null) {
            field.setText(fieldText);
            // We need to add field to the stage before we can change the selection.
            field.addAction(Actions.run(() -> {
                field.focusField();
                field.selectAll();
            }));
        }

        return dialog;
    }
}
