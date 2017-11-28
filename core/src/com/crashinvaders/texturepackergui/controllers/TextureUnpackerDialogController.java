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

    @LmlActor("edtAtlasPath") VisTextField edtAtlasPath;
    @LmlActor("edtOutputDir") VisTextField edtOutputDir;

    @ViewStage Stage stage;

    @LmlAction("onAtlasPathChanged") void onAtlasPathChanged() {

    }

    @LmlAction("onOutputDirChanged") void onOutputDirChanged() {

    }

    @LmlAction("pickAtlasPath") void pickAtlasPath() {
        FileHandle dir = FileUtils.obtainIfExists(edtAtlasPath.getText());

        final FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                .rule("Texture atlas (*.json;*.pack;*.atlas)", "json", "pack", "atlas").get());
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                edtAtlasPath.setText(chosenFile.path());
            }
        });
        stage.addActor(fileChooser.fadeIn());

    }

    @LmlAction("pickOutputDir") void pickOutputDir() {
        FileHandle dir = FileUtils.obtainIfExists(edtOutputDir.getText());
        if (dir == null && FileUtils.fileExists(edtAtlasPath.getText())) {
            dir = FileUtils.obtainIfExists(edtAtlasPath.getText()).parent();
        }

        FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                FileHandle chosenFile = file.first();
                edtOutputDir.setText(chosenFile.path());
            }
        });
        stage.addActor(fileChooser.fadeIn());
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
        interfaceService.showDialog(errorDialogController.getClass());
    }
}
