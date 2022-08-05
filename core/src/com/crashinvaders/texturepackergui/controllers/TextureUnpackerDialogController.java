package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tools.texturepacker.TextureUnpacker;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.utils.AppIconProvider;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

import java.awt.*;
import java.io.IOException;

@ViewDialog(id = "dialog_texture_unpacker", value = "lml/textureunpacker/dialogTextureUnpacker.lml")
public class TextureUnpackerDialogController implements ActionContainer {
    private static final String TAG = TextureUnpackerDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject ErrorDialogController errorDialogController;
    @Inject FileDialogService fileDialogService;

    @LmlActor("edtAtlasPath") VisTextField edtAtlasPath;
    @LmlActor("edtOutputDir") VisTextField edtOutputDir;

    @ViewStage Stage stage;

    @LmlAction("onAtlasPathChanged") void onAtlasPathChanged() {

    }

    @LmlAction("onOutputDirChanged") void onOutputDirChanged() {

    }

    @LmlAction("pickAtlasPath") void pickAtlasPath() {
        String atlasPath = edtAtlasPath.getText().trim();
        FileHandle dir = null;
        if (!Strings.isNotEmpty(atlasPath)) {
            dir = FileUtils.obtainIfExists(atlasPath);
        }

        fileDialogService.openFile("Select libGDX atlas file", dir,
                FileDialogService.FileFilter.createSingle("Texture atlas (*.json;*.pack;*.atlas)", "json", "pack", "atlas"),
        new FileDialogService.CallbackAdapter() {
            @Override
            public void selected(Array<FileHandle> files) {
                FileHandle chosenFile = files.first();
                edtAtlasPath.setText(chosenFile.path());
            }
        });

    }

    @LmlAction("pickOutputDir") void pickOutputDir() {
        String outputDirPath = edtOutputDir.getText().trim();
        FileHandle dir = null;
        if (Strings.isNotEmpty(outputDirPath)) {
            dir = Gdx.files.absolute(outputDirPath);
            if (!dir.exists()) {
                dir = FileUtils.findFirstExistentParent(dir);
            }
        }

        fileDialogService.pickDirectory(null, dir,
                new FileDialogService.CallbackAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle chosenFile = files.first();
                        edtOutputDir.setText(chosenFile.path());
                    }
                });
    }

    @LmlAction("launchUnpackProcess") void launchUnpackProcess() {
        try {
            Gdx.app.log(TAG, "Start texture unpacking");

            FileHandle outputDir = Gdx.files.absolute(edtOutputDir.getText());
            FileHandle atlasFile = FileUtils.obtainIfExists(edtAtlasPath.getText());
            if (atlasFile == null) throw new IllegalStateException("Atlas file does not exist: " + edtAtlasPath.getText());

            TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(atlasFile, atlasFile.parent(), false);

            TextureUnpacker textureUnpacker = new TextureUnpacker();
            textureUnpacker.splitAtlas(atlasData, outputDir.path());

            Gdx.app.log(TAG, "Texture unpacking successfully finished");
            showSuccessfulDialog(outputDir);
        } catch (Exception e) {
            Gdx.app.log(TAG, "Texture unpacking failed with error", e);
            showErrorDialog(e);
        }
    }

    private void showSuccessfulDialog(final FileHandle outputDir) {
        VisDialog dialog = (VisDialog)interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/textureunpacker/dialogSuccess.lml")).first();

        dialog.findActor("btnOpenOutputDir").addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    Desktop.getDesktop().open(outputDir.file());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.show(stage);
        stage.setScrollFocus(dialog);
    }

    private void showErrorDialog(Exception e) {
        errorDialogController.setError(e);
        errorDialogController.showDialog();
    }
}
